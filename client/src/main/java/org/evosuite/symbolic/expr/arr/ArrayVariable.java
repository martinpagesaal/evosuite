package org.evosuite.symbolic.expr.arr;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.str.StringValue;

import java.util.Set;

public final class ArrayVariable<T> extends AbstractExpression<T[]>
        implements Variable<T[]>{

    private final String name;


    public ArrayVariable(String name, T[] concreteValue) {
        super(concreteValue, 1, true);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Variable<?>> getVariables() {
        return null;
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return null;
    }

    @Override
    public T[] getMinValue() {
        return null;
    }

    @Override
    public T[] getMaxValue() {
        return null;
    }
}
