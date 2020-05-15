package Evaluation;
/**
 * Evaluator using the midpoint Euler method
 */

import AST.Derivative;
import Exceptions.EvaluationException;
import javafx.geometry.Point2D;

public class MidEulerEvaluator extends Evaluator
{
	protected MidEulerEvaluator(Derivative dx, Derivative dy)
	{
		super(dx, dy);
	}

	@Override
	public Point2D evaluate(double x, double y, double a, double b, double t, double inc)
	{
		try
		{
			double x1 = x + (inc / 2) * dx.eval(x, y, a, b, t);
			double y1 = y + (inc / 2) * dy.eval(x, y, a, b, t);
			return new Point2D(dx.eval(x1, y1, a, b, t + (inc/2)), dy.eval(x1, y1, a, b, t + (inc/2)));
		} catch (EvaluationException e)
		{
			return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
		} catch (NullPointerException e)
		{
			return new Point2D(0, 0);
		}
	}

	@Override
	public Point2D next()
	{
		try
		{
			double x1 = x + (inc / 2) * dx.eval(x, y, a, b, t);
			double y1 = y + (inc / 2) * dy.eval(x, y, a, b, t);
			x += inc * dx.eval(x1, y1, a, b, t + (inc/2));
			y += inc * dy.eval(x1, y1, a, b, t + (inc/2));
			t += inc;
			return new Point2D(x, y);
		} catch (EvaluationException e)
		{
			t = Double.MAX_VALUE * Math.signum(inc);
			return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
		} catch (NullPointerException e)
		{
			t = Double.MAX_VALUE * Math.signum(inc);
			return new Point2D(0, 0);
		}
	}
}
