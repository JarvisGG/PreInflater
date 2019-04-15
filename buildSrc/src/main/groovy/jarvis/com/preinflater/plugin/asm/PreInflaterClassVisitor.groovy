package jarvis.com.preinflater.plugin.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author yyf @ Zhihu Inc.
 * @since 04-01-2019
 */
class PreInflaterClassVisitor extends ClassVisitor {
    final Set<String> inflaters

    PreInflaterClassVisitor(ClassVisitor cv, Set<String> inflaters) {
        super(Opcodes.ASM4, cv)
        this.inflaters = inflaters
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        return name != '_collectPreInflater' ? mv : new PreInflaterMethodVisitor(api, mv, access, name, desc, inflaters)
    }
}
