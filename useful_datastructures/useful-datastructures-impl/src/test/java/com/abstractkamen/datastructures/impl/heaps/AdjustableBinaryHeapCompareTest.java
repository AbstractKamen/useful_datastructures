package com.abstractkamen.datastructures.impl.heaps;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class AdjustableBinaryHeapCompareTest {
    private static final double[] RANDOM_WEIGHTS = new Random().doubles(100, 0, 1000).toArray();
    private final String name;
    private final int numVertices;
    private final int numEdges;
    private final DijkstraAlgorithm dijkstraAlgorithm;
    private final DijkstraAlgorithm dijkstraAlgorithmCompare;
    private double expected;

    @Parameterized.Parameters(name = "{index}: name={0}, numVertices={1}, numEdges={2}, dijkstraAlgorithm={3}, " +
        "dijkstraAlgorithmCompare={4}")
    public static Iterable<Object[]> data() {
        final BiConsumer<AdjustableBinaryHeap<Vertex>, Vertex> push = AdjustableBinaryHeap::push;
        final BiConsumer<AdjustableBinaryHeap<Vertex>, Vertex> heapUse = (a, b) -> {
            a.decreaseKey(b, b);
        };
        final BiConsumer<PriorityQueue<Vertex>, Vertex> queueOffer = (q, v) -> {
            q.remove(v);
            q.offer(v);
        };
        final int v = 200_000;
        return Arrays.asList(new Object[][]
                                 {
                                     {"AdjustableBinaryHeap", v, 10, DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop,
                                                                                                     push,
                                                                                                     heapUse,
                                                                                                     AdjustableBinaryHeap.createComparable(v),
                                                                                                     AdjustableBinaryHeap::isEmpty),
                                         DijkstraAlgorithm.createDefault(PriorityQueue::poll,
                                                                         queueOffer,
                                                                         queueOffer,
                                                                         new PriorityQueue<>(v, Vertex::compareTo),
                                                                         PriorityQueue::isEmpty)},
                                     // ****************************************************************
                                     {"PriorityQueue", v, 10, DijkstraAlgorithm.createDefault(PriorityQueue::poll,
                                                                                              queueOffer,
                                                                                              queueOffer,
                                                                                              new PriorityQueue<>(v, Vertex::compareTo),
                                                                                              PriorityQueue::isEmpty),
                                         DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop,
                                                                         push,
                                                                         heapUse,
                                                                         AdjustableBinaryHeap.createComparable(v),
                                                                         AdjustableBinaryHeap::isEmpty)},
                                 }
        );
    }

    public AdjustableBinaryHeapCompareTest(String name, int numVertices, int numEdges, DijkstraAlgorithm dijkstraAlgorithm,
                                           DijkstraAlgorithm dijkstraAlgorithmCompare) {
        this.name = name;
        this.numVertices = numVertices;
        this.numEdges = numEdges;
        this.dijkstraAlgorithm = dijkstraAlgorithm;
        this.dijkstraAlgorithmCompare = dijkstraAlgorithmCompare;
    }

    @Before
    public void init() {
        final List<Vertex> graph = generateDAG();
        dijkstraAlgorithmCompare.computePath(graph.get(0));
        expected = dijkstraAlgorithmCompare.getShortestPathDistanceTo(graph.get(numVertices - 1));
    }

    @Test
    public void testDijkstra() {
        // arrange
        final List<Vertex> graph = generateDAG();
        // act
        final long s = System.currentTimeMillis();
        dijkstraAlgorithm.computePath(graph.get(0));
        final double actual = dijkstraAlgorithmCompare.getShortestPathDistanceTo(graph.get(numVertices - 1));
        final long e = System.currentTimeMillis() - s;
        System.out.printf("%s:%.3f%n", name, e / 1000f);
        // assert
        assertEquals(expected, actual, 0);
        System.out.printf("expected:%f    actual:%f%n", expected, actual);
    }

    public List<Vertex> generateDAG() {
        List<Vertex> vertices = new ArrayList<>();

        // Create vertices
        for (int i = 0; i < numVertices; i++) {
            Vertex vertex = new Vertex("V" + i);
            vertices.add(vertex);
        }

        for (int i = 0; i < numVertices - 1; i++) {
            Vertex startVertex = vertices.get(i);
            for (int j = i + 1; j - i - 1 < numEdges && j < numVertices; j++) {
                final Vertex nextVertex = vertices.get(j);
                link(RANDOM_WEIGHTS[(j - 1) % RANDOM_WEIGHTS.length], startVertex, nextVertex);
            }
        }

        return vertices;
    }

    private static void link(double weight, Vertex startVertex, Vertex targetVertex) {
        // Check if there is already an edge from startVertex to targetVertex
        boolean edgeExists = startVertex.getAdjacencyList().stream()
            .anyMatch(edge -> edge.getTargetVertex().equals(targetVertex));

        if (!edgeExists && !startVertex.equals(targetVertex)) {
            Edge edge = new Edge(weight, startVertex, targetVertex);
            startVertex.addNeighbor(edge);
        }
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

    public double getShortestPathDistanceTo(Vertex targetVertex) {
        double sum = 0;
        for (Vertex vertex = targetVertex; vertex != null; vertex = vertex.getPredecessor()) {
            sum += vertex.getDistance();
        }
        return sum;
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

