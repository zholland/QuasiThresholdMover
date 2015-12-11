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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Driver class to produce the GraphML files for the final report on diffusion of the Higgs Twitter data
 * for the final COSC 419 report.
 */
public class HiggsTwitterDiffusion {
    // The number of vertices to include in the network sample.
    public static final int NUMBER_OF_VERTICES = 6000;

    // The number of steps to break the time interval into.
    public static final int RESOLUTION = 20;

    // The number of largest connected components to plot.
    public static final int NUMBER_OF_COMPONENTS_TO_PLOT = 5;

    // The minimum component size to show in the network visualization.
    public static final int MINIMUM_COMPONENT_SIZE = 30;

    /**
     * Produces the required GraphML files to show the diffusion of the information across the communities in the
     * network. Visualisations are produced for the entire network, in various states of diffusion, at a series of
     * time steps. A sample of the largest connected components are produced in GraphML files as well.
     * <br><br>
     * The first argument is the location of the file containing the Twitter activity log.
     * <br><br>
     * The second argument is the location of the file containing the friend/follower network data.
     * <br><br>
     * The third argument is the directory which to output the GraphML files.
     * <br><br>
     * To view the GraphMLs it is best to use yEd.
     *
     * @param args location of the file containing the Twitter activity log,
     *             location of the file containing the friend/follower network data,
     *             the directory which to output the GraphML files
     */
    public static void main(String[] args) {
        try {
            // Parse the Twitter activity data. This is the transmission data.
            // This determines the required vertices to build the contact network.
            Set<Integer> includedVertices = new HashSet<>();
            ArrayList<HiggsActivityDto> dtoList = new ArrayList<>();
            parseActivityFile(args[0], dtoList, includedVertices);

            // Get the minimum and maximum times from the activity data.
            long maxTime = dtoList.stream().mapToLong(HiggsActivityDto::getTimestamp).max().orElse(0);
            long minTime = dtoList.stream().mapToLong(HiggsActivityDto::getTimestamp).min().orElse(0);

            // Time period
            long difference = maxTime - minTime;

            // Compute the length of the time step.
            long timeStep = difference / RESOLUTION;

            System.out.println("Max time: " + maxTime);
            System.out.println("Max time - min time (seconds):" + difference);

            // Create the input graph on which to find the communities from the edge file.
            Graph<Vertex<Integer>, Edge<String>> inputGraph = createGraphFromFileEdgesOnly(args[1], includedVertices);

            System.out.println("Vertices in input graph: " + inputGraph.getVertexCount());

            // Find the communities in the input graph.
            QuasiThresholdMover<Integer> qtm = new QuasiThresholdMover<>(inputGraph, Integer.MAX_VALUE);
            Graph<Integer, String> resultGraph = qtm.doQuasiThresholdMover(true);

            // Delete the small components in the resulting network and find the connected components.
            PriorityQueue<Set<Integer>> connectedComponents = deleteSmallComponents(resultGraph, MINIMUM_COMPONENT_SIZE);

            // Create the GraphMLs of the entire network.
            createGraphMLs(resultGraph, "output", minTime, timeStep, dtoList, args[2]);

            // Create the GraphMLs for the largest connected components.
            for (int i = 0; i < NUMBER_OF_COMPONENTS_TO_PLOT; i++) {
                Graph<Integer, String> componentGraph = createGraphOfComponent(resultGraph, connectedComponents.poll());
                createGraphMLs(componentGraph, "component" + i + "_", minTime, timeStep, dtoList, args[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a graph from the given graph using only the given vertices.
     * @param resultGraph The graph from which to create the component.
     * @param verticesInComponent The vertices to include in the component.
     * @return A graph created from the given graph using only the given vertices
     */
    private static Graph<Integer, String> createGraphOfComponent(Graph<Integer, String> resultGraph,
                                                                 Set<Integer> verticesInComponent) {
        // Add the given vertices to the componenst graph.
        Graph<Integer, String> componentGraph = new SparseGraph<>();
        verticesInComponent.forEach(componentGraph::addVertex);

        Integer[] vertexArray = new Integer[verticesInComponent.size()];
        vertexArray = verticesInComponent.toArray(vertexArray);

        // If an edge between two of the given vertices existed in the original graph, add an edge in the component.
        for (int i = 0; i < vertexArray.length; i++) {
            for (int j = i; j < vertexArray.length; j++) {
                if (resultGraph.containsEdge(vertexArray[i] + "-" + vertexArray[j])
                        || resultGraph.containsEdge(vertexArray[j] + "-" + vertexArray[i])) {
                    componentGraph.addEdge(vertexArray[i] + "-" + vertexArray[j], vertexArray[i], vertexArray[j]);
                }
            }
        }

        return componentGraph;
    }

    /**
     * Creates a GraphML file from the given graph.
     * @param resultGraph The graph from which to create the GraphML.
     * @param outputFilename The name of the output file.
     * @param minTime The start time of the diffusion.
     * @param timeStep The length of a time step.
     * @param dtoList The list of Higgs Activity data transfer objects.
     * @param fileOutputFolder The directory in which to create the output file.
     */
    private static void createGraphMLs(Graph<Integer, String> resultGraph,
                                       String outputFilename,
                                       long minTime,
                                       long timeStep,
                                       ArrayList<HiggsActivityDto> dtoList,
                                       String fileOutputFolder) {
        long currentTime = minTime + timeStep;

        // For each time step, create the GraphML of the graph and color the graph nodes if they are infected.
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

            graphToGraphMLFile(resultGraph, fileOutputFolder, outputFilename, i);
            colorGraphNodes(fileOutputFolder, outputFilename, i, infectedNodes);

            currentTime += timeStep;
        }
    }

    /**
     * Deletes the components of a graph that are smaller than the given size.
     * @param graph The graph from which to delete the components.
     * @param minComponentSize The minimum component size.
     * @return A priority queue containing the connected components sorted by decreasing size.
     */
    private static PriorityQueue<Set<Integer>> deleteSmallComponents(Graph<Integer, String> graph, int minComponentSize) {
        PriorityQueue<Set<Integer>> pq = new PriorityQueue<>((Set<Integer> s1, Set<Integer> s2) -> s2.size() - s1.size());

        Set<Integer> toVisit = new HashSet<>(graph.getVertexCount());
        toVisit.addAll(graph.getVertices());

        Set<Integer> currentComponent = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        // Breadth-first search to detect components.
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

            // Add the current component to the priority queue.
            pq.add(currentComponent);
            currentComponent = new HashSet<>();
        }
        return pq;
    }

    /**
     * Colors the nodes in the given GraphML if they are in the given list of nodes to color.
     * @param fileDirectory
     * @param outputFilename
     * @param iteration
     * @param verticesToColor
     */
    private static void colorGraphNodes(String fileDirectory, String outputFilename, int iteration, Set<Integer> verticesToColor) {
        String filepath = fileDirectory + outputFilename + iteration + ".graphml";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            // Set attributes of graphml. Most of them are specific for yEd
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

                // If the current node is in the list of nodes to color, color it.
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

            // Write the content into the xml file.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

            System.out.println(outputFilename + iteration + ".graphml created");

        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a GraphML of the given graph.
     * @param graph The graph of which to create the GraphML file.
     * @param outputDirectory The directory in which to save the GraphML.
     * @param outputFilename The name of the output file.
     * @param iteration The iteration number of the output.
     */
    private static void graphToGraphMLFile(Graph<Integer, String> graph, String outputDirectory, String outputFilename, int iteration) {
        PrintWriter out = null;
        try {
            GraphMLWriter<Integer, String> graphWriter = new GraphMLWriter<>();
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputDirectory + outputFilename + iteration + ".graphml")));
            graphWriter.save(graph, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Creates a graph from a file containing only edge definitions. i.e. on each line there are two node ids
     * separated by a space. Only the vertices in the given set of vertex ids are included.
     * @param fileLocation The location of the file to read.
     * @param includedVertices The vertices to include in the graph.
     * @return The graph created from the given edge file only included the given edges and their associated vertices.
     * @throws IOException
     */
    private static Graph<Vertex<Integer>, Edge<String>> createGraphFromFileEdgesOnly(String fileLocation, Set<Integer> includedVertices) throws IOException {
        Graph<Vertex<Integer>, Edge<String>> graph = new SparseGraph<>();
        Path path = Paths.get(fileLocation);

        Stream<String> lines = Files.lines(path);

        Map<Integer, Vertex<Integer>> vertices = new HashMap<>();

        // For each line in the given file...
        lines.forEach(line -> {
            StringTokenizer st = new StringTokenizer(line);
            Integer v1 = Integer.valueOf(st.nextToken());
            Integer v2 = Integer.valueOf(st.nextToken());

            // If the set of included vertices contains both endpoints of the current edge, include the in the
            // the graph.
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

    /**
     * Parses the given activity file and creates a list of data transfer objects containing the information.
     * @param fileLocation The location of the activity file.
     * @param activityDtoList The list of Higgs Twitter activity data transfer objects.
     * @param includedVertices The vertices included in the list.
     * @throws IOException
     */
    private static void parseActivityFile(String fileLocation, ArrayList<HiggsActivityDto> activityDtoList, Set<Integer> includedVertices) throws IOException {
        Path path = Paths.get(fileLocation);
        Stream<String> lines = Files.lines(path);

        // Keep adding dtos to the list until the limit of the network size is reached.
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

    /**
     * Represents an interaction from the Higgs Twitter activity log.
     */
    private static class HiggsActivityDto {
        private int _vertexA;
        private int _vertexB;

        // Time of the interaction.
        private long _timestamp;

        // The activity type.
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

    /**
     * Represents a Twitter activity type: retweet, mention and reply.
     */
    private enum ActivityType {
        RT, MT, RE
    }
}
