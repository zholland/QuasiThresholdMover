package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuasiThresholdMover<T> {
    private PriorityQueue<Vertex<T>> _verticies;
    private Graph<T, String> _graph;
    private Vertex<T> _root;

    public QuasiThresholdMover(Graph<T, String> graph, T root) {
        _graph = graph;
        _root = new Vertex<>(root, 0, null);
    }

    private void initialize() {
        // TODO: Use bucket-sort
        _verticies = new PriorityQueue<>(_graph.getVertexCount(), (Vertex<T> v1, Vertex<T> v2) -> v2.getDegree().compareTo(v1.getDegree()));

        T t = new T();

//        _graph.addVertex(Integer.MAX_VALUE);
        _graph.getVertices().stream()
//                .filter(id -> id != Integer.MAX_VALUE)
                .forEach(id -> {
                    _graph.addEdge(Integer.MAX_VALUE + "-" + id, Integer.MAX_VALUE, id);
                    _verticies.add(new Vertex(id, _graph.inDegree(id), null));
                });
//        _verticies.add(new Vertex(Integer.MAX_VALUE, _graph.inDegree(Integer.MAX_VALUE)));
    }

    public Graph<Integer, String> doQuasiThresholdMover() {
        initialize();

        return _graph;
    }
}
