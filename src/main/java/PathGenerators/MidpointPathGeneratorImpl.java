package PathGenerators;


import javafx.geometry.Point2D;

public abstract class MidpointPathGeneratorImpl implements MidpointPathGenerator
{
	/**
	 * the current segment during generation
	 */
	MidpointSegment current;
	/**
	 * the precision to which we must calculate
	 */
	final double precision;

	protected MidpointPathGeneratorImpl(double prec)
	{
		this.precision = prec;
	}
	@Override
	public MidpointSegment getCurrent()
	{
		return current;
	}

	@Override
	public Point2D getCurrentPoint()
	{
		return current.center;
	}

	@Override
	public boolean done()
	{
		return current.left.distance(current.center) < precision;
	}

}
