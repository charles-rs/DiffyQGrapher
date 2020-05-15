package Evaluation;
/**
  The abstract class of evaluators
 */

import AST.Derivative;
import AST.Node;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.geometry.Point2D;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;


public abstract class Evaluator
{
	protected Derivative dy, dx;
	private Node d2ydx, d2ydy, d2xdx, d2xdy;
	private final double TOLERANCE = .001;
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

	/**
	 * Evaluates a diffyQ with the specified initial conditions
	 * @param x the x val
	 * @param y the y val
	 * @param a the a param
	 * @param b the b param
	 * @param t the time value
	 * @param inc the increment
	 * @return the point which it evaluates to
	 */
	abstract public Point2D evaluate(double x, double y, double a, double b, double t, double inc);

	/**
	 * Function to return the next value in an evaluator
	 * @return the next value
	 * Sideeffects: moves the current state of the evaluator forward.
	 */
	abstract public Point2D next();

	/**
	 * Initialises the evaluator with certain important information
	 * @param x the starting x
	 * @param y the starting y
	 * @param t the starting t
	 * @param a the a param
	 * @param b the b param
	 * @param inc the increment to be used
	 */
	public void initialise(double x, double y, double t, double a, double b, double inc)
	{
		this.t = t;
		this.x = x;
		this.y = y;
		this.a = a;
		this.b = b;
		this.inc = inc;
	}

	/**
	 * gets the current time of the evaluator
	 * @return the current t
	 */
	public double getT()
	{
		return t;
	}

	/**
	 * gets the inc of the evaluator (that it was last initialised with)
	 * @return the current inc
	 */
	public double getInc()
	{
		return inc;
	}

	/**
	 * Uses Newton's method to find a critical point starting at start, with other initial conditions
	 * @param start the start point
	 * @param a the a param
	 * @param b the b param
	 * @param t the start time
	 * @return the resulting critical point
	 * @throws RootNotFound whenever no critical point is found
	 */
	public CriticalPoint findCritical(Point2D start, double a, double b, double t) throws RootNotFound
	{
		Point2D sol = NewtonEvaluator.solve(20, start, a, b, t, dx, dy, 'x', 'y');
		{
			CritPointTypes type = null;
			try
			{
				SimpleMatrix deriv = new SimpleMatrix(2, 2);
				deriv.setRow(0, 0, d2xdx.eval(sol.getX(), sol.getY(), a, b, t), d2xdy.eval(sol.getX(), sol.getY(), a, b, t));
				deriv.setRow(1, 0, d2ydx.eval(sol.getX(), sol.getY(), a, b, t), d2ydy.eval(sol.getX(), sol.getY(), a, b, t));

				SimpleEVD<SimpleMatrix> evd = deriv.eig();

				if(Math.abs(evd.getEigenvalue(0).getImaginary()) < TOLERANCE &&
						Math.abs(evd.getEigenvalue(1).getImaginary()) < TOLERANCE)
				{
					if(evd.getEigenvalue(0).getReal() > TOLERANCE && evd.getEigenvalue(1).getReal() > TOLERANCE)
						type = CritPointTypes.NODESOURCE;
					else if(evd.getEigenvalue(0).getReal() < - TOLERANCE && evd.getEigenvalue(1).getReal() < - TOLERANCE)
						type = CritPointTypes.NODESINK;
					else type = CritPointTypes.SADDLE;
				}
				else
				{
					if(evd.getEigenvalue(0).getReal() > TOLERANCE)
						type = CritPointTypes.SPIRALSOURCE;
					else if(evd.getEigenvalue(0).getReal() < - TOLERANCE)
						type = CritPointTypes.SPIRALSINK;
					else
						type = CritPointTypes.CENTER;
				}
				return new CriticalPoint(sol, type, evd);
			} catch (EvaluationException e)
			{
				throw new RootNotFound();
			}
		}
	}

}
