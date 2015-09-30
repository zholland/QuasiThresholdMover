package drivers;

import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import view.GraphViewer;

import java.io.IOException;

public class GraphTest {
    public static void main(String args[]) {
        GraphLoader<Integer, String> graphLoader = new GraphLoader<>();

        try {
            Graph<Integer, String> graph = graphLoader.createGraphFromFile(args[0]);
            GraphViewer.showGraph(graph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
