package PathGenerators;

import javafx.geometry.Point2D;

public class FiniteSpiralGenerator extends SpiralGenerator implements FinitePathGenerator
{
	final double finalDist;
	protected FiniteSpiralGenerator(double inc, Point2D start, double radius, double finalDist)
	{
		super(inc, start, radius);
		this.finalDist = finalDist;
	}

	@Override
	public boolean done()
	{
		return start.distance(current) >= finalDist;
	}
	@Override
	public Point2D next()
	{
		if(!done())
			return super.next();
		else return current;
	}

}
