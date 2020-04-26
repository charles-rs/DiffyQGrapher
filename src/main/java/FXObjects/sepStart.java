package FXObjects;

import Evaluation.CriticalPoint;
import javafx.geometry.Point2D;
import org.ejml.simple.SimpleBase;

public class sepStart
{
	public CriticalPoint saddle;
	public boolean positive;
	public int eigenvector;

	public sepStart(CriticalPoint s, boolean pos, int eig)
	{
		saddle = s;
		positive = pos;
		eigenvector = eig;
	}

	public Point2D getStart(double inc)
	{
		SimpleBase eig = saddle.matrix.getEigenVector(eigenvector);
		if(positive)
		{
			return new Point2D(saddle.point.getX() + inc * eig.get(0), saddle.point.getY() + inc * eig.get(1));
		} else
		{
			return new Point2D(saddle.point.getX() - inc * eig.get(0), saddle.point.getY() - inc * eig.get(1));
		}

	}
}
