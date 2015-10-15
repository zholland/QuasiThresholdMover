package algorithm;

import edu.uci.ics.jung.graph.Graph;
import structure.Edge;
import structure.Vertex;

import java.util.*;

public class QuasiThresholdMoverTopDown<V extends Comparable<V>> extends QuasiThresholdMover<V> {
    public QuasiThresholdMoverTopDown(Graph<Vertex<V>, Edge<String>> graph, V root) {
        super(graph, root);
    }

    private void core(Vertex<V> vm) {
        Queue<Vertex<V>> queue = new LinkedList<>();
        queue.addAll(_graph.getNeighbors(vm));
        _childCloseMap = new HashMap<>();
        _scoreMaxMap = new HashMap<>();

        while (!queue.isEmpty()) {
            Vertex<V> u = queue.poll();
            Set<Vertex<V>> uSubtree = new TreeSet<>();
            getSubtree(u, uSubtree);
            int childCloseU = childClose(vm, u);

            // Max over scoreMax of reported u-children
            Optional<ScoreMaxPair<V>> optionalX = u.getReportedChildren()
                    .stream()
                    .max((v1, v2) -> scoreMax(v2).getScoreMax() - scoreMax(v1).getScoreMax())
                    .map(this::scoreMax);
            ScoreMaxPair<V> x = optionalX.orElse(new ScoreMaxPair<V>(u, -1));

            // Sum over childClose of close u-children
            int y = 0;
            for (Vertex<V> v : uSubtree) {
                if (!v.equals(u)) {
                    int childCloseV = childClose(vm, v);
                    if (childCloseV > 0) {
                        y += childCloseV;
                    }
                }
            }

            if (_graph.getNeighbors(vm).contains(u)) {
                // scoreMax(u) = max{x,y} + 1
            } else {
                // scoreMax(u) = max{x,y} - 1
            }

            if (!u.equals(_root) && (childCloseU > 0 || scoreMax(u).getScoreMax() > 0)) {
                u.getParent().reportChild(u);
                queue.add(u.getParent());
            }
        }
    }

    private void setScoreMax(Vertex<V> v, ScoreMaxPair<V> scoreMaxPair) {
        _scoreMaxMap.put(v, scoreMaxPair);
    }

    private ScoreMaxPair<V> scoreMax(Vertex<V> v) {
        ScoreMaxPair<V> scoreMaxPair = _scoreMaxMap.get(v);
        if (scoreMaxPair == null) {
            scoreMaxPair = new ScoreMaxPair<>(v, -1);
        }
        return scoreMaxPair;
    }

    private int childClose(Vertex<V> vm, Vertex<V> v) {
        Integer childCloseScore = _childCloseMap.get(v);
        if (childCloseScore == null) {
            Set<Vertex<V>> uSubtree = new TreeSet<>();
            getSubtree(v, uSubtree);
            int vmNeighborsCount = (int) _graph.getNeighbors(vm).stream()
                    .filter(uSubtree::contains)
                    .count();
            childCloseScore = vmNeighborsCount - (uSubtree.size() - vmNeighborsCount);
            _childCloseMap.put(v, childCloseScore);
        }
        return childCloseScore;
    }


    private void getSubtree(Vertex<V> v, Set<Vertex<V>> subtree) {
        subtree.add(v);

        if (v.getChildren() != null && !v.getChildren().isEmpty()) {
            v.getChildren().stream().forEach(c -> getSubtree(c, subtree));
        }
    }

    @Override
    public Graph<V, String> doQuasiThresholdMover(boolean showTransitiveClosures) {
        initialize();


        return buildQtGraph(showTransitiveClosures);
    }
}
