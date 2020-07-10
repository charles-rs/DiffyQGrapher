package PathGenerators;

import javafx.geometry.Point2D;

public interface Generator
{
	/**
	 * Advances the generator one step and returns the result
	 * @return the next point in the generator
	 */
	Point2D next();

	/**
	 * Advances the generator by n steps and returns the result
	 * @param n the number of steps to advance
	 * @return the point after advancing n steps
	 */
	Point2D advance(int n);

	/**
	 * The current point of the generator
	 * @return the current point of the generator
	 */
	Point2D getCurrent();

	/**
	 * The increment of the generator
	 * @return the increment of the generator
	 */
	double getInc();

	/**
	 * the distance between the current point on the path and the start
	 * @return distance from start to current
	 */
	double distanceFromStart();
}
