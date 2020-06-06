package net.optionfactory.pebbel.compiled;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class NoDebugMethodVisitor extends MethodVisitor {

    public NoDebugMethodVisitor(int api, MethodVisitor inner) {
        super(api, inner);
    }

    @Override
    public void visitParameter(String name, int access) {
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
    }

    @Override
    public void visitLineNumber(int line, Label start) {
    }
}
