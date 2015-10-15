package algorithm;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import structure.Edge;
import structure.Vertex;
import utility.PseudoC4P4Counter;
import utility.TriangleCounter;

import java.util.*;
import java.util.stream.Collectors;

public class QuasiThresholdMover<V extends Comparable<V>> {
    protected PriorityQueue<Vertex<V>> _vertexQueue;
    protected Graph<Vertex<V>, Edge<String>> _graph;
    protected Vertex<V> _root;

    protected Map<Vertex<V>, Integer> _childCloseMap;
    protected Map<Vertex<V>, Integer> _dfsNextChildIndexMap;
    protected Map<Vertex<V>, ScoreMaxPair<V>> _scoreMaxMap;
    protected Map<Vertex<V>, Vertex<V>> _dfs;

    public QuasiThresholdMover(Graph<Vertex<V>, Edge<String>> graph, V root) {
        _graph = graph;
        _root = new Vertex<>(root, graph.getVertexCount(), null, 0);
    }

    protected void initialize() {
        // TODO: Use bucket-sort
        _vertexQueue = new PriorityQueue<>((v1, v2) -> v2.getDegree() - v1.getDegree());

        // Add root to every vertex
        _root.setParent(_root);
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

            if (tempParent != null && !tempParent.equals(current.getParent())) {
                changeParent(current, tempParent);
                current.setDepth(0);
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
        int degreeVm = _graph.degree(vm);
        Queue<Vertex<V>> queue = new PriorityQueue<>(degreeVm, (v1, v2) -> v2.getDepth() - v1.getDepth()); // I don't think there will be an overflow.
        queue.addAll(_graph.getNeighbors(vm));
        _dfsNextChildIndexMap = queue.stream().collect(Collectors.toMap(v -> v, v -> 0));
        _childCloseMap = queue.stream().collect(Collectors.toMap(v -> v, v -> 0));
        _scoreMaxMap = queue.stream().collect(Collectors.toMap((Vertex<V> v) -> v, (Vertex<V> v) -> new ScoreMaxPair<>(v, -1)));
        _dfs = queue.stream().collect(Collectors.toMap(v -> v, v -> v));
        HashSet<Vertex<V>> touched = new HashSet<>();
        while (!queue.isEmpty()) {
            Vertex<V> u = queue.poll();
            touched.add(u);

            if (childClose(u) > scoreMax(u)) {
                setScoreMax(u, childClose(u));
            }

            // TODO: Need to add marker to allow adjacency in G to be checked in constant time...
            if (_graph.getNeighbors(vm).contains(u)) {
                setChildClose(u, childClose(u) + 1);
                setScoreMax(u, new ScoreMaxPair<>(u, scoreMax(u) + 1));
            } else {
                setChildClose(u, childClose(u) - 1);
                setScoreMax(u, scoreMax(u) - 1);
            }

            if (!u.getChildren().isEmpty() && childClose(u) >= 0) {
                Vertex<V> x = u.getChildren().get(0);
                getNextChildIndex(u, true);
                while (x != u) {
                    if (!touched.contains(x) || childClose(x) < 0) {
                        setChildClose(u, childClose(u) - 1);
                        x = dfs(x);
                        if (childClose(u) < 0) {
                            setDfs(u, x);
                            break;
                        }
                        // Next node in DFS order after x below u.
                        x = getNextNodeInDfs(x);
                    } else {
                        // Next node in DFS order after the subtree of x below u.
                        x = u.getChildren().get(getNextChildIndex(u, true));
                    }
                }
            }

            if (!u.equals(_root)) {
                if (childClose(u) > 0) {
                    setChildClose(u.getParent(), childClose(u.getParent()) + childClose(u));
                    queue.add(u.getParent());
                }

                if (scoreMax(u) > scoreMax(u.getParent())) {
                    setScoreMax(u.getParent(), scoreMaxPair(u));
                    queue.add(u.getParent());
                }
            }
        }
    }

    private Vertex<V> getNextNodeInDfs(Vertex<V> x) {
        if (getNextChildIndex(x, false) < x.getChildren().size()) {
            return x.getChildren().get(getNextChildIndex(x, true));
        } else {
            return getNextNodeInDfs(x.getParent());
        }
    }

    private int getNextChildIndex(Vertex<V> v, boolean increment) {
        Integer index = _dfsNextChildIndexMap.get(v);
        if (index == null) {
            index = 0;
        }
        if (increment) {
            _dfsNextChildIndexMap.put(v, index + 1);
        }
        return index;
    }

    private void computeDepths(Vertex<V> v, int depth) {
        v.setDepth(depth);
        v.getChildren().forEach(c -> computeDepths(c, depth + 1));
    }

    public Graph<V, String> doQuasiThresholdMover(boolean showTransitiveClosures) {
        initialize();
        computeDepths(_root, 0);
        for (int i = 0; i < 4; i++) {
            _graph.getVertices()
                    .stream()
                    .filter(vm -> vm != _root)
                    .forEach(vm -> {
                        Vertex<V> oldParent = vm.getParent();
                        ArrayList<Vertex<V>> oldChildren = vm.getChildren();
                        changeParent(vm, _root);
                        vm.setDepth(1);
                        vm.getChildren().forEach(this::decreaseChildrenDepth);
                        vm.setChildren(new ArrayList<>());
                        oldParent.getChildren().addAll(oldChildren);
                        oldChildren.forEach(c -> c.setParent(oldParent));
                        core(vm);

                        ScoreMaxPair<V> rootScoreMaxPair = scoreMaxPair(_root);
                        int scoreMax = rootScoreMaxPair.getScoreMax();
                        Vertex<V> bestParent = rootScoreMaxPair.getBestParent();
                    });
        }
        return buildQtGraph(showTransitiveClosures);
    }

    private void decreaseChildrenDepth(Vertex<V> v) {
        v.setDepth(v.getDepth() - 1);
        v.getChildren().forEach(this::decreaseChildrenDepth);
    }

    private int childClose(Vertex<V> u) {
        Integer childClose = _childCloseMap.get(u);
        if (childClose == null) {
            childClose = 0;
            setChildClose(u, childClose);
        }
        return childClose;
    }

    private void setChildClose(Vertex<V> u, int newChildClose) {
        _childCloseMap.put(u, newChildClose);
    }

    private Vertex<V> getScoreMaxBestParent(Vertex<V> u) {
        ScoreMaxPair scoreMaxPair = _scoreMaxMap.get(u);
        return scoreMaxPair == null ? u : scoreMaxPair.getBestParent();
    }

    private int scoreMax(Vertex<V> u) {
        ScoreMaxPair<V> scoreMaxPair = _scoreMaxMap.get(u);
        if (scoreMaxPair == null) {
            scoreMaxPair = new ScoreMaxPair<>(u, -1);
            setScoreMax(u, scoreMaxPair);
        }
        return scoreMaxPair.getScoreMax();
    }

    private ScoreMaxPair<V> scoreMaxPair(Vertex<V> u) {
        ScoreMaxPair<V> scoreMaxPair = _scoreMaxMap.get(u);
        if (scoreMaxPair == null) {
            scoreMaxPair = new ScoreMaxPair<>(u, -1);
            setScoreMax(u, scoreMaxPair);
        }
        return scoreMaxPair;
    }

    private void setScoreMax(Vertex<V> u, ScoreMaxPair<V> newScoreMaxPair) {
        _scoreMaxMap.put(u, newScoreMaxPair);
    }

    private void setScoreMax(Vertex<V> u, int scoreMax) {
        Vertex<V> bestParent = getScoreMaxBestParent(u);
        _scoreMaxMap.put(u, new ScoreMaxPair<>(bestParent, scoreMax));
    }

    private Vertex<V> dfs(Vertex<V> u) {
        Vertex<V> v = _dfs.get(u);
        if (v == null) {
            v = u;
            setDfs(u, v);
        }
        return v;
    }

    private void setDfs(Vertex<V> u, Vertex<V> newValue) {
        _dfs.put(u, newValue);
    }

    protected Graph<V, String> buildQtGraph(boolean showTransitiveClosures) {
        Graph<V, String> returnGraph = new SparseGraph<>();

        if (showTransitiveClosures) {
            _root.getChildren().stream().forEach(v -> {
                Set<Vertex<V>> ancestors = new HashSet<>();
                addEdges(v, ancestors, returnGraph);
            });
        } else {
            _graph.getVertices()
                    .stream()
                    .filter(v -> v != _root)
                    .forEach(v -> {
                        returnGraph.addVertex(v.getId());
                        if (v.getParent() != null && v.getParent() != _root) {
                            returnGraph.addEdge(v.getId() + "-" + v.getParent().getId(), v.getId(), v.getParent().getId());
                        }
                    });
        }
        return returnGraph;
    }

    private void addEdges(Vertex<V> v, Set<Vertex<V>> ancestors, Graph<V, String> returnGraph) {
        returnGraph.addVertex(v.getId());
        ancestors.stream().forEach(a -> returnGraph.addEdge(a.getId() + "-" + v.getId(), a.getId(), v.getId()));
        if (!v.getChildren().isEmpty()) {
            Set<Vertex<V>> newAncestors = new HashSet<>(ancestors);
            newAncestors.add(v);
            v.getChildren().stream().forEach(c -> addEdges(c, newAncestors, returnGraph));
        }
    }
}
