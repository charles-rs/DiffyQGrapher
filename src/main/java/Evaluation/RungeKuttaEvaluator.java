package Evaluation;

import AST.Derivative;
import Exceptions.EvaluationException;
import javafx.geometry.Point2D;

public class RungeKuttaEvaluator extends Evaluator
{
	protected RungeKuttaEvaluator(Derivative dx, Derivative dy)
	{
		super(dx, dy);
	}

	@Override
	public Point2D evaluate(double x, double y, double a, double b, double t, double inc)
	{
		double x1, y1, x2, y2, x3, y3, x4, y4;
		try
		{
			x1 = dx.eval(x, y, a, b, t);
			y1 = dy.eval(x, y, a, b, t);
			x2 = dx.eval(x + x1 * inc / 2, y + y1 * inc / 2, a, b, t + inc / 2);
			y2 = dy.eval(x + x1 * inc / 2, y + y1 * inc / 2, a, b, t + inc / 2);
			x3 = dx.eval(x + x2 * inc / 2, y + y2 * inc / 2, a, b, t + inc / 2);
			y3 = dy.eval(x + x2 * inc / 2, y + y2 * inc / 2, a, b, t + inc / 2);
			x4 = dx.eval(x + x3 * inc, y + y3 * inc, a, b, t + inc);
			y4 = dy.eval(x + x3 * inc, y + y3 * inc, a, b, t + inc);
			return new Point2D((1f/6f) * (x1 + 2 * x2 + 2* x3 + x4), (1f/6f) * (y1 + 2 * y2 + 2 * y3 + y4));
		} catch (EvaluationException e)
		{
			return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
		} catch (NullPointerException e)
		{
			return new Point2D(0, 0);
		}
	}
}
