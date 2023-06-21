package com.abstractkamen.datastructures.impl.stacks;

import com.abstractkamen.datastructures.api.stacks.ImmutableStack;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ImmutableStackImpl<T> implements ImmutableStack<T> {

    @Override
    public ImmutableStack<T> push(T element) {
        return new NotEmptyStack<>(element, this);
    }

    @Override
    public ImmutableStack<T> pop() {
        throw new NoSuchElementException();
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public int depth() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new NoSuchElementException();
            }
        };
    }

    private static class NotEmptyStack<T> implements ImmutableStack<T> {
        private final T val;
        private final ImmutableStack<T> tail;

        NotEmptyStack(T val, ImmutableStack<T> tail) {
            this.val = val;
            this.tail = tail;
        }

        @Override
        public ImmutableStack<T> push(T element) {
            return new NotEmptyStack<>(element, this);
        }

        @Override
        public ImmutableStack<T> pop() {
            return tail;
        }

        @Override
        public T peek() {
            return val;
        }

        @Override
        public int depth() {
            return 1 + tail.depth();
        }

        @Override
        public boolean isEmpty() {
            return depth() > 0;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {
                private ImmutableStack<T> itTail = tail;
                private boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext || !itTail.isEmpty();
                }

                @Override
                public T next() {
                    if (hasNext) {
                        hasNext = false;
                        return val;
                    } else if (!itTail.isEmpty()) {
                        final T nextVal = itTail.peek();
                        itTail = itTail.pop();
                        return nextVal;
                    }
                    throw new NoSuchElementException();
                }
            };
        }
    }
}
