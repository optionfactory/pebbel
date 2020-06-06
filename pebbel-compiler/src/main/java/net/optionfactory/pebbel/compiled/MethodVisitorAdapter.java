package net.optionfactory.pebbel.compiled;

import org.objectweb.asm.MethodVisitor;

public class MethodVisitorAdapter extends net.bytebuddy.jar.asm.MethodVisitor {
    private MethodVisitor delegate;

    public MethodVisitorAdapter(MethodVisitor delegate) {
        super(net.bytebuddy.jar.asm.Opcodes.ASM7);
        this.delegate = delegate;
    }

    @Override
    public void visitInsn(int opcode) {
        delegate.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        delegate.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        delegate.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        delegate.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
