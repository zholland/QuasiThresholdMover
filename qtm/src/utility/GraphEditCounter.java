package utility;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import structure.Edge;
import structure.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Provides some utility methods to compare the number of edits between two graphs.
 */
public class GraphEditCounter {

    /**
     * Computes the number of edits between the two given graphs.
     *
     * @param originalGraph
     * @param editedGraph
     * @param <V>           The id type of the vertex object.
     * @return The number of edits between the two graphs.
     */
    public static <V extends Comparable<V>> int numberOfEdits(Graph<Vertex<V>, Edge<String>> originalGraph, Graph<Vertex<V>, Edge<String>> editedGraph) {
        Set<Set<Integer>> originalEdgeSet = getEdgeSet(originalGraph);
        Set<Set<Integer>> editedEdgeSet = getEdgeSet(editedGraph);

        int originalSize = originalEdgeSet.size();
        int editedSize = editedEdgeSet.size();

        originalEdgeSet.retainAll(editedEdgeSet);
        int numberOfCommonEdges = originalEdgeSet.size();

        return (originalSize + editedSize) - 2 * numberOfCommonEdges;
    }

    private static <V extends Comparable<V>> Set<Set<Integer>> getEdgeSet(Graph<Vertex<V>, Edge<String>> graph) {
        Set<Set<Integer>> edgeSet = new HashSet<>();
        graph.getEdges().forEach(e -> {
            Pair<Vertex<V>> endpoints = graph.getEndpoints(e);
            Integer first = (Integer) endpoints.getFirst().getId();
            Integer second = (Integer) endpoints.getSecond().getId();
            Set<Integer> endpointSet = new TreeSet<>();
            endpointSet.add(first);
            endpointSet.add(second);
            edgeSet.add(endpointSet);
        });
        return edgeSet;
    }

    /**
     * Computes the number of edits between the two given graphs.
     *
     * @param originalGraph
     * @param editedGraph
     * @param <V>           The id type of the vertex object.
     * @return The number of edits between the two graphs.
     */
    public static <V extends Comparable<V>> int numberOfEditsAfterFinished(Graph<Vertex<V>, Edge<String>> originalGraph, Graph<V, String> editedGraph) {
        ArrayList<Vertex<V>> vertices = originalGraph.getVertices().stream()
                                                .collect(Collectors.toCollection(ArrayList::new));

        int edits = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                Vertex<V> a = vertices.get(i);
                Vertex<V> b = vertices.get(j);
                boolean edgeInOriginal = originalGraph.isNeighbor(a, b);

                if (edgeInOriginal != editedGraph.isNeighbor(a.getId(), b.getId())) {
                    edits++;
                }

            }
        }
        return edits;
    }
}
