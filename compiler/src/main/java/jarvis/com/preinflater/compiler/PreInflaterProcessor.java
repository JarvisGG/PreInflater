package jarvis.com.preinflater.compiler;

import android.support.annotation.NonNull;

import com.hendraanggrian.RParser;

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import jarvis.com.preinflater.annotation.PreInflater;

/**
 * @author yyf @ Zhihu Inc.
 * @since 03-30-2019
 */
@SupportedAnnotationTypes({PreInflaterProcessor.ANNOTATION_TYPE_PREINFLATER})
@SupportedOptions({PreInflaterProcessor.OPTION_MODULE_NAME})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PreInflaterProcessor extends AbstractProcessor {

    static final String ANNOTATION_TYPE_PREINFLATER = "jarvis.com.preinflater.annotation.PreInflater";
    static final String OPTION_MODULE_NAME = "moduleNameOfPreInflater";

    static String CLASS_NAME = "";

    private static final Set<Class<? extends Annotation>> SUPPORTED_ANNOTATIONS = new HashSet<>(Collections.<Class<? extends Annotation>>singletonList(PreInflater.class));

    public static class Info {
        public String layoutResStr;
        public String scheduler;

        public Info(String layoutResStr, String scheduler) {
            this.layoutResStr = layoutResStr;
            this.scheduler = scheduler;
        }
    }

    private RParser parser;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new HashSet<>();
        for (Class<? extends Annotation> cls : SUPPORTED_ANNOTATIONS) {
            supported.add(cls.getCanonicalName());
        }
        return supported;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        parser = RParser.builder(processingEnv)
                .setSupportedAnnotations(SUPPORTED_ANNOTATIONS)
                .setSupportedTypes("layout")
                .build();

        parser.scan(roundEnv);
        Map<Integer, Info> map = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(PreInflater.class)) {
            if (element instanceof TypeElement) {

                String scheduler = element.getAnnotation(PreInflater.class).scheduler();
                int layoutRes = element.getAnnotation(PreInflater.class).layout();

                String holderClass = ((TypeElement) element).getQualifiedName().toString();

                CLASS_NAME = generateClassName();

                String layoutResStr = null;
                String packageName = null;
                for (String path : holderClass.split("\\.")) {
                    if (packageName == null) {
                        packageName = path;
                    } else {
                        packageName = packageName + "." + path;
                    }

                    layoutResStr = parser.parse(packageName, layoutRes);
                    if (!layoutResStr.equals(String.valueOf(layoutRes))) {
                        break;
                    }
                }

                if (layoutResStr == null || layoutResStr.equals(String.valueOf(layoutRes))) {
                    throw new IllegalStateException("process " + holderClass + " failed!");
                }

                map.put(layoutRes, new Info(layoutResStr, scheduler));
            }
        }

        if (map.size() > 0) {
            generatePreInflaterMapper(map);
        }
        return true;
    }

    private void generatePreInflaterMapper(@NonNull Map<Integer, Info> map) {
        StringBuilder builder = new StringBuilder();
        String packageName = "com.jarvis.android.preinflater";
        builder.append("package ").append(packageName).append(";\n\n");

        builder.append("import java.util.HashMap;\n");
        builder.append("import java.util.Map;\n\n");
        builder.append("import jarvis.com.preinflater.PreInflaterManager;\n\n");

        builder.append("public final class ").append(CLASS_NAME).append(" {\n\n");
        builder.append("    public static void inject() {\n");

        for (int key : map.keySet()) {
            builder.append("        PreInflaterManager.$.addPreInflateInfo(new PreInflaterManager.PreInflateInfo(")
                    .append(map.get(key).layoutResStr)
                    .append(", \"")
                    .append(map.get(key).scheduler)
                    .append("\"));\n");
        }
        builder.append("    }\n");
        builder.append("}\n");

        JavaFileObject object = null;
        try {
            object = processingEnv.getFiler().createSourceFile(packageName + "." + CLASS_NAME);
            Writer writer = object.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateClassName() {
        String moduleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
        String finalHeaderName = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        return finalHeaderName + "$R2InflaterMapper";
    }
}
