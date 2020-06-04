package net.optionfactory.pebbel.compiled;

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

    public static class Request<VAR_METADATA_TYPE> {
        private final Bindings<String, Function, FunctionDescriptor> functions;
        private final Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variableDescriptors;
        private final ClassWriter classWriter = new ClassWriter((ClassWriter.COMPUTE_MAXS));
        private final MethodVisitor methodVisitor;
        private final String name;

        private Request(Bindings<String, Function, FunctionDescriptor> functions, Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variableDescriptors) {
            this.name = "Blob" + Double.valueOf(Math.random() * 1000).intValue(); // FIXME
            this.functions = functions;
            this.variableDescriptors = variableDescriptors;

            classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, name, null, "java/lang/Object", new String[]{"net/optionfactory/pebbel/compiled/CompiledExpression"});
            final MethodVisitor constructorVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            constructorVisitor.visitCode();
            constructorVisitor.visitVarInsn(ALOAD, 0);
            constructorVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructorVisitor.visitInsn(RETURN);
            constructorVisitor.visitMaxs(1, 1);
            constructorVisitor.visitEnd();

            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "evaluate", "(Lnet/optionfactory/pebbel/loading/Bindings;)Ljava/lang/Object;", null, null);
            methodVisitor.visitParameter("varBindings", 0);
            methodVisitor.visitCode();
        }
    }

    public <R, VAR_TYPE> Result<CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, R>> compile(
            Bindings<String, Function, FunctionDescriptor> functionBindings,
            Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variableDescriptors,
            Expression expression) {
        try {
            final Request<VAR_METADATA_TYPE> req = new Request<>(functionBindings, variableDescriptors);
            expression.accept(this, req);
            req.methodVisitor.visitInsn(ARETURN);
            req.methodVisitor.visitMaxs(0, 0);
            req.methodVisitor.visitEnd();
            req.classWriter.visitEnd();
            final byte[] bytecode = req.classWriter.toByteArray();
            lastGeneratedBytecode = bytecode;
            final DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(); // One classloader per
            final Class<CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, R>> clazz = dynamicClassLoader.defineClass(req.name, bytecode);
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
        final Method method = request.functions.values().get(node.function).method();
        final Type methodType = Type.getType(method);
        final Type[] argumentTypes = methodType.getArgumentTypes();

        for (int i = 0; i < node.arguments.length; i++) {
            final Class<?> resultType = node.arguments[i].accept(this, request);
            if (resultType != descriptor.parameters[i].type) {
                request.methodVisitor.visitTypeInsn(CHECKCAST, argumentTypes[i].getInternalName());
            }
        }

        request.methodVisitor.visitMethodInsn(INVOKESTATIC, Type.getType(method.getDeclaringClass()).getInternalName(), method.getName(), Type.getType(method).getDescriptor(), false);
        return method.getReturnType();
    }

    @Override
    public Class<?> visit(ShortCircuitExpression node, Request<VAR_METADATA_TYPE> request) {
        final Type booleanType = Type.getType(Boolean.class);
        final Label end = new Label();
        for (int i = 0; i < node.terms.length; i++) {
            final Class<?> resultType = node.terms[i].accept(this, request);
            if (resultType != Boolean.class) {
                request.methodVisitor.visitTypeInsn(CHECKCAST, booleanType.getInternalName());
            }
            if (i < node.operators.length) {
                request.methodVisitor.visitInsn(DUP);
                request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                final BooleanOperator operator = node.operators[i];
                request.methodVisitor.visitJumpInsn(node.operators[i].shortCircuitsOn() ? IFNE : IFEQ, end);
                request.methodVisitor.visitInsn(POP);
            } else {
                request.methodVisitor.visitLabel(end);
                request.methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Boolean"});
            }
        }
        return Boolean.class;
    }
}
