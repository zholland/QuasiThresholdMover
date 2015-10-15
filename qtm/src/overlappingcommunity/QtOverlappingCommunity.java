package overlappingcommunity;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import structure.Edge;
import structure.Vertex;

import java.util.*;

public class QtOverlappingCommunity<V extends Comparable<V>> {
    private Graph<Vertex<V>, Edge<String>> _graph;
    private PriorityQueue<Vertex<V>> _verticesByDegree;
    private Comparator<Vertex<V>> _degreeComparator = (Vertex<V> v1, Vertex<V> v2) -> v2.getDegree() - v1.getDegree();
    private Set<Vertex<V>> _visited;

    public QtOverlappingCommunity(Graph<Vertex<V>, Edge<String>> graph) {
        _visited = new HashSet<>();
        _graph = graph;
        _verticesByDegree = new PriorityQueue<>(_graph.getVertexCount(), _degreeComparator);
        _graph.getVertices().forEach(v -> {
            v.setDegree(_graph.degree(v));
            _verticesByDegree.add(v);
        });
    }

    public List<Graph<V, String>> findCommunities() {
        List<Set<Vertex<V>>> communities = new LinkedList<>();
        while (!_verticesByDegree.isEmpty()) {
            Vertex<V> current = _verticesByDegree.poll();
            if (current != null && !_visited.contains(current)) {
                Set<Vertex<V>> community = qtSearch(current, new TreeSet<>(), new HashSet<>());
                communities.add(community);
            }
        }

        return buildCommunityGraphs(communities);
    }

    private Set<Vertex<V>> qtSearch(Vertex<V> v, Set<Vertex<V>> community, Set<Vertex<V>> ancestors) {
        _visited.add(v);
        community.add(v);
        _graph.getNeighbors(v).stream()
                .sorted(_degreeComparator)
                .filter(u -> {
                    for (Vertex<V> a : ancestors) {
                        if (!_graph.isNeighbor(a, u)) {
                            return false;
                        }
                    }
                    return true;
                })
                .forEach(u -> {
                    Set<Vertex<V>> newAncestors = new HashSet<>(ancestors);
                    newAncestors.add(v);
                    qtSearch(u, community, newAncestors);
                });


        return community;
    }

    private List<Graph<V, String>> buildCommunityGraphs(List<Set<Vertex<V>>> communities) {
        List<Graph<V, String>> graphs = new LinkedList<>();
        communities.forEach(c -> {
            Graph<V, String> graph = new SparseGraph<>();
            c.forEach(v -> {
                graph.addVertex(v.getId());
            });
//            c.forEach(v1 -> c.stream()
//                    .filter(v2 -> !v2.equals(v1) && !graph.isNeighbor(v1.getId(), v2.getId()))
//                    .forEach(v2 -> graph.addEdge(v1.toString() + "-" + v2.toString(), v1.getId(), v2.getId())));
            graphs.add(graph);
        });
        return graphs;
    }
}
