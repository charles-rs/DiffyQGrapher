package Evaluation;

import AST.Derivative;
import AST.Node;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.geometry.Point2D;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;


public abstract class Evaluator
{
	protected Derivative dy, dx;
	private Node d2ydx, d2ydy, d2xdx, d2xdy;
	private final double TOLERANCE = .0000001;
	protected double t, x, y, a, b, inc;
	protected Evaluator(Derivative dx, Derivative dy)
	{
		this.dy = dy;
		this.dx = dx;
		t = 0.;
		try
		{
			d2ydx = dy.differentiate('x');
			d2ydy = dy.differentiate('y');
			d2xdx = dx.differentiate('x');
			d2xdy = dx.differentiate('y');
		} catch (NullPointerException ignored){}
	}
	abstract public Point2D evaluate(double x, double y, double a, double b, double t, double inc);
	abstract public Point2D next();
	public void initialise(double x, double y, double t, double a, double b, double inc)
	{
		this.t = t;
		this.x = x;
		this.y = y;
		this.a = a;
		this.b = b;
		this.inc = inc;
	}
	public double getT()
	{
		return t;
	}
	public CriticalPoint findCritical(Point2D start, double a, double b, double t) throws RootNotFound
	{
		Point2D first = newtonNext(start, a, b, t);
		Point2D old = start;
		for(int i = 0; i < 8; i++)
		{
			old = first;
			first = newtonNext(first, a, b, t);
		}
		if(old.distance(first) < .00001)
		{
			CritPointTypes type = null;
			try
			{
				SimpleMatrix deriv = new SimpleMatrix(2, 2);
				deriv.setRow(0, 0, d2xdx.eval(first.getX(), first.getY(), a, b, t), d2xdy.eval(first.getX(), first.getY(), a, b, t));
				deriv.setRow(1, 0, d2ydx.eval(first.getX(), first.getY(), a, b, t), d2ydy.eval(first.getX(), first.getY(), a, b, t));
				@SuppressWarnings("rawtypes")
				SimpleEVD evd = deriv.eig();
				//if(evd.getEigenvalue(0).isReal() && evd.getEigenvalue(1).isReal())
				if(evd.getEigenvalue(0).getImaginary() < TOLERANCE && evd.getEigenvalue(1).getImaginary() < TOLERANCE)
				{
					if(evd.getEigenvalue(0).getReal() > 0 && evd.getEigenvalue(1).getReal() > 0)
						type = CritPointTypes.NODESOURCE;
					else if(evd.getEigenvalue(0).getReal() < 0 && evd.getEigenvalue(1).getReal() < 0)
						type = CritPointTypes.NODESINK;
					else type = CritPointTypes.SADDLE;
				}
				else
				{
					if(evd.getEigenvalue(0).getReal() > 0)
						type = CritPointTypes.SPIRALSOURCE;
					else if(evd.getEigenvalue(0).getReal() < 0)
						type = CritPointTypes.SPIRALSINK;
					else
						type = CritPointTypes.CENTER;
				}
				return new CriticalPoint(first, type);
			} catch (EvaluationException e)
			{
				throw new RootNotFound();
			}
		}
		else throw new RootNotFound();
	}

	private Point2D newtonNext(Point2D start, double a, double b, double t) throws RootNotFound
	{

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

			deriv.set(0, 0, d2xdx.eval(start.getX(), start.getY(), a, b, t));
			deriv.set(0, 1, d2xdy.eval(start.getX(), start.getY(), a, b, t));
			deriv.set(1, 0, d2ydx.eval(start.getX(), start.getY(), a, b, t));
			deriv.set(1, 1, d2ydy.eval(start.getX(), start.getY(), a, b, t));

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

}
