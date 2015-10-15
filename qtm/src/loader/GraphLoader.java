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

public class GraphLoader {

    public static Graph<Vertex<Integer>, Edge<String>> createGraphFromFile(String fileLocation) throws IOException {
        Graph<Vertex<Integer>, Edge<String>> graph = new SparseGraph<>();
        Path path = Paths.get(fileLocation);

        Stream<String> lines = Files.lines(path);

        List<String> vertexStringLines = lines
                .filter((line) -> {
                    Pattern p = Pattern.compile("\\d+ \"\\d+\"");
                    Matcher m = p.matcher(line);
                    return m.matches();
                })
                .collect(Collectors.toList());

        Map<Integer, Vertex<Integer>> vertexMap = new HashMap<>(vertexStringLines.size());

        for (int i = 1; i <= vertexStringLines.size(); i++) {
            Vertex<Integer> v = new Vertex<>(i);
            vertexMap.put(i, v);
            graph.addVertex(v);
        }

        lines = Files.lines(path);

        List<String> edgeStringLines = lines
                .filter((line) -> {
                    Pattern p = Pattern.compile("\\d+ \\d+");
                    Matcher m = p.matcher(line);
                    return m.matches();
                })
                .collect(Collectors.toList());
        for (String line : edgeStringLines) {
            StringTokenizer st = new StringTokenizer(line);
            String v1 = st.nextToken();
            String v2 = st.nextToken();
            graph.addEdge(new Edge<>(v1 + "-" + v2), vertexMap.get(Integer.valueOf(v1)), vertexMap.get(Integer.valueOf(v2)));
        }

        graph.getVertices().forEach(v -> v.setDegree(graph.degree(v)));

        return graph;
    }
}
