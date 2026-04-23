package com.abstractkamen.datastructures.impl.utils;

import java.util.Objects;

public class Pair<A, B> {

    private final A a;
    private final B b;

    private Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A first() {
        return a;
    }

    public B second() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(a);
        result = 31 * result + Objects.hashCode(b);
        return result;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }
}