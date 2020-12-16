package de.tum.mw.ftm.deefs.utils;

import de.tum.mw.ftm.deefs.location.Position;

/**
 * Helper Class, providing methods to do 2D Point Calculations
 *
 * @author Michael Wittmann
 */
public class PositionUtils {


	/**
	 * This Methods calculates the coordinates of a 2D-Point C witch is on a line from Point A to B with distance r from a
	 * ---------------------(B)--</br>
	 * ---------------------/----</br>
	 * --------------------/-----</br>
	 * -------------------/------</br>
	 * -----------------(C)------</br>
	 * -----------------/--------</br>
	 * ----------------/---------</br>
	 * --------------(A)---------
	 *
	 * @param A Point A
	 * @param B Point B
	 * @param r Distance of Point C from A
	 * @return distance in original unit
	 */
	public static Position getPointOnLine(Position A, Position B, double r) {

		double distA_B = A.calcDist(B);

		double[] AB = new double[2];
		//vector from A to B normed on meter
		AB[0] = (B.getX() - A.getX()) / distA_B;
		AB[1] = (B.getY() - A.getY()) / distA_B;

		return new Position(A.getX() + r * AB[0], A.getY() + r * AB[1], 0);
	}

}
