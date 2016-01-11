package loader;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import structure.Edge;
import structure.Vertex;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class PpiLoader {
    public static Graph<Vertex<String>, Edge<String>> loadPpiNetwork(String pathToFile) throws IOException {
        Graph<Vertex<String>, Edge<String>> graph = new SparseGraph<>();

        File ppiData = new File(pathToFile);
        CSVParser parser = CSVParser.parse(ppiData, Charset.defaultCharset(), CSVFormat.TDF);

        Set<String> vertices = new HashSet<>();

        parser.forEach(line -> {
            Vertex<String> v1 = new Vertex<>(line.get(0));
            Vertex<String> v2 = new Vertex<>(line.get(3));

            if (!v1.getId().equals(v2.getId())) {
                if (vertices.add(v1.getId())) {
                    graph.addVertex(v1);
                }

                if (vertices.add(v2.getId())) {
                    graph.addVertex(v2);
                }

                graph.addEdge(new Edge<>(v1.getId() + "-" + v2.getId()), v1, v2);
            }
        });

        graph.getVertices().forEach(v -> v.setDegree(graph.degree(v)));

        return graph;
    }

    public static Graph<Vertex<String>, Edge<String>> loadGoldStandard() {
        return null;
    }
}
