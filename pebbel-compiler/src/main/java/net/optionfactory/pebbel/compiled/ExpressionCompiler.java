package net.optionfactory.pebbel.compiled;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.Maybe;
import net.optionfactory.pebbel.parsing.ast.*;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static org.objectweb.asm.Opcodes.*;

public class ExpressionCompiler
        extends AbstractVisitor<Class<?>, ExpressionCompiler.Request>
        implements Expression.Visitor<Class<?>, ExpressionCompiler.Request> {

    private static final Type sourceType = Type.getType(Source.class);
    private static final Method sourceOf;
    private static final Type sourceOfType;
    private static final Type compiledExpressionType = Type.getType(CompiledExpression.class);
    private static final Method evaluate;
    private static final Type evaluateType;
    private static final Type bindingsType = Type.getType(Bindings.class);
    private static final Method bindingsValue;
    private static final Type bindingsValueType;
    private static final Type executionExceptionType = Type.getType(ExecutionException.class);
    private static final Type executionExceptionCtorType;
    private static final Type maybeType = Type.getType(Maybe.class);
    private static final Method maybeOrElse;
    private static final Type maybeOrElseType;
    private static final String targetPackage = CompiledExpression.class.getPackage().getName();
    private static final String GEN_SUBPACKAGE = "gen";


    static {
        try {
            sourceOf = Source.class.getMethod("of", int.class, int.class, int.class, int.class);
            sourceOfType = Type.getType(sourceOf);
            evaluate = CompiledExpression.class.getMethod("evaluate", Bindings.class);
            evaluateType = Type.getType(evaluate);
            bindingsValue = Bindings.class.getMethod("value", Object.class);
            bindingsValueType = Type.getType(bindingsValue);
            executionExceptionCtorType = Type.getType(ExecutionException.class.getConstructor(String.class, Source.class, Throwable.class));
            maybeOrElse = Maybe.class.getMethod("orElse", Object.class);
            maybeOrElseType = Type.getType(maybeOrElse);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Request {
        private final Bindings<String, Method, FunctionDescriptor> functions;
        private final MethodVisitor methodVisitor;

        private Request(Bindings<String, Method, FunctionDescriptor> functions, MethodVisitor methodVisitor) {
            this.functions = functions;
            this.methodVisitor = methodVisitor;
        }
    }

    private static AtomicLong i = new AtomicLong();
    private final boolean includeDebugInfo;
    private final boolean remapExceptions;


    public ExpressionCompiler() {
        this(true, true);
    }

    public ExpressionCompiler(boolean includeDebugInfo, boolean remapExceptions) {
        this.includeDebugInfo = includeDebugInfo;
        this.remapExceptions = remapExceptions;
    }

    public <R> Result<CompiledExpression.Unloaded<R>> compile(
            Bindings<String, Method, FunctionDescriptor> functionBindings,
            Expression expression, Class<R> expectedType) {
        if (expectedType.isPrimitive()) {
            throw new IllegalArgumentException("expectedType must be a reference type");
        }
        try {
            final String baseName = "Compiled" + i.incrementAndGet();
            final String internalName = String.join("/", targetPackage.replace('.', '/'), GEN_SUBPACKAGE, baseName);
            final String binaryName = String.join(".", targetPackage, GEN_SUBPACKAGE, baseName);
            final ClassWriter classWriter = new ClassWriter((ClassWriter.COMPUTE_MAXS));
            classWriter.visit(V11, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Object", new String[]{compiledExpressionType.getInternalName()});
            generateCtor(classWriter);

            final MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, evaluate.getName(), evaluateType.getDescriptor(), null, null);
            final Request request = new Request(functionBindings, includeDebugInfo ? methodVisitor : new NoDebugMethodVisitor(ASM8, methodVisitor));
            request.methodVisitor.visitParameter("varBindings", 0);
            request.methodVisitor.visitCode();
            final Label begin = new Label();
            request.methodVisitor.visitLabel(begin);
            final Label tryStart = new Label();
            final Label tryEnd = new Label();
            if (remapExceptions) {
                request.methodVisitor.visitTryCatchBlock(tryStart, tryEnd, tryEnd, "java/lang/Throwable");
                generateSourceUpdate(Source.of(0, 0, 0, 0), request.methodVisitor);
                request.methodVisitor.visitFrame(Opcodes.F_FULL, 6, new Object[]{internalName, bindingsType.getInternalName(), INTEGER, INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
                request.methodVisitor.visitLabel(tryStart);
            }

            final Class<?> resultType = expression.accept(this, request);
            Assigner.DEFAULT.assign(
                    TypeDescription.ForLoadedType.of(resultType).asGenericType(),
                    TypeDescription.ForLoadedType.of(expectedType).asGenericType(),
                    Assigner.Typing.DYNAMIC)
                    .apply(new MethodVisitorAdapter(request.methodVisitor), null);
            request.methodVisitor.visitInsn(ARETURN);

            final Label end = new Label();
            if (remapExceptions) {
                request.methodVisitor.visitLabel(tryEnd);
                request.methodVisitor.visitFrame(Opcodes.F_FULL, 6, new Object[]{internalName, bindingsType.getInternalName(), INTEGER, INTEGER, INTEGER, INTEGER}, 1, new Object[]{"java/lang/Throwable"});
                request.methodVisitor.visitVarInsn(ASTORE, 6);
                request.methodVisitor.visitTypeInsn(NEW, executionExceptionType.getInternalName());
                request.methodVisitor.visitInsn(DUP);
                request.methodVisitor.visitVarInsn(ALOAD, 6);
                request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;", false);
                request.methodVisitor.visitVarInsn(ILOAD, 2);
                request.methodVisitor.visitVarInsn(ILOAD, 3);
                request.methodVisitor.visitVarInsn(ILOAD, 4);
                request.methodVisitor.visitVarInsn(ILOAD, 5);
                request.methodVisitor.visitMethodInsn(INVOKESTATIC, sourceType.getInternalName(), sourceOf.getName(), sourceOfType.getDescriptor(), false);
                request.methodVisitor.visitVarInsn(ALOAD, 6);
                request.methodVisitor.visitMethodInsn(INVOKESPECIAL, executionExceptionType.getInternalName(), "<init>", executionExceptionCtorType.getDescriptor(), false);
                request.methodVisitor.visitInsn(ATHROW);
                request.methodVisitor.visitLabel(end);
                request.methodVisitor.visitLocalVariable("sourceRow", "I", null, begin, end, 2);
                request.methodVisitor.visitLocalVariable("sourceCol", "I", null, begin, end, 3);
                request.methodVisitor.visitLocalVariable("sourceEndRow", "I", null, begin, end, 4);
                request.methodVisitor.visitLocalVariable("sourceEndCol", "I", null, begin, end, 5);
                request.methodVisitor.visitLocalVariable("ex", "Ljava/lang/Throwable;", null, tryEnd, end, 6);
            } else {
                request.methodVisitor.visitLabel(end);
            }
            request.methodVisitor.visitLocalVariable("this", String.format("L%s;", internalName), null, begin, end, 0);
            request.methodVisitor.visitLocalVariable("varBindings", String.format("L%s;", bindingsType.getInternalName()), null, begin, end, 1);
            request.methodVisitor.visitMaxs(0, 0);
            request.methodVisitor.visitEnd();
            classWriter.visitEnd();
            final byte[] bytecode = classWriter.toByteArray();
            return Result.value(new CompiledExpression.Unloaded<>(binaryName, bytecode));

        } catch (Exception ex) {
            return Result.error(Problem.of("COMPILATION_ERROR", ex.getMessage(), null, ex));
        }
    }

    private void generateCtor(ClassWriter classWriter) {
        final MethodVisitor constructorVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(1, 1);
        constructorVisitor.visitEnd();
    }

    private void generateSourceUpdate(Source source, MethodVisitor methodVisitor) {
        methodVisitor.visitLdcInsn(source.row);
        methodVisitor.visitVarInsn(ISTORE, 2);
        methodVisitor.visitLdcInsn(source.col);
        methodVisitor.visitVarInsn(ISTORE, 3);
        methodVisitor.visitLdcInsn(source.endRow);
        methodVisitor.visitVarInsn(ISTORE, 4);
        methodVisitor.visitLdcInsn(source.endCol);
        methodVisitor.visitVarInsn(ISTORE, 5);
    }

    @Override
    public Class<?> visit(NumberLiteral node, Request request) {
        request.methodVisitor.visitLdcInsn(node.value);
        request.methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        return Double.class;
    }

    @Override
    public Class<?> visit(StringLiteral node, Request request) {
        request.methodVisitor.visitLdcInsn(node.literal);
        return String.class;
    }

    @Override
    public Class<?> visit(Variable node, Request request) {
        generateSourceUpdate(node.source, request.methodVisitor);
        request.methodVisitor.visitVarInsn(ALOAD, 1);
        request.methodVisitor.visitLdcInsn(node.name);
        request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, bindingsType.getInternalName(), bindingsValue.getName(), bindingsValueType.getDescriptor(), false);
        request.methodVisitor.visitInsn(ACONST_NULL);
        request.methodVisitor.visitMethodInsn(INVOKEVIRTUAL, maybeType.getInternalName(), maybeOrElse.getName(), maybeOrElseType.getDescriptor(), false);
        return Object.class;
    }

    @Override
    public Class<?> visit(FunctionCall node, Request request) {
        final FunctionDescriptor descriptor = request.functions.descriptors().get(node.function);
        final Method method = request.functions.values().get(node.function);
        for (int i = 0; i < node.arguments.length; i++) {
            final Class<?> resultType = node.arguments[i].accept(this, request);
            typeAdapt(request.methodVisitor, resultType, descriptor.parameters[i].type);
        }
        final Label lineNumber = new Label();
        request.methodVisitor.visitLabel(lineNumber);
        request.methodVisitor.visitLineNumber(node.source.row, lineNumber);

        generateSourceUpdate(node.source, request.methodVisitor);
        request.methodVisitor.visitMethodInsn(INVOKESTATIC, Type.getType(method.getDeclaringClass()).getInternalName(), method.getName(), Type.getType(method).getDescriptor(), false);
        return method.getReturnType();
    }

    @Override
    public Class<?> visit(ShortCircuitExpression node, Request request) {
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
}
