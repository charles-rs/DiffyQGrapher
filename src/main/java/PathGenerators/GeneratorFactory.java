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

	/**
	 * Gets a circle loop generator that advances by pixels with a center and a radius
	 * @param px the size of a pixel
	 * @param center the center of the loop
	 * @param radius the radius
	 * @return the new circle generator
	 */
	public static LoopGenerator getCircleLoopGenerator(double px, Point2D center, double radius)
	{
		return new CircleLoopGenerator(px, center, radius);
	}

	/**
	 * Gets a circle loop generator where the radius is an integer number of pixels
	 * @param px the size of one pixel
	 * @param center the center of the circle
	 * @param pxRad the number of pixels for the radius
	 * @return the new circle generator
	 */
	public static LoopGenerator getCircleLoopGenerator(double px, Point2D center, int pxRad)
	{
		return getCircleLoopGenerator(px, center, px * pxRad);
	}

	/**
	 * Gets a new loop generator of the provided type
	 * @param l the type of the loop generator
	 * @param px the size of a pixel
	 * @param center the center of the loop
	 * @param radius the radius
	 * @return the new loop generator
	 *
	 */
	public static LoopGenerator getLoopGenerator(LoopType l, double px, Point2D center, double radius)
	{
		switch (l)
		{
			case CIRCLE:
				return  new CircleLoopGenerator(px, center, radius);
			default:
				throw new IllegalArgumentException();
		}
	}
	/**
	 * Gets a loop generator where the radius is an integer number of pixels
	 * @param l the type of the loop generator
	 * @param px the size of one pixel
	 * @param center the center of the circle
	 * @param pxRad the number of pixels for the radius
	 * @return the new loop generator
	 */
	public static LoopGenerator getLoopGenerator(LoopType l, double px, Point2D center, int pxRad)
	{
		return getLoopGenerator(l, px, center, px * pxRad);
	}


}
