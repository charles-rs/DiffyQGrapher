package Evaluation;

import AST.Derivative;
import Exceptions.EvaluationException;
import javafx.geometry.Point2D;

public class EulerEvaluator extends Evaluator
{
	public EulerEvaluator(Derivative dx, Derivative dy)
	{
		super(dx, dy);
	}
	@Override
	public Point2D evaluate(double x, double y, double a, double b, double t, double inc)
	{
		try
		{
			double y1 =  dy.eval(x, y, a, b, t);
			double x1 = dx.eval(x, y, a, b, t);
			return new Point2D(x1, y1);

		} catch (EvaluationException e)
		{
			return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
		} catch (NullPointerException n)
		{
			return new Point2D(0, 0);
		}
	}

	@Override
	public Point2D next()
	{
		try
		{
			y +=  inc * dy.eval(x, y, a, b, t);
			x += inc * dx.eval(x, y, a, b, t);
			t += inc;
			return new Point2D(x, y);

		} catch (EvaluationException e)
		{
			t = Double.MAX_VALUE * Math.signum(inc);
			return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
		} catch (NullPointerException n)
		{
			t = Double.MAX_VALUE * Math.signum(inc);
			return new Point2D(0, 0);
		}
	}
}
