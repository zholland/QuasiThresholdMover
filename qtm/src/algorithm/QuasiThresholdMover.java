package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuasiThresholdMover<T extends Comparable<T>> {
    private PriorityQueue<Vertex<T>> _verticies;
    private Graph<T, String> _graph;
    private Vertex<T> _root;

    public QuasiThresholdMover(Graph<T, String> graph, T root) {
        _graph = graph;
        _root = new Vertex<>(root, 0, null);
    }

    private void initialize() {
        // TODO: Use bucket-sort
        _verticies = new PriorityQueue<>();

        // Add root to every vertex
        _graph.addVertex(_root.getId());
        _graph.getVertices().stream()
                .filter(v -> !v.equals(_root.getId()))
                .forEach(id -> {
                    _graph.addEdge(_root.getId() + "-" + id, _root.getId(), id);
                    _verticies.add(new Vertex<>(id, _graph.getNeighborCount(id), _root));
                });

    }

    public Graph<Integer, String> doQuasiThresholdMover() {
        initialize();
        return null;
        //return _graph;
    }
}
