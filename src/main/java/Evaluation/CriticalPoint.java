package Evaluation;


import javafx.geometry.Point2D;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
/**
 * Class to represent critical points. Keeps track of where they are
 */
public class CriticalPoint
{
	public Point2D point;
	public CritPointTypes type;
	public SimpleEVD<SimpleMatrix> matrix;
	public CriticalPoint(Point2D p, CritPointTypes t, SimpleEVD<SimpleMatrix> s)
	{
		point = p;
		type = t;
		matrix = s;
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
}
