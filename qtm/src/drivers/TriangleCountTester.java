package drivers;

import algorithm.QuasiThresholdMover;
import algorithm.TriangleCounter;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import loader.GraphLoader;
import view.GraphViewer;

import java.io.IOException;

public class TriangleCountTester {
    public static void main(String args[]) {
        GraphLoader<Integer, String> graphLoader = new GraphLoader<>();
        try {
            Graph<Integer, String> graph = graphLoader.createGraphFromFile(args[0]);
            TriangleCounter<Integer> tc = new TriangleCounter<>(graph);
            System.out.println(tc.countTriangles(1, 2));

            graph = new SparseGraph<>();
            graph.addVertex(1);
            graph.addVertex(2);
            graph.addVertex(3);
            graph.addEdge("1", 1, 2);
            graph.addEdge("2", 2, 3);
            graph.addEdge("3", 3, 1);

            tc = new TriangleCounter<>(graph);
            System.out.println(tc.countTriangles(1, 2));

        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
