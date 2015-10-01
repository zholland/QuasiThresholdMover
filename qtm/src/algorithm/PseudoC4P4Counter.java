package algorithm;

import edu.uci.ics.jung.graph.Graph;

public class PseudoC4P4Counter {
    public static <V extends Comparable<V>> int score(Graph<V, Edge<String>> graph, V v1, V v2) {
        Edge<String> edge = graph.findEdge(v1, v2);
        return (graph.degree(v1) - 1 - edge.getNumTriangles()) * (graph.degree(v2) - 1 - edge.getNumTriangles());
    }
}
