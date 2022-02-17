package edu.illinois.cs.cs125.fall2019.mp;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Divides a rectangular area into identically sized, roughly square cells.
 * .
 * Each cell is given an X and Y coordinate. X increases from the west boundary toward the east boundary;
 * Y increases from south to north. So (0, 0) is the cell in the southwest corner.
 * Instances of this class are created with a desired cell size. However, it is unlikely that the
 * area dimensions will be an exact multiple of that length, so placing fully sized cells would
 * leave a small "sliver" on the east or north side. Length should be redistributed so that
 * each cell is exactly the same size. If the area is 70 meters long in one dimension and the cell
 * size is 20 meters, there will be four cells in that dimension (there's room for three full cells
 * plus a 10m sliver), each of which is 70 / 4 = 17.5 meters long. Redistribution happens
 * independently for the two dimensions, so a 70x40 area would be divided into 17.5x20.0 cells with
 * a 20m cell size.
 * .
 * You may find Java's Math.ceil function and our LatLngUtils.distance function helpful.
 */
public class AreaDivider {
    /** north boundary latitude. */
    private double north;

    /** east boundary longitude. */
    private double east;

    /** south boundary latitude. */
    private double south;

    /** south boundary longitude. */
    private double west;

    /** Cell size. */
    private double cellSize;

    /** Distance between east and west boundary. */
    private double xDist;

    /** Distance between north and south boundary. */
    private double yDist;

    /** Color of map grid lines. */
    private static final int GRID_COLOR = Color.BLACK;

    /** xCell length. */
    private double xLen;

    /** yCell length. */
    private double yLen;

    /***
     * Initializes values for the boundaries and cell size of the area.
     * @param setNorth latitude of the north boundary
     * @param setEast longitude of the east boundary
     * @param setSouth latitude of the south boundary
     * @param setWest longitude of the west boundary
     * @param setCellSize the requested side length of each cell, in meters
     */

    public AreaDivider(final double setNorth,
                       final double setEast,
                       final double setSouth,
                       final double setWest,
                       final double setCellSize) {
        north = setNorth;
        east = setEast;
        west = setWest;
        south = setSouth;
        cellSize = setCellSize;
        xDist = LatLngUtils.distance(north, west, north, east);
        yDist = LatLngUtils.distance(north, west, south, west);
        xLen = xDist / getXCells();
        yLen = yDist / getYCells();

    }

    /***
     * Gets the boundaries of the specified cell as a Google Maps LatLngBounds object.
     * @param x the cell's X coordinate
     * @param y the cell's Y coordinate
     * @return the boundaries of the cell
     */
    public com.google.android.gms.maps.model.LatLngBounds getCellBounds(final int x, final int y) {
        //difference in latitude values from
        double latRange = north - south;
        double lngRange = east - west;
        double lngCellLen = lngRange / getXCells();
        double latCellLen = latRange / getYCells();
        double s = south + (y * latCellLen);
        double w = west + (x * lngCellLen);
        LatLng southWest = new LatLng(s, w);
        double n = south + ((y + 1) * latCellLen);
        double e = west + ((x + 1) * lngCellLen);
        LatLng northEast = new LatLng(n, e);
        LatLngBounds l = new LatLngBounds(southWest, northEast);


        return l;
    }

    /***
     * Gets the number of cells between the west and east boundaries. See the class description for
     * more details on area division.
     * @return int
     */
    public int getXCells() {
        return (int) Math.ceil(xDist / cellSize);
    }

    /***
     * Gets the X coordinate of the cell containing the specified location. The point is not
     * necessarily within the area.
     * @param location location
     * @return int
     */
    public int getXCoordinate(final com.google.android.gms.maps.model.LatLng location) {
        double range = LatLngUtils.distance(location.latitude, west, location.latitude, location.longitude);
        return (int) Math.floor(range / xLen);
    }

    /***
     * @return int
     */
    public int getYCells() {
        return (int) Math.ceil(yDist / cellSize);
    }

    /***
     * @param location loc
     * @return int
     */
    public int getYCoordinate(final com.google.android.gms.maps.model.LatLng location) {
        double range = LatLngUtils.distance(south, location.longitude, location.latitude, location.longitude);
        return (int) (range / yLen);
    }

    /*** Draws the grid to a map using solid black polylines.
     *
     * There should be one line on each of the four boundaries of the overall area and as many
     * internal lines as necessary to divide the rows and columns of the grid. Each line should span
     * the whole width or height of the area rather than the side of just one cell. For example, an
     * area divided into a 2x3 grid would be drawn with 7 lines total: 4 for the outer boundaries,
     * 1 vertical line to divide the west half from the east half (2 columns), and 2 horizontal lines
     * to divide the area into 3 rows.
     *
     * See the provided addLine function from GameActivity for how to add a line to the map.
     * Since these lines should be black, you do not need the extra line to make the line appear
     * to have a border.
     *
     * @param map the Google map to draw on
     */
    public void renderGrid(final com.google.android.gms.maps.GoogleMap map) {
        //add borders
        LatLng topRight = new LatLng(north, east);
        LatLng topLeft = new LatLng(north, west);
        LatLng bottomRight = new LatLng(south, east);
        LatLng bottomLeft = new LatLng(south, west);
        final int lineThickness = 12;
        PolylineOptions top = new PolylineOptions().add(topLeft, topRight).color(GRID_COLOR)
                .width(lineThickness);
        PolylineOptions right = new PolylineOptions().add(topRight, bottomRight).color(GRID_COLOR)
                .width(lineThickness);
        PolylineOptions bottom = new PolylineOptions().add(bottomLeft, bottomRight).color(GRID_COLOR)
                .width(lineThickness);
        PolylineOptions left = new PolylineOptions().add(topLeft, bottomLeft).color(GRID_COLOR)
                .width(lineThickness);
        map.addPolyline(top);
        map.addPolyline(right);
        map.addPolyline(bottom);
        map.addPolyline(left);
        double xCellLen = (east - west) / this.getXCells();
        double yCellLen = (north - south) / this.getYCells();
        if (getYCells() * getXCells() > 1) {
            for (int x = 1; x < getXCells(); x++) {
                map.addPolyline(
                        new PolylineOptions()
                                .add(new LatLng(south,
                                west + (x * xCellLen)),
                                        new LatLng(north, west + (x * xCellLen)))
                                .color(GRID_COLOR).width(lineThickness));
            }
            for (int y = 1; y < getYCells(); y++) {
                map.addPolyline(
                        new PolylineOptions()
                                .add(new LatLng(south  + (y * yCellLen),
                                                west),
                                        new LatLng(south  + (y * yCellLen), east))
                                .color(GRID_COLOR).width(lineThickness));
            }
        }
        return;
    }


}
