package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import view.GraphViewer;

import java.io.IOException;

public class QtmTester {
    public static void main(String[] args) {
        GraphLoader<Integer, String> graphLoader = new GraphLoader<>();
        try {
            Graph<Integer, String> graph = graphLoader.createGraphFromFile(args[0]);
            QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(graph, Integer.MAX_VALUE);
            Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover();

            GraphViewer.showGraph(resultGraph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
