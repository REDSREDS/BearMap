import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

//    declare a LinkedHashMap to store all connected nodes.
    private final LinkedHashMap<Long, Node> nodes = new LinkedHashMap<>();

//    declare a LinkedHashMap to store all ways.
    private final LinkedHashMap<Long, Way> ways = new LinkedHashMap<>();

//    declare a LinkedHashMap to store all pathName between two nodes. the first string tells the id of two nodes, the second
//    string tells the pathName
    private final LinkedHashMap<String, String> pathName = new LinkedHashMap<>();

//    declare a LinkedHashMap to store all nodes
    private LinkedHashMap<Long, Node> totalNodes = new LinkedHashMap<>();

    /**
     * create node class with attributes: 1)id, 2) lon & lat 3) adjNodes 4) name 5) isConnected
     */
    static class Node {
        private long id;
        private double lon, lat;
        private ArrayList<Long> adjNodes;
        private String name;
        private boolean isConnected = false;

        Node (long id, double lon, double lat) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            adjNodes = new ArrayList<>();
        }

        public long getID() {
            return id;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setConnectedT() {
            isConnected = true;
        }

        public boolean getConnected() {
            return isConnected;
        }

        public void addAdjNode(long adj) {
            this.setConnectedT();
            adjNodes.add(adj);
        }

        public ArrayList<Long> getAdjNode() {
            return adjNodes;
        }

    }

    /**
     * insert a node to the nodes
     * @param id
     * @param temp
     */
    public void insertNode(long id, Node temp) {
        nodes.put(id, temp);
    }

    /**
     * remove a node from the nodes
     * @param id
     */
    public void removeNode(long id) {
        nodes.remove(id);
    }

    /**
     * create Way class with attributes: 1)isValid 2) max_speed 3) name 4) id 5) nodeInWay
     */
    static class Way {
        private boolean isValid;
        private String max_speed, name;
        private long id;
        private ArrayList<Long> nodeInWay;

        Way(long id) {
            this.id = id;
            nodeInWay = new ArrayList<>();
        }

        public void setMax_speed(String max_speed) {
            this.max_speed = max_speed;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public long getID() {
            return id;
        }

        public void setValid(boolean valid) {
            this.isValid = valid;
        }

        public void addNode(long id) {
            nodeInWay.add(id);
        }

        public ArrayList<Long> getNodes() {
            return nodeInWay;
        }

        public boolean getValid() {
            return isValid;
        }
    }

    /**
     * add a way to ways
     * @param id
     * @param temp
     */
    public void addWay(long id, Way temp) {
        ways.put(id, temp);
    }

    /**
     * return all ways
     * @return
     */
    public LinkedHashMap<Long, Way> getWays() {
        return ways;
    }

    /**
     * connect all nodes which are in the same way
     * @param way
     */
    public void buildPath(Way way) {
        Iterator<Long> ite = way.getNodes().iterator();
        long preID = ite.next();
        long postID = 0;
        while(ite.hasNext()) {
            postID = ite.next();
            Node pre = nodes.get(preID);
            Node post = nodes.get(postID);
            pre.addAdjNode(postID);
            post.addAdjNode(preID);
            pathName.put(preID + "to" + postID, way.getName());
            pathName.put(postID + "to" + preID, way.getName());
            preID = postID;

        }
    }

    /**
     * Return the pathname between two nodes
     * @param start the id of start node.
     * @param end the id of end node.
     */
    public String getPathName(long start, long end) {
        return pathName.get(start + "to" + end);
    }


    /**
     * constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        totalNodes = new LinkedHashMap<>(nodes);
        ArrayList<Long> keys = new ArrayList<> (nodes.keySet());
        for(long id : keys) {
            if(!nodes.get(id).getConnected())
                removeNode(id);
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        ArrayList<Long> id = new ArrayList<>();
        for(long temp : nodes.get(v).getAdjNode()) {
            id.add(temp);
        }
        return id;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double closestDis =Double.MAX_VALUE;
        long closestID = 0;
        for(long id : nodes.keySet()) {
            double dis = distance(nodes.get(id).getLon(), nodes.get(id).getLat(), lon, lat);
            if(dis < closestDis) {
                closestID = id;
                closestDis = dis;
            }
        }
        return closestID;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.get(v).getLon();
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.get(v).getLat();
    }

    /**
     * return the nodes
     */
    public LinkedHashMap<Long, Node> getNodes() {
        return nodes;
    }

    public LinkedHashMap<Long, Node> getTotalNodes() {
        return totalNodes;
    }


}
