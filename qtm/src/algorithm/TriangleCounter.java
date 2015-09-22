package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class TriangleCounter<T extends Comparable<T>> {
    private Graph<T, String> _graph;

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

    public int countTriangles(T v1, T v2) {
        HashSet<Vertex<T>> vertexSet = getNeighborSet(v1, v2);
        vertexSet.retainAll(getNeighborSet(v2, v1));

        return vertexSet.size();
    }
}
