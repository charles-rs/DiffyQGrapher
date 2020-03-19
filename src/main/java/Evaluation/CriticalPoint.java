package Evaluation;


import javafx.geometry.Point2D;

public class CriticalPoint
{
	public Point2D point;
	public CritPointTypes type;
	public CriticalPoint(Point2D p, CritPointTypes t)
	{
		point = p;
		type = t;
	}
}
