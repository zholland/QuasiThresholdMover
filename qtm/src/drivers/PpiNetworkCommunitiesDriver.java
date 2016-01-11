package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import loader.PpiLoader;
import structure.Edge;
import structure.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PpiNetworkCommunitiesDriver {
    public static final int ITERATIONS = 1;

    private static class TimeEditsDto {
        private long _time;
        private int _edits;

        private TimeEditsDto(long time, int edits) {
            _time = time;
            _edits = edits;
        }

        public long getTime() {
            return _time;
        }

        public int getEdits() {
            return _edits;
        }
    }

    public static void main(String[] args) {
        try {
            //Graph<Vertex<Integer>, Edge<String>> graph = GraphLoader.createGraphFromFile(args[0]);

            long timeSum = 0L;
            int editsSum = 0;
            int minEdits = Integer.MAX_VALUE;
            for (int i = 0; i < ITERATIONS; i++) {
                TimeEditsDto timeEditsDto = doMover(PpiLoader.loadPpiNetwork("/home/zach/IdeaProjects/QuasiThresholdMover/qtm/graphs/BINARY_PROTEIN_PROTEIN_INTERACTIONS.txt"), timeSum);
                timeSum += timeEditsDto.getTime();
                editsSum += timeEditsDto.getEdits();
                if (minEdits > timeEditsDto.getEdits()) {
                    minEdits = timeEditsDto.getEdits();
                }
            }

            System.out.println("Min edits: " + minEdits);
            System.out.println("Avg edits: " + (double) editsSum / (double) ITERATIONS);
            System.out.println("Avg time(ms): " + (double) timeSum / (double) ITERATIONS);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }

    private static TimeEditsDto doMover(Graph<Vertex<String>, Edge<String>> graph, Long timeSum) {
        System.out.println("Loaded");
        long startTime = System.nanoTime();
        QuasiThresholdMover<String> qtm = new QuasiThresholdMover<>(graph, "#####");
        Graph<String, String> resultGraph = qtm.doQuasiThresholdMover(false);
        long endTime = System.nanoTime();

        ArrayList<Vertex<String>> vertices = graph.getVertices().stream()
                .collect(Collectors.toCollection(ArrayList::new));

        int edits = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                Vertex<String> a = vertices.get(i);
                Vertex<String> b = vertices.get(j);
                boolean edgeInOriginal = graph.isNeighbor(a, b);

                if (edgeInOriginal != resultGraph.isNeighbor(a.getId(), b.getId())) {
                    edits++;
                }

            }
        }

        long time = (endTime - startTime) / 1000000;
        //System.out.println("Edits: " + edits);
        //System.out.println("Time(ms): " + time);

//        GraphViewer.showGraph(resultGraph);

        return new TimeEditsDto(time, edits);
    }
}
