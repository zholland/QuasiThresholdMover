package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import structure.Edge;
import structure.Vertex;
import utility.GraphEditCounter;

import java.io.IOException;
import java.util.ArrayList;

public class QtmTester {
    public static final int ITERATIONS = 100;

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

            ArrayList<Integer> numEditsList = new ArrayList<>();

            long timeSum = 0L;
            int editsSum = 0;
            int minEdits = Integer.MAX_VALUE;
            for (int i = 0; i < ITERATIONS; i++) {
                TimeEditsDto timeEditsDto = doMover(GraphLoader.createGraphFromFile("/home/zach/IdeaProjects/QuasiThresholdMover/qtm/graphs/karate.paj"), timeSum);
                timeSum += timeEditsDto.getTime();
                editsSum += timeEditsDto.getEdits();
                numEditsList.add(timeEditsDto.getEdits());
                if (minEdits > timeEditsDto.getEdits()) {
                    minEdits = timeEditsDto.getEdits();
                }
            }

            double avgEdits = (double) editsSum / (double) ITERATIONS;

            // Compute standard deviation
            double diffSum = 0;
            for (Integer edits : numEditsList) {
                diffSum += Math.pow(edits - avgEdits, 2);
            }

            double std = Math.sqrt(diffSum / (double) ITERATIONS);

            System.out.println("Min edits: " + minEdits);
            System.out.println("Avg edits: " + avgEdits);
            System.out.println("STD: " + std);
            System.out.println("Avg time(ms): " + (double) timeSum / (double) ITERATIONS);
            // GraphViewer.showGraph(resultGraph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }

    private static TimeEditsDto doMover(Graph<Vertex<Integer>, Edge<String>> graph, Long timeSum) {
//        System.out.println("Loaded");
        long startTime = System.nanoTime();
        QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(graph, Integer.MAX_VALUE);
        Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true, false);
        long endTime = System.nanoTime();

        int edits = GraphEditCounter.numberOfEditsAfterFinished(graph, resultGraph);

        long time = (endTime - startTime) / 1000000;
        //System.out.println("Edits: " + edits);
        //System.out.println("Time(ms): " + time);
        return new TimeEditsDto(time, edits);
    }
}
