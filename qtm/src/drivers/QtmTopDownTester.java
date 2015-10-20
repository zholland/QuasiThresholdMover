package drivers;

import algorithm.QuasiThresholdMover;
import algorithm.QuasiThresholdMoverTopDown;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import structure.Edge;
import structure.Vertex;
import view.GraphViewer;

import java.io.IOException;

public class QtmTopDownTester {
    public static void main(String[] args) {
        try {
            Graph<Vertex<Integer>, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);
            QuasiThresholdMover<Integer> qtm = new QuasiThresholdMoverTopDown<>(graph, Integer.MAX_VALUE);
            Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true);

            GraphViewer.showGraph(resultGraph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
