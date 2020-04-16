package FXObjects;

import javafx.geometry.Point2D;

public class SaddleCon
{
	public Point2D pt;
	public sepStart s1;
	public sepStart s2;
	public Point2D line [];
	public SaddleCon(Point2D p, sepStart a, sepStart b, Point2D ln [])
	{
		pt = p;
		s1 = a;
		s2 = b;
		line = ln;
	}
}
