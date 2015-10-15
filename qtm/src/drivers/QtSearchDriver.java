package drivers;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import overlappingcommunity.QtOverlappingCommunity;
import structure.Edge;
import structure.Vertex;
import view.GraphViewer;

import java.io.IOException;
import java.util.List;

public class QtSearchDriver {
    public static void main(String[] args) {
        try {
            Graph<Vertex<Integer>, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);
            QtOverlappingCommunity<Integer> qto = new QtOverlappingCommunity<>(graph);
            List<Graph<Integer, String>> resultGraphs = qto.findCommunities();

            resultGraphs.forEach(g -> GraphViewer.showGraph(new CircleLayout<>(g)));
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }
}
