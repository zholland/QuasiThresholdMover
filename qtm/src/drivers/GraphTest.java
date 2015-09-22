package drivers;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import loader.GraphLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GraphTest {
    public static void main(String args[]) {
        GraphLoader<Integer, String> graphLoader = new GraphLoader<>();

        try {
            Graph<Integer, String> graph = graphLoader.createGraphFromFile(args[0]);
            showGraph(graph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }

    public static void showGraph(Graph<?,?> graph) {
        // The Layout<V, E> is parameterized by the vertex and edge types
        Layout<Integer, String> layout = new CircleLayout(graph);
        layout.setSize(new Dimension(600,600)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        BasicVisualizationServer<Integer,String> vv =
                new BasicVisualizationServer<>(layout);
        vv.setPreferredSize(new Dimension(650,650)); //Sets the viewing area size

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }
}
