package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public class TriangleCounter<T> {
    Graph<T, String> _graph;

    public TriangleCounter(Graph<T, String> graph) {
        _graph = graph;
    }

    private HashSet<Vertex<T>> getNeighborSet(T v, T exclude) {
        return _graph.getNeighbors(v)
                .stream()
                .filter(id -> !id.equals(exclude))
                .map(id -> new Vertex<>(id, _graph.degree(id)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private int countTriangles(T v1, T v2) {
        HashSet<Vertex<T>> vertexSet = getNeighborSet(v1, v2);
        vertexSet.addAll(getNeighborSet(v2, v1));

        PriorityQueue<Vertex<T>> vertexQueue = new PriorityQueue<>(vertexSet);


        HashMap<Vertex<T>, Set<Vertex<T>>> vertexMap = new HashMap<>();
        vertexQueue.stream().forEach(v -> {
            vertexMap.put(v, new HashSet<>());
        });

        int numTriangles = 0;
        while (!vertexQueue.isEmpty()) {
            Vertex<T> current = vertexQueue.poll();

        }


        return 0;
    }
}
