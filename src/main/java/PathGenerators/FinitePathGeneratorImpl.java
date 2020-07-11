package PathGenerators;

import javafx.geometry.Point2D;

public abstract class FinitePathGeneratorImpl extends GeneratorImpl implements FinitePathGenerator
{
	protected FinitePathGeneratorImpl(double inc, Point2D start)
	{
		super(inc, start);
	}
}
