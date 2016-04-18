package loader;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import structure.Edge;
import structure.Vertex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that provides methods to load a graph from a file into a JUNG graph.
 *
 * @author Zach Holland
 */
public class GraphLoader {

    /**
     * Creates a JUNG graph from a graph in a file in the .paj format.
     *
     * @param fileLocation The location of the graph file.
     * @return A graph object that represents the graph specified in the file.
     * @throws IOException
     */
    public static Graph<Vertex<Integer>, Edge<String>> createGraphFromFile(String fileLocation) throws IOException {
        Graph<Vertex<Integer>, Edge<String>> graph = new SparseGraph<>();
        Path path = Paths.get(fileLocation);

        Stream<String> lines = Files.lines(path);

        // Get all the vertices of the graph.
        List<String> vertexStringLines = lines.filter((line) -> {
            Pattern p = Pattern.compile("\\d+ \"\\d+\"");
            Matcher m = p.matcher(line);
            return m.matches();
        }).collect(Collectors.toList());

        Map<Integer, Vertex<Integer>> vertexMap = new HashMap<>(vertexStringLines.size());

        // Add the vertices to the graph.
        for (int i = 1; i <= vertexStringLines.size(); i++) {
            Vertex<Integer> v = new Vertex<>(i);
            vertexMap.put(i, v);
            graph.addVertex(v);
        }

        lines = Files.lines(path);

        // Get all the edges of the graph.
        List<String> edgeStringLines = lines.filter((line) -> {
            Pattern p = Pattern.compile("\\d+ \\d+");
            Matcher m = p.matcher(line);
            return m.matches();
        }).collect(Collectors.toList());

        // Add all the edges to the graph.
        for (String line : edgeStringLines) {
            StringTokenizer st = new StringTokenizer(line);
            String v1 = st.nextToken();
            String v2 = st.nextToken();
            graph.addEdge(new Edge<>(v1 + "-" + v2), vertexMap.get(Integer.valueOf(v1)), vertexMap.get(Integer.valueOf(v2)));
        }

        // Set the degree of all the vertex objects.
        graph.getVertices().forEach(v -> v.setDegree(graph.degree(v)));

        return graph;
    }

    /**
     * Creates a JUNG graph from a graph in a file that specifies only the edges of a graph.
     *
     * @param fileLocation The location of the graph file.
     * @return A graph object that represents the graph specified in the file.
     * @throws IOException
     */
    public static Graph<Vertex<Integer>, Edge<String>> createGraphFromFileEdgesOnly(String fileLocation) throws IOException {
        Graph<Vertex<Integer>, Edge<String>> graph = new SparseGraph<>();
        Path path = Paths.get(fileLocation);

        Stream<String> lines = Files.lines(path);
        HashMap<Integer, Vertex<Integer>> vertexMap = new HashMap<>();

        // For each line in the file, add the vertices if they are not already included,
        // and create the edge in the graph.
        lines.forEach(line -> {
            StringTokenizer st = new StringTokenizer(line);
            Integer v1 = Integer.valueOf(st.nextToken());
            Integer v2 = Integer.valueOf(st.nextToken());

            Vertex<Integer> vertex1 = vertexMap.get(v1);
            Vertex<Integer> vertex2 = vertexMap.get(v2);

            if (vertex1 == null) {
                vertex1 = new Vertex<>(v1);
                vertexMap.put(v1, vertex1);
                graph.addVertex(vertex1);
            }

            if (vertex2 == null) {
                vertex2 = new Vertex<>(v2);
                vertexMap.put(v2, vertex2);
                graph.addVertex(vertex2);
            }

            graph.addEdge(new Edge<>(v1 + "-" + v2), vertex1, vertex2);
        });

        // Set the degree of all the vertex objects.
        graph.getVertices().forEach(v -> v.setDegree(graph.degree(v)));

        return graph;
    }
}
