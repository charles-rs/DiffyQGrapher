package PathGenerators;

import javafx.geometry.Point2D;

import javax.annotation.Nullable;

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
		return new SpiralGenerator(px/2, start, px/2);
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
		return new CircleLoopGenerator(px/2, center, radius);
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
				return  new CircleLoopGenerator(px/2, center, radius);
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

	/**
	 * Gets a finite path generator with the provided information
	 * @param ty the type of finite path generator
	 * @param px the size of one pixel
	 * @param center the center point
	 * @param maxDist the max distance from the start for a spiral generator (thrown out for other types
	 * @return the new finite path generator
	 */
	public static FinitePathGenerator getFinitePathGenerator(
			FinitePathType ty, double px, Point2D center,
			double maxDist, @Nullable Point2D old)
	{
		switch (ty)
		{
			case ARC:
				Point2D diff = center.subtract(old);
				double th = Math.atan(diff.getY()/diff.getX());
				return getArcGenerator(px, center, 3 * px, th-Math.PI/2, th+Math.PI/2);
			case SPIRAL:
				return getFiniteSpiralGenerator(px, center, maxDist);
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Creates a new finite Archimedean spiral generator
	 * @param px the size of one pixel
	 * @param center the center point
	 * @param maxD the distance from the center at which to stop
	 * @return the new spiral generator
	 */
	public static FinitePathGenerator getFiniteSpiralGenerator(double px, Point2D center, double maxD)
	{
		return new FiniteSpiralGenerator(px/2, center, 2 * px, maxD);
	}

	/**
	 * Constructs a new arc generator with the provided params
	 * @param px the size of one pixel
	 * @param center the center point
	 * @param radius the radius of the arc
	 * @param thetaStart the starting angle
	 * @param thetaEnd the ending angle
	 * @return the new arc generator
	 * @implNote theta is always incremented anticlockwise
	 */
	public static FinitePathGenerator getArcGenerator(
			double px, Point2D center, double radius, double thetaStart, double thetaEnd)
	{
		return new ArcGenerator(px/2, center, radius, thetaStart, thetaEnd);
	}

	public static MidpointPathGenerator getMidpointArcGenerator(
			double px, Point2D center, double thetaLeft, double thetaRight)
	{
		return new MidpointArcGenerator(center, px/2, thetaLeft, thetaRight, px * 8);
	}
	public static MidpointPathGenerator getMidpointArcGenerator(
			double px, Point2D center, Point2D old)
	{
		Point2D diff = center.subtract(old);
		double th = Math.atan(diff.getY()/diff.getX());
		th = Math.toRadians(diff.angle(1, 0));
		System.out.println("calculated theta: " + th);
		System.out.println("diff: " + diff);
		return getMidpointArcGenerator(px, center, th + Math.PI/2, th - Math.PI/2);
	}


}
