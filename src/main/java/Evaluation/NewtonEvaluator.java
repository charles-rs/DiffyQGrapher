package Evaluation;

import AST.Node;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

public class NewtonEvaluator
{
	private static Point2D newtonNext(Point2D start, double a, double b, double t, Node dx, Node dy, char fst, char scd) throws RootNotFound
	{
		Node d2xd1 = dx.differentiate(fst).collapse();
		Node d2xd2 = dx.differentiate(scd).collapse();
		Node d2yd1 = dy.differentiate(fst).collapse();
		Node d2yd2 = dy.differentiate(scd).collapse();
		try
		{
			SimpleMatrix deriv = new SimpleMatrix(2, 2);
			SimpleMatrix init = new SimpleMatrix(2, 1);
			SimpleMatrix fx = new SimpleMatrix(2, 1);
			SimpleMatrix derivInv;
			fx.set(0, 0, dx.eval(start.getX(), start.getY(), a, b, t));
			fx.set(1, 0, dy.eval(start.getX(), start.getY(), a, b, t));



			init.set(0, 0, start.getX());
			init.set(1, 0, start.getY());

			deriv.set(0, 0, d2xd1.eval(start.getX(), start.getY(), a, b, t));
			deriv.set(0, 1, d2xd2.eval(start.getX(), start.getY(), a, b, t));
			deriv.set(1, 0, d2yd1.eval(start.getX(), start.getY(), a, b, t));
			deriv.set(1, 1, d2yd2.eval(start.getX(), start.getY(), a, b, t));
			try
			{
				derivInv = deriv.invert();
			} catch (SingularMatrixException s)
			{
				throw new RootNotFound();
			}
			SimpleMatrix result = init.minus(derivInv.mult(fx));
			return new Point2D(result.get(0, 0), result.get(1, 0));
		} catch (EvaluationException e)
		{
			throw new RootNotFound();
		}


	}
	public static Point2D solve(int iterations, Point2D start, double a, double b, double t, Node dx, Node dy, char fst, char scd) throws RootNotFound
	{
		double tol =  10E-8;//Double.MIN_VALUE;
		Point2D first = NewtonEvaluator.newtonNext(start, a, b, t, dx, dy, fst, scd);//newtonNext(start, a, b, t);
		Point2D old = start;
		for(int i = 0; i < iterations; i++)
		{
			old = first;
			first = NewtonEvaluator.newtonNext(first, a, b, t, dx, dy, fst, scd);//newtonNext(first, a, b, t);
			if(old.distance(first) < tol)
			{
				return first;
			}

		}
		if(old.distance(first) < tol) return first;
		else throw new RootNotFound();
	}

}
