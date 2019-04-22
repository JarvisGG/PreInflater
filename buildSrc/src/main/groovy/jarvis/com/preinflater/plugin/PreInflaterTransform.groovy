package jarvis.com.preinflater.plugin


import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.api.transform.Transform
import com.android.utils.FileUtils
import com.google.common.collect.ImmutableSet
import com.google.common.io.ByteStreams
import com.google.common.io.Files
import jarvis.com.preinflater.plugin.asm.AnnotationClassVisitor
import jarvis.com.preinflater.plugin.asm.PreInflaterClassVisitor
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.nio.file.Path
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class PreInflaterTransform extends Transform {

    private final Project project

    private static final Set<String> inflaters = new HashSet<>()


    PreInflaterTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "collectPreInflateTransform"
    }

    /**
     * 只处理 .class
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * SCOPE_FULL_PROJECT = Sets.immutableEnumSet(Scope.PROJECT, new Scope[]{Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES});
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * PROVIDED_ONLY compileOnly xxx
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return ImmutableSet.of(QualifiedContent.Scope.PROVIDED_ONLY, QualifiedContent.Scope.TESTED_CODE)
    }

    /**
     * Returns whether the Transform can perform incremental work.
     * If it does, then the TransformInput may contain a list of changed/removed/added files, unless something else triggers a non incremental run.
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }

    def collectClassPatch = { path ->
        String classPath = path.substring(0, path.length() - '.class'.length())
        if (isPreInflater(classPath)) {
            inflaters.add(classPath)
        }
        classPath
    }

    def isPreInflater = { className ->
        className.endsWith('$R2InflaterMapper')
    }

    def getR2PreInflaterMapperFromFile = { Path dirPath, Path classPath ->
        collectClassPatch(dirPath.relativize(classPath).toString())
    }

    def getR2PreInflaterMapperFromJar = { File jarFile ->
        new ZipFile(jarFile).stream()
                .map { entry -> entry.name }
                .filter { p -> p.endsWith(".class") }
                .map { p -> collectClassPatch(p)}
                .collect()
    }

    /**
     * ASM 注入
     * @param classBytes
     * @return
     */
    def injectMethod = { classBytes ->
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        final PreInflaterClassVisitor visitor = new PreInflaterClassVisitor(writer, inflaters)
        final ClassReader reader = new ClassReader(classBytes)
        reader.accept(visitor, ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    @Override
    void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        final TransformOutputProvider outputProvider = invocation.getOutputProvider()
        if (!invocation.isIncremental()) {
            outputProvider.deleteAll()
        }

        File preInflaterJarFile = null
        boolean isPreInflaterJarFound = false
        final Pattern preinflaterPattern = Pattern.compile('com.github.JarvisGG.PreInflater:library:(.*)')
        final Predicate<String> isPreInflaterJar = {
            jarName -> jarName == ':library' || preinflaterPattern.matcher(jarName).matches()
        }

        invocation.referencedInputs.each { input ->
            input.directoryInputs.each { directoryInput ->
                final Path dirPath = directoryInput.file.toPath()
                FileUtils.getAllFiles(directoryInput.file).stream()
                        .map { f -> getClassNameFromFile(dirPath, f.toPath()) }
                        .collect()
            }
        }

        invocation.inputs.each { input ->
            input.directoryInputs.each { directoryInput ->

                final Path dirPath = directoryInput.file.toPath()

                FileUtils.getAllFiles(directoryInput.file).stream()
                        .map { f -> getR2PreInflaterMapperFromFile(dirPath, f.toPath()) }
                        .collect()

                injectAttachBaseContextClassFile(outputProvider, directoryInput)
            }

            input.jarInputs.each { jarInput ->

                FileUtils.getAllFiles(jarInput.file).stream()
                        .map { f -> getR2PreInflaterMapperFromJar(f) }
                        .collect()

                def outJarFile = injectAttachBaseContextClassJar(outputProvider, jarInput)

                if (!isPreInflaterJarFound && isPreInflaterJar.test(jarInput.name)) {
                    preInflaterJarFile = outJarFile
                    isPreInflaterJarFound = true
                }
            }
        }

        if (isPreInflaterJarFound) {
            injectCollectMethod(preInflaterJarFile)
        }

    }

    def injectCollectMethod = { File preInflaterJarFile ->

        File tmpFile = new File(project.buildDir, String.join(File.separatorChar.toString(), 'tmp', 'inflater', 'inflater-tmp.jar'))
        Files.createParentDirs(tmpFile)

        new ZipInputStream(new FileInputStream(preInflaterJarFile)).withCloseable { zis ->
            new ZipOutputStream(new FileOutputStream(tmpFile)).withCloseable { zos ->
                ZipEntry entry
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.name.endsWith('PreInflaterManager.class')) {

                        zos.putNextEntry(new ZipEntry(entry.name))
                        zos.write(injectMethod(ByteStreams.toByteArray(zis)))
                    } else {
                        zos.putNextEntry(entry)
                        ByteStreams.copy(zis, zos)
                    }
                    zos.closeEntry()
                    zis.closeEntry()
                }
            }
        }
        FileUtils.copyFile(tmpFile, preInflaterJarFile)
        FileUtils.delete(tmpFile)
    }

    /**
     * 替换 Activity attachBaseActivity 代码
     */
    def injectAttachBaseContextClassJar = { outputProvider, jarFile ->

        File tmpFile = new File(project.buildDir, String.join(File.separatorChar.toString(), 'tmp', 'inflater', jarFile.file.name + '-tmp.jar'))
        Files.createParentDirs(tmpFile)

        new ZipInputStream(new FileInputStream(jarFile.file)).withCloseable { zis ->
            new ZipOutputStream(new FileOutputStream(tmpFile)).withCloseable { zos ->
                ZipEntry entry
                while ((entry = zis.getNextEntry()) != null) {
                    byte[] modified = injectClass(ByteStreams.toByteArray(zis))
                    zos.putNextEntry(new ZipEntry(entry.name))
                    zos.write(modified)
//                  zos.putNextEntry(entry)
//                  ByteStreams.copy(zis, zos)
                    zos.closeEntry()
                    zis.closeEntry()
                }
            }
        }

        def dest = outputProvider.getContentLocation(
                jarFile.name,
                jarFile.contentTypes,
                jarFile.scopes,
                Format.JAR
        )
        FileUtils.copyFile(tmpFile, dest)
        FileUtils.delete(tmpFile)

        return dest
    }

    def injectAttachBaseContextClassFile = { outputProvider, directoryInput ->

        if (directoryInput.file.isDirectory()) {
            directoryInput.file.eachFileRecurse { File file ->
                if (file.isFile()) {
                    byte[] modified = injectClass(file.bytes)
                    File destFile = new File(file.parentFile.absoluteFile, file.name)
                    FileOutputStream fileOutputStream = new FileOutputStream(destFile)
                    fileOutputStream.write(modified)
                    fileOutputStream.close()
                }
            }
        }

        def dest = outputProvider.getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY
        )
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    def injectClass = { contents ->
        try {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            ClassVisitor visitor = new AnnotationClassVisitor(writer)
            ClassReader reader = new ClassReader(contents)
            reader.accept(visitor, ClassReader.EXPAND_FRAMES)
            writer.toByteArray()
        } catch (ignored) {
            contents
        }

    }


}