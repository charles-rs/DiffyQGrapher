package Evaluation;


import javafx.geometry.Point2D;
import org.ejml.simple.SimpleEVD;

public class CriticalPoint
{
	public Point2D point;
	public CritPointTypes type;
	@SuppressWarnings("rawtypes")
	public SimpleEVD matrix;
	public CriticalPoint(Point2D p, CritPointTypes t, SimpleEVD s)
	{
		point = p;
		type = t;
		matrix = s;
	}
}
