package PathGenerators;


import javafx.geometry.Point2D;

public abstract class Generator
{
	protected final double inc;
	protected final Point2D start;
	protected Point2D current;
	protected int steps;

	protected Generator(double inc, Point2D start)
	{
		this.inc = inc;
		this.start = start;
		this.current = start;
		this.steps = 0;
	}

	/**
	 * Advances the generator one step and returns the result
	 * @return the next point in the generator
	 */
	public abstract Point2D next();


	/**
	 * Advances the generator by n steps and returns the result
	 * @param n the number of steps to advance
	 * @return the point after advancing n steps
	 */
	public Point2D advance(int n)
	{
		Point2D temp = this.getCurrent();
		for(int i = 0; i < n; i++)
			temp = this.next();
		return temp;
	}

	/**
	 * The current point of the generator
	 * @return the current point of the generator
	 */
	public Point2D getCurrent()
	{
		return current;
	}

	/**
	 * The increment of the generator
	 * @return the increment of the generator
	 */
	public double getInc()
	{
		return inc;
	}

	/**
	 * the distance between the current point on the path and the start
	 * @return distance from start to current
	 */
	public double distanceFromStart()
	{
		return current.distance(start);
	}
}
