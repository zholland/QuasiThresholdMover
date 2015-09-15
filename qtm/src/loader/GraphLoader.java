package loader;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphLoader<V, E> {

    public Graph<Integer, String> createGraphFromFile(String fileLocation) throws IOException {
        Graph<Integer, String> graph = new SparseGraph<>();
        Path path = Paths.get(fileLocation);

        Stream<String> lines = Files.lines(path);

        List<String> vertexStringLines = lines
                .filter((line) -> {
                    Pattern p = Pattern.compile("\\d+ \"\\d+\"");
                    Matcher m = p.matcher(line);
                    return m.matches();
                })
                .collect(Collectors.toList());
        for (int i = 1; i <= vertexStringLines.size(); i++) {
            graph.addVertex(i);
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
            graph.addEdge(v1 + "-" + v2, Integer.valueOf(v1), Integer.valueOf(v2));
        }

        return graph;
    }
}
