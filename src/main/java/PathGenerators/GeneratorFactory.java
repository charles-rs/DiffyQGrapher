package PathGenerators;

import javafx.geometry.Point2D;

/**
 * Factory class for path generators
 */
public class GeneratorFactory
{
	/**
	 * gets a new spiral generator for analysing pixels
	 * @param px the size of one pixel
	 * @param start the start point
	 * @return the spiral path generator starting at start and covering pixels of size px
	 */
	public static Generator getSpiralGenerator(double px, Point2D start)
	{
		if(start == null) start = Point2D.ZERO;
		return new SpiralGenerator(px, start, px);
	}

	/**
	 * gets a new spiral generator for analysing pixels
	 * @param px the size of one pixel
	 * @param x the starting x coordinate
	 * @param y the starting y coordinate
	 * @return the spiral path generator starting at (x, y) and covering pixels of size px
	 */
	public static Generator getSpiralGenerator(double px, double x, double y)
	{
		return getSpiralGenerator(px, new Point2D(x, y));
	}
}
