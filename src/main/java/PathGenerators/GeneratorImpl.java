package PathGenerators;


import javafx.geometry.Point2D;

public abstract class GeneratorImpl implements Generator
{
	protected final double inc;
	protected final Point2D start;
	protected Point2D current;
	protected int steps;

	protected GeneratorImpl(double inc, Point2D start)
	{
		this.inc = inc;
		this.start = start;
		this.current = start;
		this.steps = 0;
	}

	public Point2D advance(int n)
	{
		Point2D temp = this.getCurrent();
		for(int i = 0; i < n; i++)
			temp = this.next();
		return temp;
	}

	public Point2D getCurrent()
	{
		return current;
	}

	public double getInc()
	{
		return inc;
	}

	public double distanceFromStart()
	{
		return current.distance(start);
	}
}
