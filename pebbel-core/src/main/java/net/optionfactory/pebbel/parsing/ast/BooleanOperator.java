package net.optionfactory.pebbel.parsing.ast;

/**
 * A boolean operator
 */
public enum BooleanOperator {

    AND, OR;

    public boolean shouldShortCircuit(boolean lhs) {
        if (this != AND && this != OR) {
            return false;
        }
        return this == BooleanOperator.OR ? lhs : !lhs;
    }

    public boolean shortCircuitsOn() {
        return this == BooleanOperator.OR;
    }
}
