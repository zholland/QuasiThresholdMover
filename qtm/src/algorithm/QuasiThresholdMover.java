package algorithm;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;

import java.util.*;
import java.util.stream.Collectors;

public class QuasiThresholdMover<V extends Comparable<V>> {
    private PriorityQueue<Vertex<V>> _vertexQueue;
    private Graph<Vertex<V>, Edge<String>> _graph;
    private Vertex<V> _root;

    private Map<Vertex<V>, Map<Vertex<V>, Integer>> _childCloseMap;

    public QuasiThresholdMover(Graph<Vertex<V>, Edge<String>> graph, V root) {
        _graph = graph;
        _root = new Vertex<>(root, graph.getVertexCount(), null);
        _childCloseMap = new HashMap<>();
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
                    _root.addChild(v);
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
                changeParent(current, tempParent);
                current.setDepth(tempParent == null ? 0 : tempParent.getDepth() + 1);
                pc.setToInfinity(current, tempParent);
            }

            _graph.getNeighbors(current).stream()
                    .filter(v -> !processed.contains(v))
                    .filter(v -> Objects.equals(current.getParent(), v.getParent())
                            || (pc.score(current, v) < pc.score(v, v.getParent()) && v.getDepth() < _graph.findEdge(current, v).getNumTriangles() + 1))
                    .forEach(v -> {
                        changeParent(v, current);
                        v.setDepth(v.getDepth() + 1);
                    });
        }
    }

    private void changeParent(Vertex<V> child, Vertex<V> newParent) {
        Vertex<V> oldParent = child.getParent();
        if (oldParent != null) {
            oldParent.removeChild(child);
        }
        child.setParent(newParent);
        if (newParent != null) {
            newParent.addChild(child);
        }
    }

    private void core(Vertex<V> vm) {
        Queue<Vertex<V>> queue = new LinkedList<>(_graph.getNeighbors(vm));
        while (!queue.isEmpty()) {
            Vertex<V> u = queue.poll();
            HashSet<Vertex<V>> touched = new HashSet<>();
            touched.add(u);

            if (childClose(vm, u) > scoreMax(vm, u)) {
                // scoreMax(u) <- childClose(u);
            }

            if (/*u is marked as neighbor*/true) {
                // childClose(u) <- childClose(u) + 2;
                // scoreMax(u) <- scoreMax(u) + 2;
            }
            // childClose(u) <- childClose(u) - 1;
            // scoreMax(u) <- scoreMax(u) - 1;

            if (!u.getChildren().isEmpty() && childClose(vm, u) >= 0) {
                u.getChildren().stream().findAny().ifPresent(x -> {
                    while (x != u) {
                        if (!touched.contains(x) || childClose(vm, x) < 0) {
                            setChildClose(vm, u, childClose(vm, u) - 1);
                            x = u;
                        }
                    }
                });
            }

        }
    }

    public Graph<V, String> doQuasiThresholdMover() {
        initialize();
//        _graph.getVertices()
//                .stream()
//                .filter(vm -> vm != _root)
//                .forEach(vm -> {
//                    changeParent(vm, _root);
//                    core(vm);
//                });
        return buildQtGraph();
    }

    private int childClose(Vertex<V> vm, Vertex<V> u) {
        Map<Vertex<V>, Integer> neighborMap = _childCloseMap.get(vm);
        if (neighborMap == null) {
            neighborMap = new TreeMap<>();
            _childCloseMap.put(vm, neighborMap);
        }

        Integer childCloseScore = neighborMap.get(u);
        if (childCloseScore == null) {
            childCloseScore = getChildCloseScore(vm, u);
            neighborMap.put(u, childCloseScore);
        }

        return childCloseScore;
    }

    private int getChildCloseScore(Vertex<V> vm, Vertex<V> u) {
        Set<Vertex<V>> uSubtree = new TreeSet<>();
        getSubtree(u, uSubtree);
        int vmNeighborsCount = (int)_graph.getNeighbors(vm).stream()
                .filter(uSubtree::contains)
                .count();
        return vmNeighborsCount - (uSubtree.size() - vmNeighborsCount);
    }

    private void getSubtree(Vertex<V> v, Set<Vertex<V>> subtree) {
        subtree.add(v);

        if (v.getChildren() != null && !v.getChildren().isEmpty()) {
            v.getChildren().stream().forEach(c -> getSubtree(c, subtree));
        }
    }

    private void setChildClose(Vertex<V> vm, Vertex<V> u, int newChildClose) {
        Map<Vertex<V>, Integer> neighborMap = _childCloseMap.get(vm);
        if (neighborMap == null) {
            neighborMap = new TreeMap<>();
            _childCloseMap.put(vm, neighborMap);
        }

        neighborMap.put(u, newChildClose);
    }

    private int scoreMax(Vertex<V> vm, Vertex<V> u) {
        return 0;
    }

    private Graph<V, String> buildQtGraph() {
        Graph<V, String> returnGraph = new SparseGraph<>();

        _graph.getVertices()
                .stream()
                .filter(v -> v != _root)
                .forEach(v -> {
                    returnGraph.addVertex(v.getId());
                    v.getChildren()
                            .stream()
                            .forEach(u -> returnGraph.addEdge(v.getId() + "-" + u.getId(), v.getId(), u.getId()));
                });

        return returnGraph;
    }
}
