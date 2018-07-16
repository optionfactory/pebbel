package net.optionfactory.pebbel.linking;

import java.util.ArrayList;
import java.util.List;
import net.optionfactory.pebbel.ast.BooleanExpression;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.ast.FunctionCall;
import net.optionfactory.pebbel.ast.NumberExpression;
import net.optionfactory.pebbel.ast.NumberLiteral;
import net.optionfactory.pebbel.ast.ShortCircuitExpression;
import net.optionfactory.pebbel.ast.Source;
import net.optionfactory.pebbel.ast.StringExpression;
import net.optionfactory.pebbel.ast.StringLiteral;
import net.optionfactory.pebbel.ast.Variable;
import net.optionfactory.pebbel.linking.ExpressionVerifier.Request;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.FunctionDescriptor.ParameterDescriptor;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.results.Problem;

/**
 * An AST visitor verifying every symbol referenced can be linked to.
 * Accumulates linking problems.
 */
public class ExpressionVerifier<VDMD> implements Expression.Visitor<Class<?>, Request<VDMD>> {

    public static class Request<VDMD> {

        public Descriptors<VDMD> descriptors;
        public Class<?> expected;
        public List<Problem> problems;

        public static <VDMD> Request<VDMD> of(Descriptors<VDMD> descriptors, Class<?> expected, List<Problem> problems) {
            final Request<VDMD> request = new Request<>();
            request.problems = problems;
            request.expected = expected;
            request.descriptors = descriptors;
            return request;
        }
    }

    public List<Problem> verify(Descriptors<VDMD> descriptors, Expression expression, Class<?> expected) {
        final List<Problem> problems = new ArrayList<>();
        final Class<?> got = expression.accept(this, Request.of(descriptors, expected, problems));
        if (!TypeChecks.isAssignable(expected, got)) {
            problems.add(TYPE_MISMATCH(expression.source(),/*TODO: image*/ null, null, expected, got));
        }
        return problems;
    }

    @Override
    public Class<?> visit(Expression node, Request<VDMD> request) {
        return node.accept(this, request);
    }

    @Override
    public Class<?> visit(StringExpression node, Request<VDMD> request) {
        final Class<?> type = node.accept(this, request);
        if (!String.class.equals(type)) {
            request.problems.add(TYPE_MISMATCH(node.source(),/*TODO: image*/ null, null, String.class, type));
        }
        return type;
    }

    @Override
    public Class<?> visit(BooleanExpression node, Request<VDMD> request) {
        final Class<?> type = node.accept(this, request);
        if (!Boolean.class.equals(type)) {
            request.problems.add(TYPE_MISMATCH(node.source(),/*TODO: string*/ null, null, Boolean.class, type));
        }
        return type;
    }

    @Override
    public Class<?> visit(NumberExpression node, Request<VDMD> request) {
        final Class<?> type = node.accept(this, request);
        if (!Double.class.equals(type)) {
            request.problems.add(TYPE_MISMATCH(node.source(),/*TODO: string*/ null, null, Double.class, type));
        }
        return type;
    }

    @Override
    public Class<?> visit(NumberLiteral node, Request<VDMD> request) {
        return Double.class;
    }

    @Override
    public Class<?> visit(Variable node, Request<VDMD> request) {
        if (!request.descriptors.variables.containsKey(node.name)) {
            request.problems.add(UNKNOWN_SYMBOL(node.source, node.name));
        }
        final VariableDescriptor<VDMD> var = request.descriptors.variables.get(node.name);
        return var != null ? var.type : UnknownType.class;
    }

    @Override
    public Class<?> visit(StringLiteral node, Request<VDMD> request) {
        return String.class;
    }

    @Override
    public Class<?> visit(FunctionCall node, Request<VDMD> request) {
        final Class<?>[] argumentTypes = new Class<?>[node.arguments.length];
        for (int i = 0; i != node.arguments.length; ++i) {
            argumentTypes[i] = node.arguments[i].accept(this, request);
        }
        if (!request.descriptors.functions.containsKey(node.function)) {
            request.problems.add(UNKNOWN_SYMBOL(node.source, node.function));
            return UnknownType.class;
        }
        final FunctionDescriptor descriptor = request.descriptors.functions.get(node.function);
        final boolean arityMatches = !descriptor.vararg ? descriptor.arity == argumentTypes.length : argumentTypes.length >= descriptor.arity - 1;
        if (!arityMatches) {
            request.problems.add(ARITY_MISMATCH(node.source, node.function, descriptor.arity, argumentTypes.length));
            return descriptor.returnType;
        }

        for (int parameterIndex = 0; parameterIndex != descriptor.parameters.length; ++parameterIndex) {
            final ParameterDescriptor parameter = descriptor.parameters[parameterIndex];
            if (!descriptor.vararg || parameterIndex != descriptor.parameters.length - 1) {
                final Class<?> argumentType = argumentTypes[parameterIndex];
                if (!TypeChecks.isAssignable(parameter.type, argumentType)) {
                    request.problems.add(TYPE_MISMATCH(node.source, node.function, parameterIndex, parameter, argumentType));
                }
                continue;
            }
            final Class<?> parameterComponentType = parameter.type.getComponentType();
            for (int argumentIndex = parameterIndex; argumentIndex != argumentTypes.length; ++argumentIndex) {
                final Class<?> argumentType = argumentTypes[argumentIndex];
                //arrays are variant
                if (!TypeChecks.isAssignable(parameterComponentType, argumentType)) {
                    request.problems.add(TYPE_MISMATCH(node.source, node.function, argumentIndex, parameterComponentType, argumentType));
                }
            }
        }
        return descriptor.returnType;
    }

    @Override
    public Class<?> visit(ShortCircuitExpression node, Request<VDMD> request) {
        for (BooleanExpression term : node.terms) {
            final Class<?> termType = term.accept(this, request);
            if (!Boolean.class.equals(termType)) {
                request.problems.add(TYPE_MISMATCH(term.source(),/*todo: string*/ null, null, boolean.class, termType));
            }
        }
        return Boolean.class;
    }

    private static Problem LINKING_PROBLEM(String type, String reason, Source source, String symbol, Integer index, Object expected, Object got) {
        final LinkingProblemDetails details = new LinkingProblemDetails();
        details.source = source;
        details.symbol = symbol;
        details.index = index;
        details.expected = expected;
        details.got = got;
        return Problem.of(type, reason, details);
    }

    private static Problem TYPE_MISMATCH(Source source, String symbol, Integer index, Object expected, Object got) {
        return LINKING_PROBLEM("TYPE_MISMATCH", "Type mismatch", source, symbol, index, expected, got);
    }

    private static Problem ARITY_MISMATCH(Source source, String symbol, int expected, int got) {
        return LINKING_PROBLEM("ARITY_MISMATCH", "Arity mismatch", source, symbol, null, expected, got);
    }

    private static Problem UNKNOWN_SYMBOL(Source source, String symbol) {
        return LINKING_PROBLEM("UNKNOWN_SYMBOL", "Unknown symbol", source, symbol, null, null, null);
    }

    public static class LinkingProblemDetails {

        public Source source;
        public String symbol;
        public Integer index;
        public Object expected;
        public Object got;

        @Override
        public String toString() {
            return String.format("%s@%s index:%s expected: %s, got: %s", symbol, source, index, expected, got);
        }

    }

}
