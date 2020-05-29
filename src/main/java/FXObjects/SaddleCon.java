package FXObjects;

import javafx.geometry.Point2D;

/**
 * Class to represent a saddle connection. Contains a point and two separatrices.
 */
public class SaddleCon
{
	public Point2D pt;
	public sepStart s1;
	public sepStart s2;
	public SaddleCon(Point2D p, sepStart a, sepStart b)
	{
		pt = p;
		s1 = a;
		s2 = b;
	}
}
