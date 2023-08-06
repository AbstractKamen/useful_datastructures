package com.abstractkamen.datastructures.impl.trees.search.demos;

import com.abstractkamen.datastructures.impl.heaps.AdjustableBinaryHeap;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdjustableBinaryHeapDemo {

    public static void main(String[] args) {
        System.out.println("""
                                   Graph: 12 Vertices and 27 Edges
                                   A ---5---> B ---4---> E ---1---> A
                                   |           |                  ^
                                   |           |                  |
                                   10          |                  |
                                   |           |                  |
                                   v           |                  |
                                   C --6---> D -8---> H --7---> J -3---> K ---5---> L
                                   |                                          ^
                                   2                                          |
                                   |                                          |
                                   v                                          |
                                   F --4---> E --9---> I --2---> G --6---> K ---6---> L
                                   |
                                   4
                                   |
                                   v
                                   D
                                                              
                               """);
        final BiConsumer<AdjustableBinaryHeap<Vertex>, Vertex> push = AdjustableBinaryHeap::push;
        final BiConsumer<AdjustableBinaryHeap<Vertex>, Vertex> heapUse = (a, b) -> {
            if (a.containsKey(b)) {
                a.decreaseKey(b);
            } else {
                a.push(b);
            }
        };
        final BiConsumer<PriorityQueue<Vertex>, Vertex> queueOffer = PriorityQueue::offer;
        for (int i = 0; i < 12; i++) {
            final AdjustableBinaryHeap<Vertex> adjustableBinaryHeap = AdjustableBinaryHeap.createComparable();
            final PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>();
            final List<Vertex> dagVertices1 = getGraph();
            final List<Vertex> dagVertices2 = getGraph();
            final DijkstraAlgorithm heapAlgorithm = DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop, push, heapUse,
                                                                                    adjustableBinaryHeap, AdjustableBinaryHeap::isEmpty);
            final DijkstraAlgorithm queueAlgorithm = DijkstraAlgorithm.createDefault(PriorityQueue::poll, queueOffer, queueOffer,
                                                                                     priorityQueue, PriorityQueue::isEmpty);
            heapAlgorithm.computePath(dagVertices1.get(i));
            queueAlgorithm.computePath(dagVertices2.get(i));

            for (int j = 0; j < 12; j++) {
                final List<Vertex> h = heapAlgorithm.getShortestPathTo(dagVertices1.get(j));
                final List<Vertex> q = queueAlgorithm.getShortestPathTo(dagVertices2.get(j));
                if (!q.equals(h)) throw new AssertionError();
                System.out.println(h);
                System.out.println(q);
            }
        }
    }

    public static List<Vertex> getGraph() {
        // Create vertices
        Vertex v1 = new Vertex("A");
        Vertex v2 = new Vertex("B");
        Vertex v3 = new Vertex("C");
        Vertex v4 = new Vertex("D");
        Vertex v5 = new Vertex("E");
        Vertex v6 = new Vertex("F");
        Vertex v7 = new Vertex("G");
        Vertex v8 = new Vertex("H");
        Vertex v9 = new Vertex("I");
        Vertex v10 = new Vertex("J");
        Vertex v11 = new Vertex("K");
        Vertex v12 = new Vertex("L");

        // Add vertices to the graph
        final List<Vertex> graph = new ArrayList<>();
        graph.add(v1);
        graph.add(v2);
        graph.add(v3);
        graph.add(v4);
        graph.add(v5);
        graph.add(v6);
        graph.add(v7);
        graph.add(v8);
        graph.add(v9);
        graph.add(v10);
        graph.add(v11);
        graph.add(v12);

        // Create edges with different weights targeting the same vertex
        new Edge(5.0, v1, v2);
        new Edge(3.0, v1, v2);
        new Edge(10.0, v1, v3);
        new Edge(7.0, v1, v4);
        new Edge(4.0, v2, v5);
        new Edge(2.0, v2, v4);
        new Edge(6.0, v3, v5);
        new Edge(8.0, v4, v3);
        new Edge(1.0, v5, v1);
        new Edge(9.0, v5, v6);
        new Edge(3.0, v6, v7);
        new Edge(4.0, v6, v8);
        new Edge(2.0, v7, v3);
        new Edge(6.0, v8, v4);
        new Edge(5.0, v8, v1);
        new Edge(7.0, v4, v9);
        new Edge(5.0, v9, v5);
        new Edge(8.0, v10, v3);
        new Edge(6.0, v5, v10);
        new Edge(4.0, v6, v10);
        new Edge(2.0, v7, v10);
        new Edge(3.0, v8, v10);
        new Edge(1.0, v9, v10);
        new Edge(3.0, v10, v11);
        new Edge(5.0, v11, v12);
        new Edge(2.0, v12, v4);
        new Edge(4.0, v12, v7);
        new Edge(6.0, v12, v10);
        return graph;
    }
}

class DijkstraAlgorithm {
    private final Function<Object, Object> heapPoll;
    private final Predicate<Object> isEmpty;
    private final BiConsumer<Object, Object> heapPush;
    private final BiConsumer<Object, Object> heapUse;
    private final Object heap;

    public DijkstraAlgorithm(Function<Object, Object> heapPoll, BiConsumer<Object, Object> heapPush,
                             BiConsumer<Object, Object> heapUse, Object heap, Predicate<Object> isEmpty) {
        this.heapPoll = heapPoll;
        this.heapPush = heapPush;
        this.heapUse = heapUse;
        this.heap = heap;
        this.isEmpty = isEmpty;
    }

    public static <H, V> DijkstraAlgorithm createDefault(Function<H, Object> heapPoll, BiConsumer<H, V> heapPush,
                                                         BiConsumer<H, V> heapUse, H heap, Predicate<H> isEmpty) {
        return new DijkstraAlgorithm(convert(heapPoll), convert(heapPush), convert(heapUse), heap, convert(isEmpty));
    }

    @SuppressWarnings("unchecked")
    private static <H, V> BiConsumer<Object, Object> convert(BiConsumer<H, V> c) {
        return (a, b) -> c.accept((H) a, (V) b);
    }

    @SuppressWarnings("unchecked")
    private static <H> Function<Object, Object> convert(Function<H, Object> c) {
        return a -> c.apply((H) a);
    }

    @SuppressWarnings("unchecked")
    private static <H> Predicate<Object> convert(Predicate<H> c) {
        return a -> c.test((H) a);
    }

    public void computePath(Vertex source) {
        // heap
        heapPush.accept(heap, source);
        source.setDistance(0);
        while (!isEmpty.test(heap)) {
            final Vertex actualVertex = (Vertex) heapPoll.apply(heap);
            for (Edge edge : actualVertex.getAdjacencyList()) {
                final Vertex v = edge.getTargetVertex();
                final double d = actualVertex.getDistance() + edge.getWeight();
                if (d < v.getDistance()) {
                    v.setDistance(d);
                    v.setPredecessor(actualVertex);
                    heapUse.accept(heap, v);
                }
            }
        }
    }

    public List<Vertex> getShortestPathTo(Vertex targetVertex) {
        final Deque<Vertex> path = new ArrayDeque<>();
        for (Vertex vertex = targetVertex; vertex != null; vertex = vertex.getPredecessor()) {
            path.push(vertex);
        }
        return new ArrayList<>(path);
    }
}

class Edge {

    private final double weight;
    private final Vertex targetVertex;

    public Edge(double weight, Vertex sourceVertex, Vertex targetVertex) {
        this.weight = weight;
        this.targetVertex = targetVertex;
        sourceVertex.addNeighbor(this);
    }

    public double getWeight() {
        return weight;
    }

    public Vertex getTargetVertex() {
        return targetVertex;
    }
}

class Vertex implements Comparable<Vertex> {

    private final String name;
    private final List<Edge> adjacencyList;
    private double distance;
    private Vertex predecessor;

    public Vertex(String name) {
        this.name = name;
        this.adjacencyList = new ArrayList<>();
        this.distance = Double.MAX_VALUE;
    }

    public List<Edge> getAdjacencyList() {
        return adjacencyList;
    }

    public void addNeighbor(Edge edge) {
        this.adjacencyList.add(edge);
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Vertex getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Vertex predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    public int compareTo(Vertex otherVertex) {
        return Double.compare(this.distance, otherVertex.getDistance());
    }

    @Override
    public String toString() {
        return name + " - " + distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        return Objects.equals(name, vertex.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}