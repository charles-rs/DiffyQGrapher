package FXObjects;
/** Class representing separatrices. able to return their initial point.
 *  Three important pieces of info: the actual saddle point, which eigenvector, and whether to go with or
 *  against that eigenvector. This is stored in an int called state. The least significant bit is the eigenvector,
 *  with 0 corresponding to the negative one, and 1 to the positive and the second least significant is whether it it
 *  positive or negative
 *
 */

import Evaluation.CritPointTypes;
import Evaluation.CriticalPoint;
import javafx.geometry.Point2D;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;

public class sepStart implements Cloneable
{
	public final CriticalPoint saddle;
	private final int state;

	private sepStart(CriticalPoint s, int st)
	{
		this.saddle = s;
		this.state = st;
	}
	public sepStart(CriticalPoint s, boolean posDir, boolean posEig)
	{
		int state1;
		saddle = s;

		if(saddle.matrix.getEigenVector(0).get(0) < 0)
			saddle.matrix.getEigenVector(0).set(saddle.matrix.getEigenVector(0).negative());
		if(saddle.matrix.getEigenVector(1).get(0) < 0)
			saddle.matrix.getEigenVector(1).set(saddle.matrix.getEigenVector(1).negative());
		state1 = 0;
		if(posEig) state1 = 1;
		if(posDir) state1 |= 2;

		state = state1;
	}

	public boolean posDir()
	{
		return 1 == (state >> 1 & 1);
	}
	public boolean posEig()
	{
		return 1 == (1 & state);
	}

	public Point2D getStart(double inc)
	{
		if(saddle.type != CritPointTypes.SADDLE)
		{
			System.out.println("not a saddle");
		}
		SimpleBase<SimpleMatrix> eig;
		if (1 == (state & 1))
		{
			if(saddle.matrix.getEigenvalue(0).getReal() > 0)
				eig = saddle.matrix.getEigenVector(0);
			else eig = saddle.matrix.getEigenVector(1);
		} else
		{
			if(saddle.matrix.getEigenvalue(1).getReal() > 0)
				eig = saddle.matrix.getEigenVector(0);
			else eig = saddle.matrix.getEigenVector(1);
		}

		if(1 == (state >> 1 & 1))
		{
			return new Point2D(saddle.point.getX() + inc * eig.get(0), saddle.point.getY() + inc * eig.get(1));
		} else
		{
			return new Point2D(saddle.point.getX() - inc * eig.get(0), saddle.point.getY() - inc * eig.get(1));
		}

	}
	public static sepStart flip(final sepStart other)
	{
		return new sepStart(other.saddle, !other.posDir(), other.posEig());
	}
	public sepStart updateSaddle(CriticalPoint sad)
	{
		return new sepStart(sad, this.posDir(), this.posEig());
	}
	public sepStart clone()
	{
		return new sepStart(this.saddle, this.state);
	}
}
