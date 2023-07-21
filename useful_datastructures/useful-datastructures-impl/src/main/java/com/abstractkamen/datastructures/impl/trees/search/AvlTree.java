package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.BinarySearchTree;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.SORTED;

/**
 * An AVL implementation of the BinarySearchTree interface. Currently, does not support concurrent modification.
 *
 * @param <T> type of elements
 */
public class AvlTree<T> implements BinarySearchTree<T> {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @SuppressWarnings("rawtypes")
    private static final UnaryOperator LEFT = (UnaryOperator<Node>) c -> c.left;
    @SuppressWarnings("rawtypes")
    private static final UnaryOperator RIGHT = (UnaryOperator<Node>) c -> c.right;
    private final Comparator<T> comparator;
    private Node<T> root;
    private int size;

    /**
     * Create an {@code AvlTree<T>} with a custom comparator
     * @param comparator custom comparator
     */
    public AvlTree(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * Create an {@code AvlTree<T>} with natural order comparator. {@code T} is expected to be {@code instanceof Comparable<T>}
     */
    @SuppressWarnings("unchecked")
    public AvlTree() {
        this(Comparator.comparing(t -> ((Comparable<Object>) t)));
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void add(T item) {
        failFastCheckComparable(item);
        this.root = insertNode(this.root, item);
        size++;
    }

    @Override
    public void remove(T item) {
        failFastCheckComparable(item);
        final boolean[] isPresent = new boolean[1];
        this.root = removeNode(this.root, item, isPresent);
        if (root != null) {
            root.setParent(null);
            root.setHeight(1 + Math.max(height(root.left), height(root.right)));
        }
        if (isPresent[0]) {
            size--;
        }
    }

    @Override
    public boolean contains(T item) {
        failFastCheckComparable(item);
        return findNode(null, this.root, item)[1] != null;
    }

    @Override
    public int containsCount(T item) {
        failFastCheckComparable(item);
        final Node<T>[] n = findNode(null, this.root, item);
        if (n[1] != null) {
            return n[1].count;
        } else {
            return 0;
        }
    }

    @Override
    public int height() {
        return isEmpty() ? -1 : root.height;
    }

    @Override
    public T greater(T item) {
        return findItem(item, n -> n != null && grThan(n.getItem(), item), AvlTree::successor, AvlTree::predecessor);
    }

    @Override
    public T lesser(T item) {
        return findItem(item, n -> n != null && lsThan(n.getItem(), item), AvlTree::predecessor, AvlTree::successor);

    }

    @Override
    public T min() {
        final Node<T> cur = walkOneDir(this.root, left());
        if (cur == null)
            throw new NoSuchElementException();
        return cur.item;
    }

    @Override
    public T max() {
        final Node<T> cur = walkOneDir(this.root, right());
        if (cur == null)
            throw new NoSuchElementException();
        return cur.item;
    }

    @Override
    public String prettyString() {
        if (root != null) {
            final StringBuilder sb = new StringBuilder();
            visitAllNodes(root, ">>>", sb);
            return sb.toString();
        }
        return "empty tree";
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), size(), SORTED | IMMUTABLE), false);
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            // dereference children recursively to prevent memory leaks
            new Object() {

                void doRemove(Node<T> node) {
                    if (node != null) {
                        doRemove(node.left);
                        doRemove(node.right);
                        node.item = null;
                        node.parent = node.left = node.right = null;
                    }
                }
            }.doRemove(this.root);
            this.root = null;
            this.size = 0;
        }
    }

    @Override
    public Iterator<T> descendingIterator() {

        return new Iterator<>() {

            private final Iterator<Node<T>> nodeIterator = new NodeIterator(walkOneDir(AvlTree.this.root, right()), AvlTree::predecessor);

            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return nodeIterator.next().item;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                nodeIterator.remove();
            }
        };
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private final Iterator<Node<T>> nodeIterator = new NodeIterator(walkOneDir(AvlTree.this.root, left()), AvlTree::successor);

            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return nodeIterator.next().item;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                nodeIterator.remove();
            }
        };
    }

    @Override
    public String toString() {
        return stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
    }

    private void visitAllNodes(Node<T> node, String prefix, StringBuilder visitor) {
        final String leftPointer = "├─► ";
        final String rightPointer = "└─► ";
        final String pointerConnection = "│";
        final String itemCount = "|_count";
        final String height = "|_height";
        final char op = '[';
        final char cl = ']';
        // depth-first search
        String s = "";
        if (node.parent != null) {
            if (node.parent.left == node) {
                s += leftPointer;
            } else if (node.parent.right == node) {
                s += rightPointer;
            }
        }
        // visit current
        visitor.append(prefix).append(s).append(node.item);
        if (node.count > 1) {
            visitor.append(itemCount).append(op).append(node.count).append(cl);
        }
        visitor.append(height).append(op).append(node.height).append(cl);

        visitor.append(LINE_SEPARATOR);
        final String nextPrefix;
        if (node.parent != null && node.parent.left == node && node.parent.right != null) {
            nextPrefix = prefix + pointerConnection + " ".repeat(s.length());
        } else {
            nextPrefix = prefix + " ".repeat(s.length());
        }
        // visit left subtree
        if (node.left != null) {
            visitAllNodes(node.left, nextPrefix, visitor);
        }
        // visit right subtree
        if (node.right != null) {
            visitAllNodes(node.right, nextPrefix, visitor);
        }
    }

    private Node<T> removeNode(Node<T> current, T item, boolean[] isPresent) {
        if (current == null)
            return null;
        if (lsThan(item, current.item)) {
            // when item is smaller than current, go left
            current.setLeft(removeNode(current.left, item, isPresent));
        } else if (grThan(item, current.item)) {
            // when item is greater than current, go right
            current.setRight(removeNode(current.right, item, isPresent));
        } else {
            // when item is equal set isPresent to true, so we know to decrement size
            isPresent[0] = true;
            current = removeCurrentNode(current, isPresent);
        }
        current = balanceTree(current, item);
        // Update the height of the current node
        if (current != null) {
            current.setHeight(1 + Math.max(height(current.left), height(current.right)));
        }
        return current;
    }

    private Node<T> removeCurrentNode(Node<T> current, boolean[] isPresent) {
        // when current node has count > 1 decrement() without structural changes
        if (current.count > 1) {
            current.decrement();
            return current;
        } else {
            // we have to remove current and it has children
            if (current.right != null && current.left != null) {
                // find inOrder successor
                final Node<T> successor = walkOneDir(current.right, left());
                // copy over item from successor
                current.setItem(successor.item);
                // remove successor
                current.setRight(removeNode(current.right, successor.item, isPresent));
                // when current has only one child reassign
            } else if (current.right == null) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return current;
    }

    private T findItem(T item, Predicate<Node<T>> grThanOrLsThan, UnaryOperator<Node<T>> down, UnaryOperator<Node<T>> up) {
        failFastCheckComparable(item);
        final Node<T>[] found = findNode(null, root, item);
        final Node<T> nodeWithItem = found[1];
        if (nodeWithItem != null) {
            return Optional.ofNullable(down.apply(nodeWithItem))
                .map(Node::getItem)
                .orElse(null);
        } else {
            return Optional.ofNullable(found[0]).map(n -> {
                    if (grThanOrLsThan.test(n)) {
                        do {
                            n = up.apply(n);
                            if (!grThanOrLsThan.test(n)) {
                                return n;
                            }
                        } while (n != null);
                        return null;
                    } else {
                        return n;
                    }
                })
                .map(down)
                .map(Node::getItem)
                .orElse(null);
        }
    }

    @SuppressWarnings("unchecked")
    private Node<T>[] findNode(Node<T> parent, Node<T> root, T item) {
        if (root == null)
            return new Node[]{parent, null};
        if (eq(root.item, item))
            return new Node[]{parent, root};
        if (grThan(root.item, item))
            return findNode(root, root.left, item);
        return findNode(root, root.right, item);
    }

    private Node<T> insertNode(Node<T> current, T item) {
        if (current == null) {
            return new Node<>(item);
        }
        // item is equal increment current
        if (eq(item, current.item)) {
            current.increment();
            return current;
            // item is lesser go left
        } else if (lsThan(item, current.item)) {
            current.setLeft(insertNode(current.left, item));
            // item is greater go right
        } else {
            current.setRight(insertNode(current.right, item));
        }
        current = balanceTree(current, item);
        // Update the height of the current node
        current.setHeight(1 + Math.max(height(current.left), height(current.right)));
        return current;
    }

    private Node<T> balanceTree(Node<T> current, T item) {
        if (current == null)
            return null;
        final int balance = current.getBalance();
        // when tree is right-skewed
        if (balance > 0 && current.left != null) {
            // Left-Left
            if (lsThan(item, current.left.item)) {
                return rotate(current, this::rightRotate);
                // Left-Right
            } else if (grThan(item, current.left.item)) {
                current.setLeft(rotate(current.left, this::leftRotate));
                return rotate(current, this::rightRotate);
            }
            // when tree is left-skewed
        } else if (balance < 0 && current.right != null) {
            // Right-Right
            if (lsThan(item, current.right.item)) {
                return rotate(current, this::leftRotate);
                // Right-Left
            } else if (grThan(item, current.right.item)) {
                current.setRight(rotate(current.right, this::rightRotate));
                return rotate(current, this::leftRotate);
            }
        }
        return current;
    }

    private Node<T> rotate(Node<T> oldRoot, UnaryOperator<Node<T>> rotationDirection) {
        final Node<T> newRoot = rotationDirection.apply(oldRoot);
        newRoot.setHeight(Math.max(height(newRoot.left), height(newRoot.right)) + 1);
        oldRoot.setHeight(Math.max(height(oldRoot.left), height(oldRoot.right)) + 1);
        return newRoot;
    }

    private int height(Node<T> node) {
        return Optional.ofNullable(node).map(n -> n.height).orElse(-1);
    }

    private Node<T> leftRotate(Node<T> oldRoot) {
        if (oldRoot == null)
            return null;
        final Node<T> newRoot = oldRoot.right;
        if (newRoot == null)
            return oldRoot;
        final Node<T> oldLeft = newRoot.left;
        oldRoot.setRight(oldLeft);
        newRoot.setLeft(oldRoot);
        return newRoot;
    }

    private Node<T> rightRotate(Node<T> oldRoot) {
        if (oldRoot == null)
            return null;
        final Node<T> newRoot = oldRoot.left;
        if (newRoot == null)
            return oldRoot;
        final Node<T> oldRight = newRoot.right;
        oldRoot.setLeft(oldRight);
        newRoot.setRight(oldRoot);
        return newRoot;
    }

    private boolean eq(T a, T b) {
        return comparator.compare(a, b) == 0;
    }

    private boolean grThan(T a, T b) {
        return comparator.compare(a, b) > 0;
    }

    private boolean lsThan(T a, T b) {
        return comparator.compare(a, b) < 0;
    }

    private static <T> Node<T> predecessor(Node<T> n) {
        return ancestry(n, right(), left());
    }

    private static <T> Node<T> successor(Node<T> n) {
        return ancestry(n, left(), right());
    }

    private static <T> Node<T> ancestry(Node<T> node, UnaryOperator<Node<T>> firstDirection, UnaryOperator<Node<T>> lastDirection) {
        if (node == null)
            return null;
        else if (lastDirection.apply(node) != null) {
            return walkOneDir(lastDirection.apply(node), firstDirection);
        } else {
            Node<T> p = node.parent;
            Node<T> ch = node;
            while (p != null && ch != firstDirection.apply(p)) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> UnaryOperator<Node<T>> left() {
        return LEFT;
    }

    @SuppressWarnings("unchecked")
    private static <T> UnaryOperator<Node<T>> right() {
        return RIGHT;
    }

    private static <T> Node<T> walkOneDir(Node<T> cur, UnaryOperator<Node<T>> nextOp) {
        while (cur != null) {
            final Node<T> next = nextOp.apply(cur);
            if (next == null) {
                return cur;
            } else {
                cur = next;
            }
        }
        return null;
    }

    private void failFastCheckComparable(T item) {
        comparator.compare(item, item);
    }

    private static class Node<T> {

        private Node<T> parent;
        private Node<T> left;
        private Node<T> right;
        private int count = 1;
        private int height;
        private T item;

        Node(T item) {
            this.item = item;
        }

        int getBalance() {
            final int leftHeight = Optional.ofNullable(left).map(n -> n.height).orElse(0);
            final int rightHeight = Optional.ofNullable(right).map(n -> n.height).orElse(0);
            return leftHeight - rightHeight;
        }

        void increment() {
            count++;
        }

        void decrement() {
            count--;
        }

        void setParent(Node<T> parent) {
            if (this.parent != null) {
                // make sure to update old parent
                if (this.parent.left == this) {
                    this.parent.left = null;
                } else {
                    this.parent.right = null;
                }
            }
            this.parent = parent;
        }

        T getItem() {
            return item;
        }

        void setItem(T item) {
            this.item = item;
        }

        void setLeft(Node<T> left) {
            if (this.left != null) {
                this.left.setParent(null);
            }
            if (left != null) {
                left.setParent(this);
            }
            this.left = left;
        }

        void setHeight(int height) {
            this.height = height;
        }

        void setRight(Node<T> right) {
            if (this.right != null) {
                this.right.setParent(null);
            }
            if (right != null) {
                right.setParent(this);
            }
            this.right = right;
        }

    }

    private class NodeIterator implements Iterator<Node<T>> {

        private final UnaryOperator<Node<T>> nextFunc;
        private Node<T> prev;
        private Node<T> next;
        private int i;

        public NodeIterator(Node<T> first, UnaryOperator<Node<T>> nextFunc) {
            this.next = first;
            this.nextFunc = nextFunc;
            this.i = next != null ? next.count : 0;
        }

        @Override
        public boolean hasNext() {
            return next != null && i > 0;
        }

        @Override
        public Node<T> next() {
            if (hasNext()) {
                Node<T> result = next;
                i--;
                prev = next;
                if (i <= 0) {
                    next = nextFunc.apply(next);
                    i = next != null ? next.count : 0;
                }
                return result;
            }
            // shouldn't reach here ever
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (prev == null) {
                throw new NoSuchElementException();
            }
            if (prev.count > 1) {
                // decrement node count only without structural changes
                prev.decrement();
            } else {
                Node<T> parent = prev.parent;
                Node<T> removed = removeCurrentNode(prev, new boolean[]{true});
                if (parent == null) {
                    // just tree root reference no need to balance as removed is always balanced or null
                    updateRootReference(removed);
                } else {
                    final Queue<Node<T>> parents = getParents();
                    if (removed != prev) {
                        balance(parent, prev, removed);
                        parents.poll();// skip the first parent because we handled it above
                        while (!parents.isEmpty()) {
                            prev = parents.poll();// continue balancing all parents
                            balance(prev, parent, parent);
                            parent = prev;
                        }
                    }
                    // update tree root reference
                    updateRootReference(balanceTree(parent, parent.item));
                }
            }
            // decrement tree size
            AvlTree.this.size--;
            prev = null;
        }

        private Queue<Node<T>> getParents() {
            final Queue<Node<T>> parents = new ArrayDeque<>();
            Node<T> parent = prev.parent;
            while (parent != null) {
                parents.add(parent);
                parent = parent.parent;
            }
            return parents;
        }

        private void balance(Node<T> parent, Node<T> beforeUpdate, Node<T> afterUpdate) {
            if (parent.left == beforeUpdate) {
                parent.setLeft(balanceTree(afterUpdate, beforeUpdate.item));
            } else {
                parent.setRight(balanceTree(afterUpdate, beforeUpdate.item));
            }
            // Update the height of the parent node
            parent.setHeight(1 + Math.max(height(parent.left), height(parent.right)));

        }

        private void updateRootReference(Node<T> newRoot) {
            if (newRoot != null) {
                newRoot.parent = null;
                // Update the height of the current node
                newRoot.setHeight(1 + Math.max(height(newRoot.left), height(newRoot.right)));
            }
            AvlTree.this.root = newRoot;
        }
    }
}