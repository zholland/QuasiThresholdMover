package algorithm;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;

import java.util.*;
import java.util.stream.Collectors;

public class QuasiThresholdMover<V extends Comparable<V>> {
    private PriorityQueue<Vertex<V>> _vertexQueue;
    private Graph<Vertex<V>, Edge<String>> _graph;
    private Vertex<V> _root;

    private Map<Vertex<V>, Integer> _childCloseMap;
    private Map<Vertex<V>, Integer> _scoreMaxMap;
    private Map<Vertex<V>, Vertex<V>> _dfs;

    public QuasiThresholdMover(Graph<Vertex<V>, Edge<String>> graph, V root) {
        _graph = graph;
        _root = new Vertex<>(root, graph.getVertexCount(), null, 0);
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
                    v.setDepth(1);
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

            if (tempParent != null && current.getParent() != null && !tempParent.equals(current.getParent())) {
                changeParent(current, tempParent);
                current.setDepth(tempParent.getDepth() + 1);
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
        _childCloseMap = queue.stream().collect(Collectors.toMap(v -> v, v -> 0));
        _scoreMaxMap = queue.stream().collect(Collectors.toMap(v -> v, v -> -1));
        _dfs = queue.stream().collect(Collectors.toMap(v -> v, v -> v));
        while (!queue.isEmpty()) {
            Vertex<V> u = queue.poll();
            HashSet<Vertex<V>> touched = new HashSet<>();
            touched.add(u);

            if (childClose(u) > scoreMax(u)) {
                setScoreMax(u, childClose(u));
            }

            // TODO: Need to add marker to allow adjacency in G to be checked in constant time...
            if (_graph.getNeighbors(vm).contains(u)) {
                setChildClose(u, childClose(u) + 2);
                setScoreMax(u, scoreMax(u) + 2);
            }
            setChildClose(u, childClose(u) - 1);
            setScoreMax(u, scoreMax(u) - 1);

            if (!u.getChildren().isEmpty() && childClose(u) >= 0) {
                Iterator<Vertex<V>> siblingIterator = u.getChildren().iterator();
                Vertex<V> x = siblingIterator.next();
                while (x != u) {
                    if (!touched.contains(x) || childClose(x) < 0) {
                        setChildClose(u, childClose(u) - 1);
                        x = dfs(x);
                        if (childClose(u) < 0) {
                            setDfs(u, x);
                            break;
                        }
                        //x = x.getChildren().
                    } else {
                        x = siblingIterator.next();
                    }
                }
            }

            if (!u.equals(_root)) {
                if (childClose(u) > 0) {
                    setChildClose(u.getParent(), childClose(u.getParent()) + childClose(u));
                    queue.add(u.getParent());
                }

                if (scoreMax(u) > scoreMax(u.getParent())) {
                    setScoreMax(u.getParent(), scoreMax(u));
                    queue.add(u.getParent());
                }
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

    private int childClose(Vertex<V> u) {
        return _childCloseMap.get(u);
    }

    private void setChildClose(Vertex<V> u, int newChildClose) {
        _childCloseMap.put(u, newChildClose);
    }

    private int scoreMax(Vertex<V> u) {
        return _scoreMaxMap.get(u);
    }

    private void setScoreMax(Vertex<V> u, int newScoreMax) {
        _scoreMaxMap.put(u, newScoreMax);
    }

    private Vertex<V> dfs(Vertex<V> u) {
        return _dfs.get(u);
    }

    private void setDfs(Vertex<V> u, Vertex<V> newValue) {
        _dfs.put(u, newValue);
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

    private Graph<V, String> buildQtGraph() {
        Graph<V, String> returnGraph = new SparseGraph<>();

        _root.getChildren().stream().forEach(v -> {
            Set<Vertex<V>> ancestors = new HashSet<>();
            addEdges(v, ancestors, returnGraph);
        });

//        _graph.getVertices()
//                .stream()
//                .filter(v -> v != _root)
//                .forEach(v -> {
//                    returnGraph.addVertex(v.getId());
//                    if (v.getParent() != null && v.getParent() != _root) {
//                        returnGraph.addEdge(v.getId() + "-" + v.getParent().getId(), v.getId(), v.getParent().getId());
//                    }
//                });
        return returnGraph;
    }

    private void addEdges(Vertex<V> v, Set<Vertex<V>> ancestors, Graph<V, String> returnGraph) {
        returnGraph.addVertex(v.getId());
        ancestors.stream().forEach(a -> returnGraph.addEdge(a.getId() + "-" + v.getId(), a.getId(), v.getId()));
        if (!v.getChildren().isEmpty()) {
            ancestors.add(v);
            v.getChildren().stream().forEach(c -> addEdges(c, ancestors, returnGraph));
        }
    }
}
