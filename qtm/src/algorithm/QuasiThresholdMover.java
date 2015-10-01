package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuasiThresholdMover<V extends Comparable<V>> {
    private PriorityQueue<Vertex<V>> _vertexQueue;
    private Graph<V, Edge<String>> _graph;
    private Vertex<V> _root;

    public QuasiThresholdMover(Graph<V, Edge<String>> graph, V root) {
        _graph = graph;
        _root = new Vertex<>(root, 0, null);
    }

    private void initialize() {
        // TODO: Use bucket-sort
        _vertexQueue = new PriorityQueue<>();

        // Add root to every vertex
        _graph.addVertex(_root.getId());
        _graph.getVertices().stream()
                .filter(v -> !v.equals(_root.getId()))
                .forEach(id -> {
                    _graph.addEdge(new Edge<>(_root.getId() + "-" + id), _root.getId(), id);
                    _vertexQueue.add(new Vertex<>(id, _graph.getNeighborCount(id), null));
                });

        TriangleCounter.countAllTriangles(_graph);

        HashSet<Vertex<V>> processed = new HashSet<>(_vertexQueue.size());
        while (!_vertexQueue.isEmpty()) {
            Vertex<V> current = _vertexQueue.poll();
            processed.add(current);
            Vertex<V> tempParent;
        }
    }

    public Graph<Integer, String> doQuasiThresholdMover() {
        initialize();
        return null;
        //return _graph;
    }
}
