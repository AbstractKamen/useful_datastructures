package com.abstractkamen.datastructures.impl.heaps;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class AdjustableBinaryHeapCompareTest {
    private static final double[] RANDOM_WEIGHTS = new Random().doubles(1000, 0, 1000).toArray();
    private final String name;
    private final int numVertices;
    private final int numEdges;
    private final DijkstraAlgorithm dijkstraAlgorithm;
    private final DijkstraAlgorithm dijkstraAlgorithmCompare;
    private double expected;
    private final Object initialCapacity;

    @Parameterized.Parameters(name = "{index}: name={0}, numVertices={1}, numEdges={2}, dijkstraAlgorithm={3}, " +
        "dijkstraAlgorithmCompare={4}, initialCapacity={5}")
    public static Iterable<Object[]> data() {
        final BiConsumer<AdjustableBinaryHeap<Vertex>, Vertex> push = AdjustableBinaryHeap::push;
        final BiConsumer<AdjustableBinaryHeap<Vertex>, Vertex> decreaseKey = (a, b) -> a.decreaseKey(b, b);
        final BiConsumer<PriorityQueue<Vertex>, Vertex> removeOffer = (q, v) -> {
            q.remove(v);
            q.offer(v);
        };
        final int vertices = 200_000;
        final int edges = 15;
        return Arrays.asList(new Object[][]
                                 {// @formatter:off
               {"AdjustableBinaryHeap", vertices, edges,
                   DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop,
                                                   push,
                                                   decreaseKey,
                                                   AdjustableBinaryHeap.createComparable(),
                                                   AdjustableBinaryHeap::isEmpty),
                   DijkstraAlgorithm.createDefault(PriorityQueue::poll,
                                                   removeOffer,
                                                   removeOffer,
                                                   new PriorityQueue<>(Vertex::compareTo),
                                                   PriorityQueue::isEmpty),
                                                   "default"},
               // ****************************************************************
               {"PriorityQueue.remove().offer()", vertices, edges,
                   DijkstraAlgorithm.createDefault(PriorityQueue::poll,
                                                   removeOffer,
                                                   removeOffer,
                                                   new PriorityQueue<>(Vertex::compareTo),
                                                   PriorityQueue::isEmpty),
                   DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop,
                                                   push,
                                                   decreaseKey,
                                                   AdjustableBinaryHeap.createComparable(),
                                                   AdjustableBinaryHeap::isEmpty),
                                                   "default"},
               // ****************************************************************
               {"AdjustableBinaryHeap", vertices, edges,
                   DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop,
                                                   push,
                                                   decreaseKey,
                                                   AdjustableBinaryHeap.createComparable(vertices),
                                                   AdjustableBinaryHeap::isEmpty),
                   DijkstraAlgorithm.createDefault(PriorityQueue::poll,
                                                   removeOffer,
                                                   removeOffer,
                                                   new PriorityQueue<>(vertices, Vertex::compareTo),
                                                   PriorityQueue::isEmpty),
                                                   vertices},
               // ****************************************************************
               {"PriorityQueue.remove().offer()", vertices, edges,
                   DijkstraAlgorithm.createDefault(PriorityQueue::poll,
                                                   removeOffer,
                                                   removeOffer,
                                                   new PriorityQueue<>(vertices,Vertex::compareTo),
                                                   PriorityQueue::isEmpty),
                   DijkstraAlgorithm.createDefault(AdjustableBinaryHeap::pop,
                                                   push,
                                                   decreaseKey,
                                                   AdjustableBinaryHeap.createComparable(vertices),
                                                   AdjustableBinaryHeap::isEmpty),
                                                   vertices}
           }// @formatter:on
        );
    }

    public AdjustableBinaryHeapCompareTest(String name, int numVertices, int numEdges, DijkstraAlgorithm dijkstraAlgorithm,
                                           DijkstraAlgorithm dijkstraAlgorithmCompare, Object initialCapacity) {
        this.name = name;
        this.numVertices = numVertices;
        this.numEdges = numEdges;
        this.dijkstraAlgorithm = dijkstraAlgorithm;
        this.dijkstraAlgorithmCompare = dijkstraAlgorithmCompare;
        this.initialCapacity = initialCapacity;
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
        System.out.printf("%s:%.3f initialCapacity=%s%n", name, e / 1000f, initialCapacity);
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
    private final Function<Object, Object> poll;
    private final Predicate<Object> isEmpty;
    private final BiConsumer<Object, Object> push;
    private final BiConsumer<Object, Object> use;
    private final Object dataStructure;

    public DijkstraAlgorithm(Function<Object, Object> poll, BiConsumer<Object, Object> push,
                             BiConsumer<Object, Object> use, Object dataStructure, Predicate<Object> isEmpty) {
        this.poll = poll;
        this.push = push;
        this.use = use;
        this.dataStructure = dataStructure;
        this.isEmpty = isEmpty;
    }

    public static <H, V> DijkstraAlgorithm createDefault(Function<H, Object> poll, BiConsumer<H, V> push,
                                                         BiConsumer<H, V> use, H dataStructure, Predicate<H> isEmpty) {
        return new DijkstraAlgorithm(convert(poll), convert(push), convert(use), dataStructure, convert(isEmpty));
    }

    @SuppressWarnings("unchecked")
    private static <H, V> BiConsumer<Object, Object> convert(BiConsumer<H, V> c) {
        return (a, b) -> c.accept((H) a, (V) b);
    }

    @SuppressWarnings("unchecked")
    private static <H> Function<Object, Object> convert(Function<H, Object> op) {
        return a -> op.apply((H) a);
    }

    @SuppressWarnings("unchecked")
    private static <H> Predicate<Object> convert(Predicate<H> op) {
        return a -> op.test((H) a);
    }

    public void computePath(Vertex source) {
        // heap
        push.accept(dataStructure, source);
        source.setDistance(0);
        while (!isEmpty.test(dataStructure)) {
            final Vertex actualVertex = (Vertex) poll.apply(dataStructure);
            for (Edge edge : actualVertex.getAdjacencyList()) {
                final Vertex v = edge.getTargetVertex();
                final double d = actualVertex.getDistance() + edge.getWeight();
                if (d < v.getDistance()) {
                    v.setDistance(d);
                    v.setPredecessor(actualVertex);
                    use.accept(dataStructure, v);
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

