package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.HashSet;
import java.util.PriorityQueue;

public class QuasiThresholdMover<T extends Comparable<T>> {
    private PriorityQueue<Vertex<T>> _vertexQueue;
    private Graph<T, String> _graph;
    private Vertex<T> _root;

    public QuasiThresholdMover(Graph<T, String> graph, T root) {
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
                    _graph.addEdge(_root.getId() + "-" + id, _root.getId(), id);
                    _vertexQueue.add(new Vertex<>(id, _graph.getNeighborCount(id), _root));
                });

        HashSet<Vertex<T>> processed = new HashSet<>(_vertexQueue.size());
        while (!_vertexQueue.isEmpty()) {
            Vertex<T> current = _vertexQueue.poll();
            processed.add(current);
            Vertex<T> tempParent;
        }
    }

    public Graph<Integer, String> doQuasiThresholdMover() {
        initialize();
        return null;
        //return _graph;
    }
}
