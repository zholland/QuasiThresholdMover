package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import structure.Edge;
import structure.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class QtmTester {
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
                TimeEditsDto timeEditsDto = doMover(GraphLoader.createGraphFromFileEdgesOnly(args[0]), timeSum);
                timeSum += timeEditsDto.getTime();
                editsSum += timeEditsDto.getEdits();
                if (minEdits > timeEditsDto.getEdits()) {
                    minEdits = timeEditsDto.getEdits();
                }
            }

            System.out.println("Min edits: " + minEdits);
            System.out.println("Avg edits: " + (double) editsSum / (double) ITERATIONS);
            System.out.println("Avg time(ms): " + (double) timeSum / (double) ITERATIONS);
            // GraphViewer.showGraph(resultGraph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }

    private static TimeEditsDto doMover(Graph<Vertex<Integer>, Edge<String>> graph, Long timeSum) {
        System.out.println("Loaded");
        long startTime = System.nanoTime();
        QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(graph, Integer.MAX_VALUE);
        Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true);
        long endTime = System.nanoTime();

        ArrayList<Vertex<Integer>> vertices = graph.getVertices().stream()
                .collect(Collectors.toCollection(ArrayList::new));

        int edits = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                Vertex<Integer> a = vertices.get(i);
                Vertex<Integer> b = vertices.get(j);
                boolean edgeInOriginal = graph.isNeighbor(a, b);

                if (edgeInOriginal != resultGraph.isNeighbor(a.getId(), b.getId())) {
                    edits++;
                }

            }
        }

        long time = (endTime - startTime) / 1000000;
        //System.out.println("Edits: " + edits);
        //System.out.println("Time(ms): " + time);
        return new TimeEditsDto(time, edits);
    }
}
