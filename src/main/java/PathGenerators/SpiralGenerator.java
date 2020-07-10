package PathGenerators;

import javafx.geometry.Point2D;

/**
 * SpiralGenerator is a path generator that follows an Archimedean spiral.
 */

public class SpiralGenerator extends GeneratorImpl
{
	/**
	 * r is the distance between each round of the spiral
	 */
	private final double r;
	/**
	 * theta and currentR are the current point in polar coordinates
	 */
	private double theta, currentR;
	SpiralGenerator(double inc, Point2D start, double radius)
	{
		super(inc, start);
		this.r = radius;
		this.theta = 0;
		this.currentR = 0;
	}
	SpiralGenerator(double inc, Point2D start, double radius, double theta)
	{
		super(inc, start);
		this.r = radius;
		this.theta = theta;
		this.currentR = 0;
	}

	@Override
	public Point2D next()
	{
		theta += Math.atan(inc/currentR);
		currentR = theta * (r/(2*Math.PI));
		current = start.add(new Point2D(currentR * Math.cos(theta), currentR * Math.sin(theta)));
		return current;
	}

}
