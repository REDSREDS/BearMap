import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {

    /**
     *
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        long startND = g.closest(stlon, stlat);
        long endND = g.closest(destlon, destlat);

        //A*'s algorithm
        List<Long> path = stpA(g, startND, endND);

        return path;
    }

    public static List<Long> stpA(GraphDB g, long start, long end) {
        int verSize = ((Collection<Long>)g.vertices()).size();
        Map<Long, Long> edgeTo = new HashMap<>();
        Map<Long, Double> distance = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<Long> pq = new PriorityQueue<>(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return Double.compare(distance.get(o1) + g.distance(o1, end), distance.get(o2) + g.distance(o2, end));
            }
        });
        edgeTo.put(start, start);
        distance.put(start, 0.0);
        pq.add(start);


        while(pq.peek() != end) {
            long head = pq.poll();
            for(long temp : g.adjacent(head)) {
                if(!visited.contains(temp)) {
                    edgeTo.put(temp, head);
                    distance.put(temp, distance.get(head) + g.distance(temp, head));
                    pq.add(temp);
                }
                visited.add(head);
            }
        }
        Stack<Long> path = new Stack<>();
        long current = end;
        while(current != start) {
            path.push(current);
            current = edgeTo.get(current);
        }
        path.push(start);

        ArrayList<Long> stp = new ArrayList<>();
        while(!path.empty()) {
            stp.add(path.pop());
        }

        return stp;
    }




    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> direction = new ArrayList<>();


        NavigationDirection start = new NavigationDirection();
        start.direction = NavigationDirection.START;
        String pathName = g.getPathName(route.get(0), route.get(1));
        if(pathName != null)
            start.way = pathName;
        start.distance = g.distance(route.get(0), route.get(1));
        String currentWay = start.way;
        NavigationDirection currentDir = start;
        direction.add(currentDir);

        int change = 1;

        while(change + 1 < route.size()) {
            while (g.getPathName(route.get(change), route.get(change + 1)) == null && currentWay == NavigationDirection.UNKNOWN_ROAD) {
                currentDir.distance += g.distance(route.get(change), route.get(change + 1));
                change++;
                if(change + 1 == route.size())
                    break;
            }

            if(change + 1 == route.size())
                break;

            while (g.getPathName(route.get(change), route.get(change + 1)) != null && g.getPathName(route.get(change), route.get(change + 1)).equals(currentWay)) {
                currentDir.distance += g.distance(route.get(change), route.get(change + 1));
                change++;
                if(change + 1 == route.size())
                    break;
            }

            if(change + 1 == route.size())
                break;
            System.out.println(currentDir);
            currentDir = new NavigationDirection();
            currentDir.direction = directionChoice(g, route.get(change - 1), route.get(change), route.get(change + 1));
            pathName = g.getPathName(route.get(change), route.get(change + 1));
            if(pathName != null)
                currentDir.way = pathName;
            currentWay = currentDir.way;
            currentDir.distance = 0;
            direction.add(currentDir);
        }
        return direction;
    }

    public static int directionChoice(GraphDB g, long pre, long cur, long post) {
        double beforeTurn = g.bearing(pre, cur);
        double afterTurn = g.bearing(cur, post);
        if(beforeTurn < 0)
            beforeTurn += 360;
        if(afterTurn < 0)
            afterTurn += 360;

        double degree = afterTurn - beforeTurn;

        if(degree > 180)
            degree -= 360;

        if(degree < -180)
            degree +=360;

        //System.out.println(degree);
        if(degree < 15.0 && degree > -15.0)
            return NavigationDirection.STRAIGHT;
        else if(degree <30.0 && degree > -30.0) {
            if (degree > 0)
                return NavigationDirection.SLIGHT_RIGHT;
            else
                return NavigationDirection.SLIGHT_LEFT;
        }
        else if (degree <100 && degree > -100) {
            if(degree > 0)
                return NavigationDirection.RIGHT;
            else
                return NavigationDirection.LEFT;
        }
        else {
            if(degree > 0)
                return NavigationDirection.SHARP_RIGHT;
            else
                return NavigationDirection.SHARP_LEFT;
        }

    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "";

        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
