package drivers;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import loader.GraphLoader;
import structure.Edge;
import structure.Vertex;
import utility.TriangleCounter;
import view.GraphViewer;

import java.io.IOException;

public class TriangleCountTester {
    public static void main(String args[]) {
        try {
            Graph<Vertex<Integer>, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);
            TriangleCounter<Integer> tc = new TriangleCounter<>(graph);

            graph = new SparseGraph<>();
            Vertex<Integer> v1 = new Vertex<>(1);
            Vertex<Integer> v2 = new Vertex<>(2);
            Vertex<Integer> v3 = new Vertex<>(3);
            graph.addVertex(v1);
            graph.addVertex(v2);
            graph.addVertex(v3);
            //graph.addVertex(4);
            graph.addEdge(new Edge<>("1-2"), v1, v2);
            graph.addEdge(new Edge<>("2-3"), v2, v3);
            graph.addEdge(new Edge<>("3-1"), v3, v1);
//            graph.addEdge(new Edge<>("4-1"), 4, 1);
//            graph.addEdge(new Edge<>("4-2"), 4, 2);

            TriangleCounter.countAllTriangles(graph);
            graph.getEdges().forEach(e -> System.out.println(e.toString() + ": " + e.getNumTriangles()));
            GraphViewer.showGraph(graph);

        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
