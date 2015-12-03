package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import structure.Edge;
import structure.Vertex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HiggsTwitterDiffusion {
    public static final int NUMBER_OF_VERTICES = 5000;

    public static void main(String[] args) {
        try {
            Set<Integer> includedVertices = new HashSet<>();
            ArrayList<HiggsActivityDto> dtoList = new ArrayList<>();
            parseActivityFile("/Users/zach/IdeaProjects/QuasiThresholdMover/qtm/graphs/higgs-activity_time.txt",
                    dtoList,
                    includedVertices);

            Graph<Vertex<Integer>, Edge<String>> inputGraph = createGraphFromFileEdgesOnly("/Users/zach/IdeaProjects/QuasiThresholdMover/qtm/graphs/higgs-social_network.edgelist",
                    includedVertices);

            System.out.println(inputGraph.getVertexCount());

            QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(inputGraph, Integer.MAX_VALUE);
            Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true);

            graphToEdgeFile(resultGraph);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void graphToEdgeFile(Graph<Integer, String> graph) {
        BufferedWriter bufferedWriter = null;

        try {
            File file = new File("/Users/zach/IdeaProjects/QuasiThresholdMover/qtm/graphs/output.tgf");
            bufferedWriter = new BufferedWriter(new FileWriter(file));

            ArrayList<String> lines
                    = graph.getVertices()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (String line : lines) {
                bufferedWriter.write(line + System.getProperty("line.separator"));
            }

            bufferedWriter.write("#" + System.getProperty("line.separator"));

            lines = graph.getEdges()
                    .stream()
                    .map((String in) -> {
                        StringTokenizer st = new StringTokenizer(in, "-");
                        return st.nextToken() + " " + st.nextToken();
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            for (String line : lines) {
                bufferedWriter.write(line + System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            System.err.println("Error writing the file : ");
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static Graph<Vertex<Integer>, Edge<String>> createGraphFromFileEdgesOnly(String fileLocation, Set<Integer> includedVertices) throws IOException {
        Graph<Vertex<Integer>, Edge<String>> graph = new SparseGraph<>();
        Path path = Paths.get(fileLocation);

        Stream<String> lines = Files.lines(path);

        Map<Integer, Vertex<Integer>> vertices = new HashMap<>();

        lines.forEach(line -> {
            StringTokenizer st = new StringTokenizer(line);
            Integer v1 = Integer.valueOf(st.nextToken());
            Integer v2 = Integer.valueOf(st.nextToken());

            if (includedVertices.contains(v1) && includedVertices.contains(v2)) {
                Vertex<Integer> vertex1 = vertices.get(v1);
                Vertex<Integer> vertex2 = vertices.get(v2);

                if (vertex1 == null) {
                    vertex1 = new Vertex<>(v1);
                    vertices.put(v1, vertex1);
                    graph.addVertex(vertex1);
                }

                if (vertex2 == null) {
                    vertex2 = new Vertex<>(v2);
                    vertices.put(v2, vertex2);
                    graph.addVertex(vertex2);
                }

                graph.addEdge(new Edge<>(v1 + "-" + v2), vertex1, vertex2);
            }
        });

        graph.getVertices().forEach(v -> v.setDegree(graph.degree(v)));

        return graph;
    }

    private static void parseActivityFile(String fileLocation, ArrayList<HiggsActivityDto> activityDtoList, Set<Integer> includedVertices) throws IOException {
        Path path = Paths.get(fileLocation);
        Stream<String> lines = Files.lines(path);

        lines.filter(p -> includedVertices.size() < NUMBER_OF_VERTICES)
                .forEach(line -> {
                    StringTokenizer st = new StringTokenizer(line);
                    HiggsActivityDto dto = new HiggsActivityDto(Integer.valueOf(st.nextToken()),
                            Integer.valueOf(st.nextToken()),
                            Long.valueOf(st.nextToken()),
                            ActivityType.valueOf(st.nextToken()));

                    activityDtoList.add(dto);

                    includedVertices.add(dto.getVertexA());
                    includedVertices.add(dto.getVertexB());
                });
    }

    public static class HiggsActivityDto {
        private int _vertexA;
        private int _vertexB;
        private long _timestamp;
        private ActivityType _activityType;

        public HiggsActivityDto(int vertexA, int vertexB, long timestamp, ActivityType activityType) {
            _vertexA = vertexA;
            _vertexB = vertexB;
            _timestamp = timestamp;
            _activityType = activityType;
        }

        public int getVertexA() {
            return _vertexA;
        }

        public int getVertexB() {
            return _vertexB;
        }

        public long getTimestamp() {
            return _timestamp;
        }

        public ActivityType getActivityType() {
            return _activityType;
        }
    }

    private enum ActivityType {
        RT, MT, RE
    }
}
