package drivers;

import algorithm.Edge;
import algorithm.QuasiThresholdMover;
import algorithm.Vertex;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;

import java.io.IOException;

public class QtmTester {
    public static void main(String[] args) {
        try {
            Graph<Vertex<Integer>, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);
            QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(graph, Integer.MAX_VALUE);
            Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover();

            //GraphViewer.showGraph(resultGraph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
