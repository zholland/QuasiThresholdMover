package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import loader.GraphLoader;
import structure.Edge;
import structure.Vertex;
import utility.GraphEditCounter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Driver used to run trials of the qtm algorithm on various graphs.
 * Provides some statistics about the results.
 *
 * @author Zach Holland
 */
public class QtmTester {
    // The number of trials to run.
    public static final int ITERATIONS = 100;

    /**
     * A data transfer object that tracks the number of edits and the time.
     */
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
            ArrayList<Integer> numEditsList = new ArrayList<>();

            long timeSum = 0L;
            int editsSum = 0;
            int minEdits = Integer.MAX_VALUE;

            // Do a number of trials equal to ITERATIONS
            for (int i = 0; i < ITERATIONS; i++) {
                TimeEditsDto timeEditsDto = doMover(GraphLoader.createGraphFromFile("/home/zach/IdeaProjects/QuasiThresholdMover/qtm/graphs/karate.paj"), timeSum);
                // Get the time and edits from the trial and track the minimum.
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

            // Display the statistics.
            System.out.println("Min edits: " + minEdits);
            System.out.println("Avg edits: " + avgEdits);
            System.out.println("STD: " + std);
            System.out.println("Avg time(ms): " + (double) timeSum / (double) ITERATIONS);

            // Uncomment to visualize with JUNG
            // GraphViewer.showGraph(resultGraph);
        } catch (IOException e) {
            System.out.println("Unable to load graph!");
            e.printStackTrace();
        }
    }

    /**
     * Runs the qtm algorithm on the given graph. Returns the time it took and the number of edits.
     *
     * @param graph   The graph to run the algorithm on.
     * @param timeSum The total sum of the time.
     * @return A dto with the time it took for the trial and the number of edits.
     */
    private static TimeEditsDto doMover(Graph<Vertex<Integer>, Edge<String>> graph, Long timeSum) {
        long startTime = System.nanoTime();
        QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(graph, Integer.MAX_VALUE);
        Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true, false);
        long endTime = System.nanoTime();

        int edits = GraphEditCounter.numberOfEditsAfterFinished(graph, resultGraph);

        long time = (endTime - startTime) / 1000000;

        // Uncomment to show the time and edits for each trial.
        //System.out.println("Edits: " + edits);
        //System.out.println("Time(ms): " + time);

        return new TimeEditsDto(time, edits);
    }
}
