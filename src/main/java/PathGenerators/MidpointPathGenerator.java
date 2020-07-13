package PathGenerators;

import javafx.geometry.Point2D;

/**
 * Midpoint path generators generate a path, but in segments so that the user tells it which side to generate
 * the segments on. This means it will only end up generating one segment of the desired length (eventually)
 */
public interface MidpointPathGenerator
{
	/**
	 * gets the current midpoint segment
	 * @return the current midpoint segment
	 */
	MidpointSegment getCurrent();

	/**
	 * calculates the next midpoint segment
	 * @param s the side to calculate
	 * @return the next midpoint segment
	 */
	MidpointSegment getNext(Side s);

	/**
	 * @return the center point of current
	 */
	Point2D getCurrentPoint();

	/**
	 * whether or not the segment is small enough to be finished
	 * @return whether or not the calculation is done
	 */
	boolean done();
}
