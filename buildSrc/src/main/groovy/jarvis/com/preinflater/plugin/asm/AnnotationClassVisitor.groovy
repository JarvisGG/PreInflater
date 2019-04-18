package jarvis.com.preinflater.plugin.asm

import jarvis.com.preinflater.annotation.PreInflater
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class AnnotationClassVisitor extends ClassVisitor {

    def isHasPreInflaterAnnotation = false

    def isHasAttachBaseContext = false

    def superName

    AnnotationClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM4, cv)

    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        isHasAttachBaseContext = (name == 'attachBaseContext')
        return (isHasAttachBaseContext && isHasPreInflaterAnnotation) ? new AnnotationMethodVisitor(api, mv, access, name, desc, superName) : mv
    }

    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        isHasPreInflaterAnnotation = Type.getDescriptor(PreInflater.class).equals(desc)
        return super.visitAnnotation(desc, visible)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.superName = superName
    }

    @Override
    void visitEnd() {
        if (isHasPreInflaterAnnotation) {
//            MethodVisitor mv = super.visitMethod(Opcodes.ACC_PROTECTED, "attachBaseContext", "(Landroid/content/Context;)V", null, null)
//            mv.visitCode()
//            Label l0 = new Label()
//            mv.visitLabel(l0)
//            mv.visitLineNumber(24, l0)
//            mv.visitVarInsn(Opcodes.ALOAD, 0)
//            mv.visitVarInsn(Opcodes.ALOAD, 1)
//            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "jarvis/com/preinflater/AsyncWrapperLayoutInflater", "getInstance", "(Landroid/content/Context;)Ljarvis/com/preinflater/AsyncWrapperLayoutInflater;", false)
//            mv.visitVarInsn(Opcodes.ALOAD, 1)
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "jarvis/com/preinflater/AsyncWrapperLayoutInflater", "wrapContext", "(Landroid/content/Context;)Landroid/content/Context;", false)
//            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/support/v7/app/Activity", "attachBaseContext", "(Landroid/content/Context;)V", false)
//            Label l1 = new Label()
//            mv.visitLabel(l1)
//            mv.visitLineNumber(25, l1)
//            mv.visitInsn(Opcodes.RETURN)
//            Label l2 = new Label()
//            mv.visitLabel(l2)
//            mv.visitLocalVariable("this", "Ljarvis/com/app/Module2FristActivity;", null, l0, l2, 0)
//            mv.visitLocalVariable("newBase", "Landroid/content/Context;", null, l0, l2, 1)
//            mv.visitMaxs(3, 2)
//            mv.visitEnd()
        }
        super.visitEnd()
    }
}