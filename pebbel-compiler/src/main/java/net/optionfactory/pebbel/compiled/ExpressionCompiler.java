package net.optionfactory.pebbel.compiled;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.optionfactory.pebbel.loading.*;
import net.optionfactory.pebbel.parsing.ast.*;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Method;
import java.util.Map;

public class ExpressionCompiler<VAR_METADATA_TYPE> implements Expression.Visitor<Class<?>, ExpressionCompiler.Request<VAR_METADATA_TYPE>> {

    public static byte[] lastGeneratedBytecode; // FIXME
    private static final Method sourceOf;
    static {
        try {
            sourceOf = Source.class.getMethod("of", int.class, int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Request<VAR_METADATA_TYPE> {
        private final Bindings<String, Method, FunctionDescriptor> functions;
        private final Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variableDescriptors;
        private MethodVisitor methodVisitor;

        private Request(Bindings<String, Method, FunctionDescriptor> functions, Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variableDescriptors) {
            this.functions = functions;
            this.variableDescriptors = variableDescriptors;
        }
    }

    public <R, VAR_TYPE> Result<CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, R>> compile(
            Bindings<String, Method, FunctionDescriptor> functionBindings,
            Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variableDescriptors,
            Expression expression, Class<R> expectedType) {
        if (expectedType.isPrimitive()) {
            throw new IllegalArgumentException("expectedType must be a reference type");
        }
        try {
            final String name = "Blob" + Double.valueOf(Math.random() * 1000).intValue(); // FIXME
            final Request<VAR_METADATA_TYPE> request = new Request<>(functionBindings, variableDescriptors);
            final ClassWriter classWriter = new ClassWriter((ClassWriter.COMPUTE_MAXS));
            classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, name, null, "java/lang/Object", new String[]{"net/optionfactory/pebbel/compiled/CompiledExpression"});
            final MethodVisitor constructorVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            constructorVisitor.visitCode();
            constructorVisitor.visitVarInsn(ALOAD, 0);
            constructorVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructorVisitor.visitInsn(RETURN);
            constructorVisitor.visitMaxs(1, 1);
            constructorVisitor.visitEnd();

            request.methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "evaluate", "(Lnet/optionfactory/pebbel/loading/Bindings;)Ljava/lang/Object;", null, null);
            request.methodVisitor.visitParameter("varBindings", 0);
            request.methodVisitor.visitCode();
            final Label tryStart = new Label();
            final Label tryEnd = new Label();
            request.methodVisitor.visitTryCatchBlock(tryStart, tryEnd, tryEnd, "java/lang/Throwable");
            request.methodVisitor.visitIntInsn(SIPUSH, 0);
            request.methodVisitor.visitInsn(DUP);
            request.methodVisitor.visitInsn(DUP);
            request.methodVisitor.visitInsn(DUP);
            request.methodVisitor.visitVarInsn(ISTORE, 2);
            request.methodVisitor.visitVarInsn(ISTORE, 3);
            request.methodVisitor.visitVarInsn(ISTORE, 4);
            request.methodVisitor.visitVarInsn(ISTORE, 5);
            request.methodVisitor.visitFrame(Opcodes.F_FULL, 6, new Object[] {name, "net/optionfactory/pebbel/loading/Bindings", INTEGER, INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
            request.methodVisitor.visitLabel(tryStart);


            final Class<?> resultType = expression.accept(this, request);
            Assigner.DEFAULT.assign(
                    TypeDescription.ForLoadedType.of(resultType).asGenericType(),
                    TypeDescription.ForLoadedType.of(expectedType).asGenericType(),
                    Assigner.Typing.DYNAMIC)
                    .apply(new MethodVisitorAdapter(request.methodVisitor), null);
            request.methodVisitor.visitInsn(ARETURN);

            request.methodVisitor.visitLabel(tryEnd);
            request.methodVisitor.visitFrame(Opcodes.F_FULL, 6, new Object[] {name, "net/optionfactory/pebbel/loading/Bindings", INTEGER, INTEGER, INTEGER, INTEGER}, 1, new Object[] {"java/lang/Throwable"});
            request.methodVisitor.visitVarInsn(ASTORE, 6);
            request.methodVisitor.visitTypeInsn(NEW, "net/optionfactory/pebbel/compiled/ExecutionException");
            request.methodVisitor.visitInsn(DUP);
            request.methodVisitor.visitVarInsn(ALOAD, 6);
            request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;", false);
            request.methodVisitor.visitVarInsn(ILOAD, 2);
            request.methodVisitor.visitVarInsn(ILOAD, 3);
            request.methodVisitor.visitVarInsn(ILOAD, 4);
            request.methodVisitor.visitVarInsn(ILOAD, 5);
            request.methodVisitor.visitMethodInsn(INVOKESTATIC, Type.getType(Source.class).getInternalName(), sourceOf.getName(), Type.getType(sourceOf).getDescriptor(), false);
            request.methodVisitor.visitVarInsn(ALOAD, 6);
            request.methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/optionfactory/pebbel/compiled/ExecutionException", "<init>", "(Ljava/lang/String;Lnet/optionfactory/pebbel/parsing/ast/Source;Ljava/lang/Throwable;)V", false);
            request.methodVisitor.visitInsn(ATHROW);
            // TODO: visitVarNames + visitParameters
            // TODO: Reflection on types instead of strings
            request.methodVisitor.visitMaxs(0, 0);
            request.methodVisitor.visitEnd();
            classWriter.visitEnd();
            final byte[] bytecode = classWriter.toByteArray();
            lastGeneratedBytecode = bytecode;
            final DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(); // One classloader per
            final Class<CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, R>> clazz = dynamicClassLoader.defineClass(name, bytecode);
            return Result.value(clazz.getConstructor().newInstance());

        } catch (Exception ex) {
            return Result.error(Problem.of("COMPILATION_ERROR", ex.getMessage(), null, ex));
        }
    }

    public static class DynamicClassLoader extends ClassLoader {
        @SuppressWarnings("unchecked")
        public <C> Class<C> defineClass(String name, byte[] b) {
            return (Class<C>) defineClass(name, b, 0, b.length);
        }
    }

    @Override
    public Class<?> visit(Expression node, Request<VAR_METADATA_TYPE> request) {
        return node.accept(this, request);
    }

    @Override
    public Class<?> visit(BooleanExpression node, Request<VAR_METADATA_TYPE> request) {
        return node.accept(this, request);
    }

    @Override
    public Class<?> visit(NumberExpression node, Request<VAR_METADATA_TYPE> request) {
        return node.accept(this, request);
    }

    @Override
    public Class<?> visit(StringExpression node, Request<VAR_METADATA_TYPE> request) {
        return node.accept(this, request);
    }

    @Override
    public Class<?> visit(NumberLiteral node, Request<VAR_METADATA_TYPE> request) {
        request.methodVisitor.visitLdcInsn(node.value);
        request.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        return Double.class;
    }

    @Override
    public Class<?> visit(StringLiteral node, Request<VAR_METADATA_TYPE> request) {
        request.methodVisitor.visitLdcInsn(node.literal);
        return String.class;
    }

    @Override
    public Class<?> visit(Variable node, Request<VAR_METADATA_TYPE> request) {
        request.methodVisitor.visitVarInsn(ALOAD, 1);
        request.methodVisitor.visitLdcInsn(node.name);
        request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/optionfactory/pebbel/loading/Bindings", "value", "(Ljava/lang/Object;)Lnet/optionfactory/pebbel/loading/Maybe;", false);
        request.methodVisitor.visitInsn(ACONST_NULL);
        request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/optionfactory/pebbel/loading/Maybe", "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        return Object.class;
    }

    @Override
    public Class<?> visit(FunctionCall node, Request<VAR_METADATA_TYPE> request) {
        final FunctionDescriptor descriptor = request.functions.descriptors().get(node.function);
        final Method method = request.functions.values().get(node.function);
        for (int i = 0; i < node.arguments.length; i++) {
            final Class<?> resultType = node.arguments[i].accept(this, request);
            typeAdapt(request.methodVisitor, resultType, descriptor.parameters[i].type);
        }
        final Label lineNumber = new Label();
        request.methodVisitor.visitLabel(lineNumber);
        request.methodVisitor.visitLineNumber(node.source.row, lineNumber);

        request.methodVisitor.visitIntInsn(SIPUSH, node.source.row);
        request.methodVisitor.visitVarInsn(ISTORE, 2);
        request.methodVisitor.visitIntInsn(SIPUSH, node.source.col);
        request.methodVisitor.visitVarInsn(ISTORE, 3);
        request.methodVisitor.visitIntInsn(SIPUSH, node.source.endRow);
        request.methodVisitor.visitVarInsn(ISTORE, 4);
        request.methodVisitor.visitIntInsn(SIPUSH, node.source.endCol);
        request.methodVisitor.visitVarInsn(ISTORE, 5);
//        request.methodVisitor.visitMethodInsn(INVOKESTATIC, Type.getType(Source.class).getInternalName(), sourceOf.getName(), Type.getType(sourceOf).getDescriptor(), false);
//        request.methodVisitor.visitVarInsn(ASTORE, 2);
        request.methodVisitor.visitMethodInsn(INVOKESTATIC, Type.getType(method.getDeclaringClass()).getInternalName(), method.getName(), Type.getType(method).getDescriptor(), false);
        return method.getReturnType();
    }

    @Override
    public Class<?> visit(ShortCircuitExpression node, Request<VAR_METADATA_TYPE> request) {
        final Label end = new Label();
        for (int i = 0; i < node.terms.length; i++) {
            final Class<?> resultType = node.terms[i].accept(this, request);
            typeAdapt(request.methodVisitor, resultType, Boolean.class);
            if (i < node.operators.length) {
                request.methodVisitor.visitInsn(DUP);
                typeAdapt(request.methodVisitor, Boolean.class, boolean.class);
                request.methodVisitor.visitJumpInsn(node.operators[i].shortCircuitsOn() ? IFNE : IFEQ, end);
                request.methodVisitor.visitInsn(POP);
            } else {
                request.methodVisitor.visitLabel(end);
                request.methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Boolean"});
            }
        }
        return Boolean.class;
    }

    private void typeAdapt(MethodVisitor mv, Class<?> srcType, Class<?> dstType) {
        Assigner.DEFAULT.assign(
                TypeDescription.ForLoadedType.of(srcType).asGenericType(),
                TypeDescription.ForLoadedType.of(dstType).asGenericType(),
                Assigner.Typing.DYNAMIC)
                .apply(new MethodVisitorAdapter(mv), null);
    }

    public static class MethodVisitorAdapter extends net.bytebuddy.jar.asm.MethodVisitor {
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
}
