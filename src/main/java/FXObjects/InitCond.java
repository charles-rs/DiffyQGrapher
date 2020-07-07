package FXObjects;

import javafx.geometry.Point2D;

class InitCond
{
	public double x, y, t;

	public InitCond(double x, double y, double t)
	{
		this.x = x;
		this.y = y;
		this.t = t;
	}

	public InitCond(Point2D p)
	{
		this.t = t;
		this.x = p.getX();
		this.y = p.getY();
	}
}
