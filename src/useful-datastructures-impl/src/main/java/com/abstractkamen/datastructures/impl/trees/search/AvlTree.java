package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.api.trees.search.BinarySearchTree;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.*;

import static java.util.Spliterator.*;

public class AvlTree<T> implements BinarySearchTree<T> {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @SuppressWarnings("rawtypes")
    private static final UnaryOperator LEFT = (UnaryOperator<Node>) c -> c.left;
    @SuppressWarnings("rawtypes")
    private static final UnaryOperator RIGHT = (UnaryOperator<Node>) c -> c.right;
    private final Comparator<T> comparator;
    private Node<T> root;
    private int size;

    public AvlTree(Comparator<T> comparator) {
        this.comparator = comparator;
    }

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
    public void add(T value) {
        this.root = insertNode(this.root, value);
        size++;
    }

    @Override
    public void remove(T value) {
        final boolean[] isPresent = new boolean[1];
        this.root = removeNode(this.root, value, isPresent);
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
        return findNode(this.root, item) != null;
    }

    @Override
    public int containsCount(T item) {
        final Node<T> n = findNode(this.root, item);
        if (n != null) {
            return n.count;
        } else {
            return 0;
        }
    }

    @Override
    public int height() {
        return isEmpty() ? -1 : root.height;
    }

    @Override
    public T min() {
        final Node<T> cur = walkOneDir(this.root, left());
        if (cur == null) throw new NoSuchElementException();
        return cur.value;
    }

    @Override
    public T max() {
        final Node<T> cur = walkOneDir(this.root, right());
        if (cur == null) throw new NoSuchElementException();
        return cur.value;
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
                        node.value = null;
                        node.parent = node.left = node.right = null;
                    }
                }
            }.doRemove(this.root);
            this.root = null;
            this.size = 0;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final Iterator<Node<T>> nodeIterator = new NodeIterator(
                walkOneDir(AvlTree.this.root, left()),
                n -> successor(n, left(), right())
            );

            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return nodeIterator.next().value;
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
    public Iterator<T> descendingIterator() {

        return new Iterator<>() {
            private final Iterator<Node<T>> nodeIterator = new NodeIterator(
                walkOneDir(AvlTree.this.root, right()),
                n -> successor(n, right(), left())
            );

            @Override
            public boolean hasNext() {
                return nodeIterator.hasNext();
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return nodeIterator.next().value;
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

    @Override
    public String prettyString() {
        if (root != null) {
            final StringBuilder sb = new StringBuilder();
            visitAllNodes(root, ">>>", sb);
            return sb.toString();
        }
        return "empty tree";
    }

    private void visitAllNodes(Node<T> node, String prefix, StringBuilder visitor) {
        final String leftPointer = "├─► ";
        final String rightPointer = "└─► ";
        final String pointerConnection = "│";
        final String valueCount = "|_count";
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
        visitor.append(prefix).append(s).append(node.value);
        if (node.count > 1) {
            visitor.append(valueCount).append(op).append(node.count).append(cl);
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

    private Node<T> removeNode(Node<T> current, T value, boolean[] isPresent) {
        if (current == null) return null;
        final int compare = comparator.compare(current.value, value);
        if (compare > 0) {
            // when value is smaller than current, go left
            current.setLeft(removeNode(current.left, value, isPresent));
        } else if (compare < 0) {
            // when value is greater than current, go right
            current.setRight(removeNode(current.right, value, isPresent));
        } else {
            // when value is equal set isPresent to true, so we know to decrement size
            isPresent[0] = true;
            current = removeCurrentNode(current, isPresent);
        }
        current = balanceTree(current, value);
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
                // copy over value from successor
                current.setValue(successor.value);
                // remove successor
                current.setRight(removeNode(current.right, successor.value, isPresent));
                // when current has only one child reassign
            } else if (current.right == null) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return current;
    }

    private Node<T> findNode(Node<T> root, T value) {
        if (root == null) return null;
        if (comparator.compare(root.value, value) == 0) return root;
        if (comparator.compare(root.value, value) > 0) return findNode(root.left, value);
        return findNode(root.right, value);
    }

    private Node<T> insertNode(Node<T> current, T value) {
        if (current == null) {
            return new Node<>(value);
        }
        final int compare = comparator.compare(current.value, value);
        // value is equal increment current
        if (compare == 0) {
            current.increment();
            return current;
            // value is lesser go left
        } else if (compare > 0) {
            current.setLeft(insertNode(current.left, value));
            // value is greater go right
        } else {
            current.setRight(insertNode(current.right, value));
        }
        current = balanceTree(current, value);
        // Update the height of the current node
        current.setHeight(1 + Math.max(height(current.left), height(current.right)));
        return current;
    }

    private Node<T> balanceTree(Node<T> current, T value) {
        if (current == null) return null;
        final int balance = current.getBalance();
        // when tree is right-skewed
        if (balance > 0 && current.left != null) {
            // Left-Left
            if (comparator.compare(value, current.left.value) < 0) {
                return rotate(current, this::rightRotate);
                // Left-Right
            } else if (comparator.compare(value, current.left.value) > 0) {
                current.setLeft(rotate(current.left, this::leftRotate));
                return rotate(current, this::rightRotate);
            }
            // when tree is left-skewed
        } else if (balance < 0 && current.right != null) {
            // Right-Right
            if (comparator.compare(value, current.right.value) < 0) {
                return rotate(current, this::leftRotate);
                // Right-Left
            } else if (comparator.compare(value, current.right.value) > 0) {
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
        if (oldRoot == null) return null;
        final Node<T> newRoot = oldRoot.right;
        if (newRoot == null) return oldRoot;
        final Node<T> oldLeft = newRoot.left;
        oldRoot.setRight(oldLeft);
        newRoot.setLeft(oldRoot);
        return newRoot;
    }

    private Node<T> rightRotate(Node<T> oldRoot) {
        if (oldRoot == null) return null;
        final Node<T> newRoot = oldRoot.left;
        if (newRoot == null) return oldRoot;
        final Node<T> oldRight = newRoot.right;
        oldRoot.setLeft(oldRight);
        newRoot.setRight(oldRoot);
        return newRoot;
    }

    private class NodeIterator implements Iterator<Node<T>> {

        private Node<T> prev;
        private Node<T> next;
        private final UnaryOperator<Node<T>> nextFunc;
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
                prev.count--;
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
                    updateRootReference(balanceTree(parent, parent.value));
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
                parent.setLeft(balanceTree(afterUpdate, beforeUpdate.value));
            } else {
                parent.setRight(balanceTree(afterUpdate, beforeUpdate.value));
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

    private static class Node<T> {
        private Node<T> parent;
        private Node<T> left;
        private Node<T> right;
        private int count = 1;
        private int height;
        private T value;

        Node(T value) {
            this.value = value;
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

        void setValue(T value) {
            this.value = value;
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

    private static <T> Node<T> successor(Node<T> node, UnaryOperator<Node<T>> firstDirection, UnaryOperator<Node<T>> lastDirection) {
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
}