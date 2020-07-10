package PathGenerators;

import javafx.geometry.Point2D;

public abstract class LoopGeneratorImpl extends GeneratorImpl implements LoopGenerator
{
	protected int rounds;
	protected final Point2D center;
	protected LoopGeneratorImpl(double inc, Point2D center, double rInitial)
	{
		super(inc, center.add(new Point2D(rInitial, 0)));
		rounds = 0;
		this.center = center;
	}
	public int numRounds()
	{
		return rounds;
	}
	public boolean completed()
	{
		return rounds > 0;
	}
}
