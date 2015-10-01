package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TriangleCounter<T extends Comparable<T>> {
    private Graph<T, Edge<String>> _graph;

    public TriangleCounter(Graph<T, Edge<String>> graph) {
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

    public static <V extends Comparable<V>> void countAllTriangles(Graph<V, Edge<String>> graph) {
        TreeSet<Vertex<V>> vertices = graph.getVertices().stream()
                .map((Function<V, Vertex<V>>) Vertex::new)
                .collect(Collectors.toCollection(TreeSet::new));

        HashMap<V, Set<V>> vertexMap = new HashMap<>(graph.getVertexCount());
        graph.getVertices().forEach(v -> vertexMap.put(v, new TreeSet<>()));

        vertices.forEach(s -> graph.getNeighbors(s.getId())
                .forEach(t -> {
                    int sDegree = graph.degree(s.getId());
                    int tDegree = graph.degree(t);
                    if ( sDegree > tDegree || (sDegree == tDegree && s.getId().compareTo(t) < 0)) {
                        Set<V> sSet = vertexMap.get(s.getId());
                        Set<V> tSet = vertexMap.get(t);
                        Set<V> intersection = new TreeSet<>(sSet);
                        intersection.retainAll(tSet);
                        intersection.forEach(v -> {
                            Edge<String> edgeST = graph.findEdge(s.getId(), t);
                            edgeST.setNumTriangles(edgeST.getNumTriangles() + 1);
                            Edge<String> edgeSV = graph.findEdge(s.getId(), v);
                            edgeSV.setNumTriangles(edgeSV.getNumTriangles() + 1);
                            Edge<String> edgeVT = graph.findEdge(v, t);
                            edgeVT.setNumTriangles(edgeVT.getNumTriangles() + 1);
                        });
                        vertexMap.get(t).add(s.getId());
                    }
                }));
    }
}
