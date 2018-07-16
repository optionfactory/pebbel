package net.optionfactory.pebbel.ast;

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
}
