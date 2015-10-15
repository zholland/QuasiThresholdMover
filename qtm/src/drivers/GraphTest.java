package drivers;

import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import structure.Edge;
import structure.Vertex;
import view.GraphViewer;

import java.io.IOException;

public class GraphTest {
    public static void main(String args[]) {
        try {
            Graph<Vertex<Integer>, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);
            GraphViewer.showGraph(graph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
