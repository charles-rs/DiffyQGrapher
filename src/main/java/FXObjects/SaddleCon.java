package FXObjects;

import javafx.geometry.Point2D;

/**
 * Class to represent a saddle connection. Contains a point and two separatrices.
 */
public class SaddleCon
{
	public final Point2D pt;
	public final SepStart s1;
	public final SepStart s2;
	public final SaddleConTransversal transversal;
	public SaddleCon(Point2D p, SepStart a, SepStart b, SaddleConTransversal transversal)
	{
		pt = p;
		s1 = a;
		s2 = b;
		this.transversal = transversal;
	}
}
