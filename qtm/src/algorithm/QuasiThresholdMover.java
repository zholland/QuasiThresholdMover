package algorithm;

import edu.uci.ics.jung.graph.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class QuasiThresholdMover<V extends Comparable<V>> {
    private PriorityQueue<Vertex<V>> _vertexQueue;
    private Graph<Vertex<V>, Edge<String>> _graph;
    private Vertex<V> _root;

    public QuasiThresholdMover(Graph<Vertex<V>, Edge<String>> graph, V root) {
        _graph = graph;
        _root = new Vertex<>(root, graph.getVertexCount(), null);
    }

    private void initialize() {
        // TODO: Use bucket-sort
        _vertexQueue = new PriorityQueue<>((Vertex<V> v1, Vertex<V> v2) -> {
            if (v1.getDegree() > v2.getDegree()) {
                return -1;
            } else if (v1.getDegree() < v2.getDegree()) {
                return 1;
            } else {
                return 0;
            }
        });

        // Add root to every vertex
        _graph.addVertex(_root);
        _graph.getVertices().stream()
                .filter(v -> !v.equals(_root))
                .forEach(v -> {
                    v.setParent(_root);
                    _graph.addEdge(new Edge<>(_root.getId() + "-" + v.getId()), _root, v);
                    _vertexQueue.add(v);
                });

        _vertexQueue.add(_root);

        TriangleCounter.countAllTriangles(_graph);

        HashSet<Vertex<V>> processed = new HashSet<>(_vertexQueue.size());
        PseudoC4P4Counter<V> pc = new PseudoC4P4Counter<>(_graph);
        while (!_vertexQueue.isEmpty()) {
            Vertex<V> current = _vertexQueue.poll();
            processed.add(current);

            TreeSet<Vertex<V>> neighbors = _graph.getNeighbors(current).stream()
                    .filter(v -> !processed.contains(v) && (Objects.equals(current.getParent(), v.getParent())
                            || (pc.score(current, v) <= pc.score(v, v.getParent())
                            && v.getDepth() <= _graph.findEdge(v, current).getNumTriangles() + 1)))
                    .collect(Collectors.toCollection(TreeSet::new));

            Map<Vertex<V>, Integer> parentOccurrences = new HashMap<>();
            neighbors.stream().map(Vertex::getParent).forEach(p -> {
                Integer count = parentOccurrences.get(p);
                if (count == null) {
                    parentOccurrences.put(p, 1);
                } else {
                    parentOccurrences.put(p, count + 1);
                }
            });
            Optional<Map.Entry<Vertex<V>, Integer>> optionalEntry = parentOccurrences.entrySet().stream().max((e1, e2) -> e1.getValue().compareTo(e2.getValue()));
            Vertex<V> tempParent = optionalEntry.isPresent() ? optionalEntry.get().getKey() : null;

            if (!Objects.equals(tempParent, current.getParent())) {
                current.setParent(tempParent);
                current.setDepth(0);
                pc.setToInfinity(current, tempParent);
            }

            _graph.getNeighbors(current).stream()
                    .filter(v -> !processed.contains(v))
                    .filter(v -> Objects.equals(current.getParent(), v.getParent())
                            || (pc.score(current, v) < pc.score(v, v.getParent()) && v.getDepth() < _graph.findEdge(current, v).getNumTriangles() + 1))
                    .forEach(v -> {
                        v.setParent(current);
                        v.setDepth(v.getDepth() + 1);
                    });
        }
    }

    public Graph<Integer, String> doQuasiThresholdMover() {
        initialize();
        return null;
        //return _graph;
    }
}
