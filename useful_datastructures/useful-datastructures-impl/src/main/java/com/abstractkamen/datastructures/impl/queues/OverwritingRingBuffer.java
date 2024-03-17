package com.abstractkamen.datastructures.impl.queues;

import com.abstractkamen.datastructures.api.queues.RingBuffer;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringJoiner;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a ring buffer that overwrites the oldest element when full.
 *
 * @param <T> the type of elements stored in the buffer.
 */
public class OverwritingRingBuffer<T> implements RingBuffer<T> {
    private final Object[] items;
    private int start;
    private int count;
    private final int mask;

    /**
     * Constructs a new {@code OverwritingRingBuffer} with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the ring buffer (must be greater than zero).
     * @throws IllegalArgumentException if the initial capacity is lesser than one.
     */
    public OverwritingRingBuffer(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity cannot be lesser than one");
        }
        if (Integer.bitCount(initialCapacity) != 1) {
            throw new IllegalArgumentException("Initial capacity must be a power of two");
        }
        this.items = new Object[initialCapacity];
        this.mask = items.length - 1;
    }

    @Override
    public boolean isEmpty() {
        return count <= 0;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public int capacity() {
        return items.length;
    }

    /**
     * Enqueues the specified item into the buffer, overwriting the oldest item if the buffer is full.
     *
     * @param item the item to be enqueued.
     * @return {@code true} always.
     */
    @Override
    public boolean enqueue(T item) {
        items[(start + count) & mask] = item;
        if (count == capacity()) {
            start = (start + 1) % capacity();
        } else {
            count++;
        }
        return true;
    }
    /**
     * Dequeues and returns the oldest item from the buffer.
     *
     * @return the oldest item in the buffer.
     * @throws NoSuchElementException if the buffer is empty.
     */
    @Override
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (count == 0) {
            throw new NoSuchElementException("Buffer is empty");
        }
        final T res = (T) items[start];
        start = (start + 1) & mask;
        count--;
        return res;
    }

    /**
     * Returns a sequential {@link java.util.stream.Stream} of items in the buffer.
     *
     * @return a sequential stream of items in the buffer.
     */
    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a sequential {@link java.util.Spliterator} of items in the buffer.
     *
     * @return a sequential spliterator of items in the buffer.
     */
    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i + 1 <= count;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove is not supported");
            }

            @Override
            public T next() {
                if (hasNext()) {
                    final T next = (T) items[(i + start) & mask];
                    i++;
                    return next;
                }
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (T item : this) {
            sj.add(String.valueOf(item));
        }
        return sj.toString();
    }
}
