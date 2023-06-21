package com.abstractkamen.datastructures.api.stacks;

public interface ImmutableStack<T> extends Iterable<T> {
    ImmutableStack<T> push(T element);

    ImmutableStack<T> pop();

    T peek();

    int depth();

    boolean isEmpty();
}
