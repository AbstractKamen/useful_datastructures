package com.abstractkamen.datastructures.api.stacks;

/**
 * This stack is considered 'immutable' because no operations are performed in-place at any given element. All 'modifying' operations
 * like push and pop return new stacks.
 * @param <T> type of elements
 */
public interface ImmutableStack<T> extends Iterable<T> {
    /**
     * Creates a new stack out of the element and appends the previous stack to it.
     * @param element to create a new stack from
     * @return new stack
     */
    ImmutableStack<T> push(T element);

    /**
     * Returns the tail of the current stack.
     * @return tail of current stack stack
     */
    ImmutableStack<T> pop();

    /**
     * See the current element.
     * @return current element
     */
    T peek();

    /**
     * The depth of current stack.
     * @return depth of current stack
     */
    int depth();

    /**
     * True if stack is empty.
     * @return true if stack is empty
     */
    boolean isEmpty();
}
