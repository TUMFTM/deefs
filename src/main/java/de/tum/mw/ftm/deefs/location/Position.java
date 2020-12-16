package de.tum.mw.ftm.deefs.location;

import static java.lang.Math.*;


/**
 * Position Element for all physical elements in the behavior model.
 * Supports distance calculations and coarse locations
 *
 * @author Michael Wittmann
 */
public class Position {

	private double x;
	private double y;
	private int area;


	/**
	 * Basic constructor for inputs from isarfunk data
	 *
	 * @param x    x-coordinates in degrees
	 * @param y    y-coordinates in degrees
	 * @param area area id
	 */
	public Position(double x, double y, int area) {
		super();
		this.x = x;
		this.y = y;
		this.area = area;
	}

	/**
	 * Alternative constructor for easy use with lat-lon positions
	 *
	 * @param lat
	 * @param lon
	 */
	public Position(double lat, double lon) {
		super();
		this.x = lon;
		this.y = lat;
		this.area = 0;
	}


	/**
	 * @return x-coordinate (longitude) of point in degrees
	 */
	public double getX() {
		return x;
	}

	/**
	 * sets the x-cooridnate
	 *
	 * @param x x-coordinate (longitude) of point in degrees
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return y-cooridnate (latitude) of point in degrees
	 */
	public double getY() {
		return y;
	}

	/**
	 * sets the y-coordinate
	 *
	 * @param y y-cooridnate (latitude) of point in degrees
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Gives back area's OSM-ID of the point if set.
	 *
	 * @return
	 */
	public int getArea() {
		return area;
	}

	/**
	 * Define the area's OSM-ID this point belongs to
	 *
	 * @param area OSM-ID (see isarfunk database for possible areas)
	 */
	public void setArea(int area) {
		this.area = area;
	}

	/**
	 * @return latitude of this point in degrees
	 */
	public double getLat() {
		return this.y;
	}

	/**
	 * Sets the latitude of this point
	 *
	 * @param lat latitude in degrees
	 */
	public void setLat(double lat) {
		this.y = lat;
	}

	/**
	 * @return longitude of this pint in degrees
	 */
	public double getLon() {
		return this.x;
	}

	/**
	 * Sets the longitude of this point
	 *
	 * @param lon longitude in degrees
	 */
	public void setLon(double lon) {
		this.x = lon;
	}

	/**
	 * Calculates distance to another Position in meter
	 * <p/> Uses Haversine-Formula:
	 * <p/> http://en.wikipedia.org/wiki/Haversine_formula
	 *
	 * @param p
	 * @return distacne to Position p in meter
	 */
	public double calcDist(Position p) {
		double sinDeltaLat = sin(toRadians(p.getLat() - this.getLat()) / 2);
		double sinDeltaLon = sin(toRadians(p.getLon() - this.getLon()) / 2);
		double normedDist = sinDeltaLat * sinDeltaLat
				+ sinDeltaLon * sinDeltaLon * cos(toRadians(this.getLat())) * cos(toRadians(p.getLat()));
		return 6371000 * 2 * asin(sqrt(normedDist));
	}

	/**
	 * @return Latitude rounded to 4 digits
	 */
	public double getCoarseLat() {
		return (double) Math.round(this.y * 10000) / 10000;
	}

	/**
	 * @return Longitude rounded to 4 digits
	 */
	public double getCoarseLon() {
		return (double) Math.round(this.x * 10000) / 10000;
	}

	@Override
	public String toString() {
		return String.format("[%f;%f]", y, x);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) return this.x == ((Position) obj).getX() && this.y == ((Position) obj).getY();
		return false;
	}


}
