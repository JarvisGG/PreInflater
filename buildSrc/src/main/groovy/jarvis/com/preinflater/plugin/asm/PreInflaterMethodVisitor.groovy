package jarvis.com.preinflater.plugin.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class PreInflaterMethodVisitor extends AdviceAdapter {

    private final Set<String> inflaters

    private String name

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
     *            the method's descriptor (see {@link org.objectweb.asm.Type}).
     */
    protected PreInflaterMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc, Set<String> inflaters) {
        super(api, mv, access, name, desc)
        this.name = name
        this.inflaters = inflaters
    }

    @Override
    protected void onMethodEnter() {
        for (String inflater : inflaters) {
            System.out.println("onMethodEnter name --------------------> " + name)

            mv.visitMethodInsn(INVOKESTATIC, inflater, "inject", "()V", false)

        }
    }
}