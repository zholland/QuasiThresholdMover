package utility;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class GraphUtils {
    // NOTE: This doesn't work.
    public static <V, E> Graph<V, E> cloneGraph(Graph<V, E> graph) {
        Graph<V, E> graphClone = new SparseGraph<>();
        graph.getVertices().forEach(graphClone::addVertex);
        graph.getEdges().forEach(e -> {
            Pair<V> endpoints = graph.getEndpoints(e);
            graphClone.addEdge(e, endpoints.getFirst(), endpoints.getSecond());
        });
        return graphClone;
    }
}
