package jarvis.com.preinflater.plugin.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.Opcodes


class AnnotationMethodVisitor extends AdviceAdapter {

    def superName

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api
     *            the ASM API version implemented by this visitor. Must be one
     *            of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param mv
     *            the method visitor to which this adapter delegates calls.
     * @param access
     *            the method's access flags (see {@link Opcodes}).
     * @param name
     *            the method's name.
     * @param desc
     *            the method's descriptor (see {@link org.objectweb.asm.Type Type}).
     */
    protected
    AnnotationMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc, String superName) {
        super(api, mv, access, name, desc)
        this.superName = superName
    }

    @Override
    void visitCode() {

        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESTATIC, "jarvis/com/preinflater/AsyncWrapperLayoutInflater", "getInstance", "(Landroid/content/Context;)Ljarvis/com/preinflater/AsyncWrapperLayoutInflater;", false)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKEVIRTUAL, "jarvis/com/preinflater/AsyncWrapperLayoutInflater", "wrapContext", "(Landroid/content/Context;)Landroid/content/Context;", false)
        mv.visitMethodInsn(INVOKESPECIAL, superName, "attachBaseContext", "(Landroid/content/Context;)V", false)

    }

    @Override
    protected void onMethodEnter() {

    }
}