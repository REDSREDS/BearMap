import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

//    declare all variables;
    private boolean querySuccess;
    private Map<String, Object> results;
    private double ullat;
    private double ullon;
    private double lrlat;
    private double lrlon;
    private double w;
    private double h;
    private String[][] renderGrid;

    public Rasterer() {

    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end.
     *
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "rendergrid"   : String[][], the files to display.
     * "ullon" : Number, the bounding upper left longitude of the rastered image.
     * "ullat" : Number, the bounding upper left latitude of the rastered image.
     * "lrlon" : Number, the bounding lower right longitude of the rastered image.
     * "lrlat" : Number, the bounding lower right latitude of the rastered image.
     * "depth"         : Number, the depth of the nodes of the rastered image
     * "querySuccess" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success!
     */


    public Map<String, Object> getMapRaster(Map<String, Double> params) {


//        initialize the variables from params
        ullat = params.get("ullat");
        ullon = params.get("ullon");
        lrlat = params.get("lrlat");
        lrlon = params.get("lrlon");
        w = params.get("w");
        h = params.get("h");
        querySuccess = true;
        results = new HashMap<>();

//        handle exceptions
        if(ullon > MapServer.ROOT_LRLON || lrlon < MapServer.ROOT_ULLON || lrlat > MapServer.ROOT_ULLAT || ullat < MapServer.ROOT_LRLAT || ullon > lrlon || ullat < lrlat) {
            querySuccess = false;
            results.put("query_success", querySuccess);
            return results;
        }

//        get the image depth
        int depth = getDepth(lrlon, ullon, w);


        //decide the lonLength(lonUnit) and latLength(latUnit) of one figure;
        double lonUnit = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / (Math.pow(2, depth));
        double latUnit = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / (Math.pow(2, depth));

        //get the index of the upperLeft figure, note if user requires exceed root boundary;
        int colStart = (int) (Math.max(ullon-MapServer.ROOT_ULLON, 0) / lonUnit);
        int rowStart = (int) (Math.max(MapServer.ROOT_ULLAT - ullat, 0) / latUnit);

        //get the index of the lowerRight figure, note if user requires exceed root boundary;
        int colEnd = (int) Math.min(Math.ceil((lrlon - MapServer.ROOT_ULLON) / lonUnit), Math.pow(2, depth)) - 1;
        int rowEnd = (int) Math.min(Math.ceil((MapServer.ROOT_ULLAT - lrlat) / latUnit) , Math.pow(2, depth)) - 1;

        //get the raster coordinate;
        double raster_ullon = MapServer.ROOT_ULLON + lonUnit * colStart;
        double raster_lrlon = MapServer.ROOT_ULLON + lonUnit * (colEnd + 1);
        double raster_ullat = MapServer.ROOT_ULLAT - latUnit * rowStart;
        double raster_lrlat = MapServer.ROOT_ULLAT - latUnit * (rowEnd + 1);


        getRenderGrid(rowStart, rowEnd, colStart, colEnd, depth);


        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", raster_ullon);
        results.put("raster_ul_lat", raster_ullat);
        results.put("raster_lr_lon", raster_lrlon);
        results.put("raster_lr_lat", raster_lrlat);
        results.put("depth", depth);
        results.put("query_success", querySuccess);

        return results;
    }


    /**
     * This is a helper function to get the required depth
     * @param lrlon lower right longitude
     * @param ullon upper left longitude
     * @param w width in pixel
     * @return depth
     */
    public int getDepth(double lrlon, double ullon, double w)  {

        int depth = 0;

//        user required lonDPP
        double userlonDPP = (lrlon - ullon) / w;

//        root lonDPP
        double rootlonDPP = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / 256;


//        the depth satisfies that acquiredDPP (rootDPP / 2 ^ depth) <= requiredDPP
        while (rootlonDPP / Math.pow(2, depth) > userlonDPP) {
                depth++;
        }

        return Math.min(depth, 7);
    }

    /**
     * This is a helper function to get 2d image grids given row num range and col num range and depth
     * @param rowStart row starts number
     * @param rowEnd row ends number
     * @param colStart col starts number
     * @param colEnd col ends number
     * @param depth depth
     * @return image
     */

    public void getRenderGrid(int rowStart, int rowEnd, int colStart, int colEnd, int depth) {


        int row = rowEnd - rowStart + 1;
        int col = colEnd - colStart + 1;
        renderGrid = new String[row][col];


        for(int i = 0; i < row; i++) {
            int rowCurrent = rowStart + i;
            for(int j = 0; j < col; j++) {
                int colCurrent = colStart + j;
                renderGrid[i][j] = "d" + depth + "_x" + colCurrent + "_y" + rowCurrent + ".png";
            }
        }
    }



}
