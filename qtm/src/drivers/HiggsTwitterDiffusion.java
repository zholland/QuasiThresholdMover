package drivers;

import algorithm.QuasiThresholdMover;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.io.GraphMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import structure.Edge;
import structure.Vertex;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HiggsTwitterDiffusion {
    public static final int NUMBER_OF_VERTICES = 6000;
    public static final int RESOLUTION = 20;

    public static void main(String[] args) {
        try {
            Set<Integer> includedVertices = new HashSet<>();
            ArrayList<HiggsActivityDto> dtoList = new ArrayList<>();
            parseActivityFile(args[0], dtoList, includedVertices);

            long maxTime = dtoList.stream().mapToLong(HiggsActivityDto::getTimestamp).max().orElse(0);
            long minTime = dtoList.stream().mapToLong(HiggsActivityDto::getTimestamp).min().orElse(0);

            long difference = maxTime - minTime;

            long timeStep = difference / RESOLUTION;

            System.out.println("Max time - min time (seconds):" + difference);

            Graph<Vertex<Integer>, Edge<String>> inputGraph = createGraphFromFileEdgesOnly(args[1], includedVertices);

            System.out.println(inputGraph.getVertexCount());

            QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(inputGraph, Integer.MAX_VALUE);
            Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true);

            deleteSmallComponents(resultGraph, 7);

            long currentTime = minTime + timeStep;
            for (int i = 0; i < RESOLUTION; i++) {
                final long time = currentTime;
                Set<Integer> infectedNodes
                        = dtoList.stream()
                        .filter(dto -> dto.getTimestamp() < time)
                        .map(HiggsActivityDto::getVertexA)
                        .collect(Collectors.toCollection(HashSet::new));
                infectedNodes.addAll(dtoList.stream()
                        .filter(dto -> dto.getTimestamp() < time)
                        .map(HiggsActivityDto::getVertexB)
                        .collect(Collectors.toList()));

                graphToGraphMLFile(resultGraph, args[2], i);
                colorGraphNodes(args[2], i, infectedNodes);

                currentTime += timeStep;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSmallComponents(Graph<Integer, String> graph, int minComponentSize) {
        Set<Integer> toVisit = new HashSet<>(graph.getVertexCount());
        toVisit.addAll(graph.getVertices());

        Set<Integer> currentComponent = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        // Breadth-first search
        while (!toVisit.isEmpty()) {
            queue.add(toVisit.iterator().next());
            while (!queue.isEmpty()) {
                Integer current = queue.poll();
                toVisit.remove(current);
                currentComponent.add(current);
                queue.addAll(graph.getNeighbors(current)
                        .stream()
                        .filter(toVisit::contains)
                        .collect(Collectors.toList()));
            }
            if (currentComponent.size() < minComponentSize) {
                currentComponent.forEach(graph::removeVertex);
            }
            currentComponent = new HashSet<>();
        }
    }

    public static void colorGraphNodes(String fileDirectory, int iteration, Set<Integer> verticesToColor) {
        String filepath = fileDirectory + "output" + iteration + ".graphml";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            // Set attributes of graphml
            Element graphmlRoot = (Element) doc.getElementsByTagName("graphml").item(0);
            graphmlRoot.setAttribute("xsi:schemaLocation",
                    "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
            graphmlRoot.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
            graphmlRoot.setAttribute("xmlns:java", "http://www.yworks.com/xml/yfiles-common/1.0/java");
            graphmlRoot.setAttribute("xmlns:sys", "http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0");
            graphmlRoot.setAttribute("xmlns:x", "http://www.yworks.com/xml/yfiles-common/markup/2.0");
            graphmlRoot.setAttribute("xmlns:y", "http://www.yworks.com/xml/graphml");
            graphmlRoot.setAttribute("xmlns:yed", "http://www.yworks.com/xml/yed/3");

            // Set graph undirected
            Element graphNode = (Element) doc.getElementsByTagName("graph").item(0);
            graphNode.setAttribute("edgedefault", "undirected");

            // Set Color Red
            Element key = doc.createElement("key");
            key.setAttribute("for", "node");
            key.setAttribute("id", "d6");
            key.setAttribute("yfiles.type", "nodegraphics");

            graphmlRoot.insertBefore(key, graphNode);

            NodeList nodeList = doc.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element nodeElement = (Element) nodeList.item(i);

                int nodeId = Integer.valueOf(nodeElement.getAttribute("id"));

                if (verticesToColor.contains(nodeId)) {
                    Element data = doc.createElement("data");
                    data.setAttribute("key", "d6");
                    Element shapeNode = doc.createElement("y:ShapeNode");
                    Element fillColor = doc.createElement("y:Fill");
                    fillColor.setAttribute("color", "#FF0000");
                    fillColor.setAttribute("transparent", "false");
                    shapeNode.appendChild(fillColor);
                    data.appendChild(shapeNode);
                    nodeElement.appendChild(data);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

            System.out.println("Done");

        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void graphToGraphMLFile(Graph<Integer, String> graph, String outputDirectory, int iteration) {
        PrintWriter out = null;
        try {
            GraphMLWriter<Integer, String> graphWriter = new GraphMLWriter<>();
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputDirectory + "output" + iteration + ".graphml")));
            graphWriter.save(graph, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void graphToEdgeFile(Graph<Integer, String> graph, String outputDirectory) {
        BufferedWriter bufferedWriter = null;

        try {
            File file = new File(outputDirectory + "output.tgf");
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
