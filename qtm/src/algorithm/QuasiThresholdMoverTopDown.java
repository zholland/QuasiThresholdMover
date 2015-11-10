package algorithm;

public class QuasiThresholdMoverTopDown//<V extends Comparable<V>> extends QuasiThresholdMover<V> {
{
//    public QuasiThresholdMoverTopDown(Graph<Vertex<V>, Edge<String>> graph, V root) {
//        super(graph, root);
//    }
//
//    @Override
//    protected void core(Vertex<V> vm) {
//        Queue<Vertex<V>> queue = new PriorityQueue<>(_depthComparator);
//        queue.addAll(_graph.getNeighbors(vm));
//        _childCloseMap = new HashMap<>();
//        _scoreMaxMap = new HashMap<>();
//
//        while (!queue.isEmpty()) {
//            Vertex<V> u = queue.poll();
//            Set<Vertex<V>> uSubtree = new TreeSet<>();
//            getSubtree(u, uSubtree);
//            int childCloseU = childClose(vm, u);
//
//            // Max over scoreMax of reported u-children
//            Optional<ScoreMaxPair<V>> optionalX = u.getReportedChildren()
//                    .stream()
//                    .max((v1, v2) -> scoreMax(v2).getScoreMax() - scoreMax(v1).getScoreMax())
//                    .map(this::scoreMax);
//            ScoreMaxPair<V> x = optionalX.orElse(new ScoreMaxPair<>(u, -1));
//
//            // Sum over childClose of close u-children
//            int y = 0;
//            for (Vertex<V> v : uSubtree) {
//                if (!v.equals(u)) {
//                    int childCloseV = childClose(vm, v);
//                    if (childCloseV > 0) {
//                        y += childCloseV;
//                    }
//                }
//            }
//
//            if (_graph.getNeighbors(vm).contains(u)) {
//                // scoreMax(u) = max{x,y} + 1
//                setScoreMax(u, getMaxScoreMaxPair(u, y, x, 1));
//            } else {
//                // scoreMax(u) = max{x,y} - 1
//                setScoreMax(u, getMaxScoreMaxPair(u, y, x, -1));
//            }
//
//            if (!u.equals(_root) && (childCloseU > 0 || scoreMax(u).getScoreMax() > 0)) {
//                u.getParent().reportChild(u);
//                queue.add(u.getParent());
//            }
//        }
//    }
//
//    private ScoreMaxPair<V> getMaxScoreMaxPair(Vertex<V> u,
//                                               int childCloseSum,
//                                               ScoreMaxPair<V> scoreMaxOverReportedChildren, int modifier) {
//        if (childCloseSum >= scoreMaxOverReportedChildren.getScoreMax()) {
//            return new ScoreMaxPair<>(u, childCloseSum + modifier);
//        } else {
//            return new ScoreMaxPair<>(scoreMaxOverReportedChildren.getBestParent(),
//                    scoreMaxOverReportedChildren.getScoreMax() + modifier);
//        }
//    }
//
////    private void setScoreMax(Vertex<V> v, ScoreMaxPair<V> scoreMaxPair) {
////        _scoreMaxMap.put(v, scoreMaxPair);
////    }
////
////    private ScoreMaxPair<V> scoreMax(Vertex<V> v) {
////        ScoreMaxPair<V> scoreMaxPair = _scoreMaxMap.get(v);
////        if (scoreMaxPair == null) {
////            scoreMaxPair = new ScoreMaxPair<>(v, -1);
////            _scoreMaxMap.put(v, scoreMaxPair);
////        }
////        return scoreMaxPair;
////    }
//
//    private int childClose(Vertex<V> vm, Vertex<V> v) {
//        Integer childCloseScore = _childCloseMap.get(v);
//        if (childCloseScore == null) {
//            Set<Vertex<V>> uSubtree = new TreeSet<>();
//            getSubtree(v, uSubtree);
//            int vmNeighborsCount = (int) _graph.getNeighbors(vm).stream()
//                    .filter(uSubtree::contains)
//                    .count();
//            childCloseScore = vmNeighborsCount - (uSubtree.size() - vmNeighborsCount);
//            _childCloseMap.put(v, childCloseScore);
//        }
//        return childCloseScore;
//    }
//
//
//    private void getSubtree(Vertex<V> v, Set<Vertex<V>> subtree) {
//        subtree.add(v);
//
//        if (v.getChildren() != null && !v.getChildren().isEmpty()) {
//            v.getChildren().stream().forEach(c -> getSubtree(c, subtree));
//        }
//    }
//
//    @Override
//    public Graph<V, String> doQuasiThresholdMover(boolean showTransitiveClosures) {
//        initialize();
//        computeDepths(_root, 0);
//        for (int i = 0; i < 4; i++) {
//            _graph.getVertices()
//                    .stream()
//                    .filter(vm -> vm != _root)
//                    .forEach(vm -> {
//                        Vertex<V> oldParent = vm.getParent();
//                        ArrayList<Vertex<V>> oldChildren = vm.getChildren();
//                        changeParent(vm, _root);
//                        vm.setDepth(1);
//                        vm.getChildren().forEach(c -> adjustChildrenDepth(c, -1));
//                        vm.setChildren(new ArrayList<>());
//                        oldParent.getChildren().addAll(oldChildren);
//                        oldChildren.forEach(c -> c.setParent(oldParent));
//                        core(vm);
//
//                        ScoreMaxPair<V> rootScoreMaxPair = scoreMax(_root);
//                        int scoreMax = rootScoreMaxPair.getScoreMax();
//                        Vertex<V> bestParent = rootScoreMaxPair.getBestParent();
//                        if (scoreMax > 0) {
//                            changeParent(vm, bestParent);
//                            vm.setDepth(bestParent.getDepth() + 1);
//                            adoptCloseChildren(vm);
//                        } else {
//                            changeParent(vm, oldParent);
//                            vm.setDepth(oldParent.getDepth() + 1);
//                            vm.setChildren(oldChildren);
//                            oldParent.getChildren().removeAll(oldChildren);
//                            vm.getChildren().forEach(c -> adjustChildrenDepth(c, 1));
//                        }
//                    });
//        }
//        return buildQtGraph(showTransitiveClosures);
//    }
//
//    private void adoptCloseChildren(Vertex<V> vm) {
//        _childCloseMap.forEach((c, childClose) -> {
//            if (c != _root && childClose > 0) {
//                changeParent(c, vm);
//            }
//        });
//    }
}
