package Evaluation;


import javafx.geometry.Point2D;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
/**
 * Class to represent critical points. Keeps track of where they are
 */
public class CriticalPoint implements Cloneable
{
	public Point2D point;
	public CritPointTypes type;
	public SimpleEVD<SimpleMatrix> matrix;
	public SimpleMatrix jacob;
	public CriticalPoint(Point2D p, CritPointTypes t, SimpleEVD<SimpleMatrix> s, SimpleMatrix jacob)
	{
		point = p;
		type = t;
		matrix = s;
		this.jacob = jacob;
		try
		{
			if (t == CritPointTypes.SADDLE)
			{
				if (matrix.getEigenVector(0).get(0) < 0)
					matrix.getEigenVector(0).set(matrix.getEigenVector(0).negative());
				if (matrix.getEigenVector(1).get(0) < 0)
					matrix.getEigenVector(1).set(matrix.getEigenVector(1).negative());
			}
		} catch (NullPointerException ignored) {}
	}
	@Override
	public CriticalPoint clone()
	{
		return new CriticalPoint(point, type, matrix, jacob);
	}
}
