package drivers;

import algorithm.Edge;
import algorithm.QuasiThresholdMover;
import algorithm.TriangleCounter;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import loader.GraphLoader;
import view.GraphViewer;

import java.io.IOException;

public class TriangleCountTester {
    public static void main(String args[]) {
        try {
            Graph<Integer, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);
            TriangleCounter<Integer> tc = new TriangleCounter<>(graph);
            System.out.println(tc.countTriangles(1, 2));

            graph = new SparseGraph<>();
            graph.addVertex(1);
            graph.addVertex(2);
            graph.addVertex(3);
            //graph.addVertex(4);
            graph.addEdge(new Edge<>("1-2"), 1, 2);
            graph.addEdge(new Edge<>("2-3"), 2, 3);
            graph.addEdge(new Edge<>("3-1"), 3, 1);
            //graph.addEdge(new Edge<>("4-1"), 4, 1);
            //graph.addEdge(new Edge<>("4-2"), 4, 2);

            TriangleCounter.countAllTriangles(graph);
            graph.getEdges().forEach(e -> System.out.println(e.toString() + ": " + e.getNumTriangles()));
            GraphViewer.showGraph(graph);

        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
