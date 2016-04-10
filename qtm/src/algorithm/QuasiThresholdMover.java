package algorithm;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import structure.Edge;
import structure.Vertex;
import utility.PseudoC4P4Counter;
import utility.TriangleCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class QuasiThresholdMover<V extends Comparable<V>> {
    protected PriorityQueue<Vertex<V>> _vertexQueue;
    protected Graph<Vertex<V>, Edge<String>> _graph;
    protected Graph<Vertex<V>, Edge<String>> _originalGraph;
    protected Vertex<V> _root;
    protected Random _random;

    protected Comparator<Vertex<V>> _depthComparator = (v1, v2) -> v2.getDepth() - v1.getDepth(); // I don't think there will be an overflow.

    protected Map<Vertex<V>, Integer> _scoreMaxMap;
    protected Map<Vertex<V>, Set<Vertex<V>>> _closeChildren;
    protected Map<Vertex<V>, Integer> _childCloseMap;
    protected Map<Vertex<V>, Vertex<V>> _bestParentMap;

    public QuasiThresholdMover(Graph<Vertex<V>, Edge<String>> graph, V root) {
        _random = new Random();
        _graph = graph;
        _root = new Vertex<>(root, graph.getVertexCount(), null, 0);
        _originalGraph = new SparseGraph<>();
        graph.getVertices().stream().forEach(v -> _originalGraph.addVertex(v));
        graph.getEdges().stream().forEach(e -> {
            Pair<Vertex<V>> endpoints = graph.getEndpoints(e);
            _originalGraph.addEdge(new Edge<>(endpoints.getFirst().getId() + "-" + endpoints.getSecond()), endpoints.getFirst(), endpoints.getSecond());
        });
//        _originalGraph.addVertex(_root);
//        _originalGraph.getVertices().stream()
//                .filter(v -> !_root.equals(v))
//                .forEach(v -> _originalGraph.addEdge(new Edge<>(_root.getId() + "-" + v.getId()), _root, v));
    }

    protected void initialize() {
        long startTime = System.nanoTime();

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
                                         || (pc.score(current, v) < pc.score(v, v.getParent())
                                                     && v.getDepth() < _graph.findEdge(current, v).getNumTriangles() + 1))
                    .forEach(v -> {
                        changeParent(v, current);
                        v.setDepth(v.getDepth() + 1);
                    });
        }
        long endTime = System.nanoTime();
//        System.out.println((double)(endTime - startTime) / 1000000d);
    }

    protected void changeParent(Vertex<V> child, Vertex<V> newParent) {
        Vertex<V> oldParent = child.getParent();
        if (oldParent != null) {
            oldParent.removeChild(child);
        }
        child.setParent(newParent);
        if (newParent != null) {
            newParent.addChild(child);
        }
    }

    protected void core(Vertex<V> vm) {
        // Queue of all vertices in T
        Queue<Vertex<V>> queue = new PriorityQueue<>(_graph.getVertexCount(), _depthComparator);
        queue.addAll(_graph.getVertices());

        _childCloseMap = new HashMap<>();
        _scoreMaxMap = new HashMap<>();
        _bestParentMap = new HashMap<>();
        _closeChildren = new HashMap<>();

        while (!queue.isEmpty()) {
            Vertex<V> v = queue.poll();

            int childCloseSum = 0;
            int childCloseSumOverCloseChildren = 0;
            Vertex<V> potentialBestChild = null;
            int potentialScoreMax = -1;

            for (Vertex<V> c : v.getChildren()) {
                int childCloseC = _childCloseMap.get(c);
                if (childCloseC > 0) {
                    Set<Vertex<V>> closeChildren = _closeChildren.get(v);
                    if (closeChildren == null) {
                        closeChildren = new TreeSet<>();
                    }
                    closeChildren.add(c);
                    _closeChildren.put(v, closeChildren);
                    childCloseSumOverCloseChildren += childCloseC;
                }
                childCloseSum += childCloseC;

                int scoreMaxC = _scoreMaxMap.get(c);
                if (scoreMaxC > potentialScoreMax) {
                    potentialScoreMax = scoreMaxC;
                    potentialBestChild = c;
                }
            }

            _childCloseMap.put(v, childCloseSum + diff(vm, v));
            if (potentialScoreMax > childCloseSumOverCloseChildren) {
                _scoreMaxMap.put(v, potentialScoreMax + diff(vm, v));
                _bestParentMap.put(v, _bestParentMap.get(potentialBestChild));
            } else {
                _scoreMaxMap.put(v, childCloseSumOverCloseChildren + diff(vm, v));
                _bestParentMap.put(v, v);
            }
        }
    }

    private int diff(Vertex<V> vm, Vertex<V> v) {
        return _graph.isNeighbor(vm, v) ? 1 : -1;
    }

    protected void computeDepths(Vertex<V> v, int depth) {
        v.setDepth(depth);
        v.getChildren().forEach(c -> computeDepths(c, depth + 1));
    }

    public Graph<V, String> doQuasiThresholdMover(boolean showTransitiveClosures) {
        initialize();
        computeDepths(_root, 0);
        ArrayList<Vertex<V>> vertices = _graph.getVertices().stream()
                                                .filter(vm -> vm != _root)
                                                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < 10; i++) {
            long startTime = System.nanoTime();
            Collections.shuffle(vertices);
            vertices.forEach(vm -> {
                Vertex<V> oldParent = vm.getParent();
                ArrayList<Vertex<V>> oldChildren = vm.getChildren();
                changeParent(vm, _root);
                vm.setDepth(1);
                vm.getChildren().forEach(c -> adjustChildrenDepth(c, -1));
                vm.setChildren(new ArrayList<>());
                oldParent.getChildren().addAll(oldChildren);
                oldChildren.forEach(c -> c.setParent(oldParent));
                core(vm);

                Vertex<V> newParent = _bestParentMap.get(_root);
                changeParent(vm, newParent);
                vm.setDepth(newParent.getDepth() + 1);
                Set<Vertex<V>> childrenToAdopt = _closeChildren.get(newParent);
                if (childrenToAdopt != null) {
                    newParent.getChildren().removeAll(childrenToAdopt);
                    vm.setChildren(new ArrayList<>(childrenToAdopt));
                    childrenToAdopt.forEach(c -> adjustChildrenDepth(c, 1));
                    childrenToAdopt.forEach(c -> c.setParent(vm));
                }
            });
            long endTime = System.nanoTime();
//            if (i==3) {
//                System.out.println((double) (endTime - startTime) / 1000000d);
//            }
//            System.out.println("Edits (iteration " + (i + 1) + "): " + GraphEditCounter.numberOfEditsAfterFinished(_originalGraph, buildQtGraph(true, false)));
        }
        return buildQtGraph(showTransitiveClosures, true);
    }

    protected void adjustChildrenDepth(Vertex<V> v, int adjustment) {
        v.setDepth(v.getDepth() + adjustment);
        v.getChildren().forEach(c -> adjustChildrenDepth(c, adjustment));
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

    protected Graph<V, String> buildQtGraph(boolean showTransitiveClosures, boolean finalGraph) {
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
        if (finalGraph) {
            _graph.removeVertex(_root);
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
