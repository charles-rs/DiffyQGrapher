package PathGenerators;


import javafx.geometry.Point2D;

public class CircleLoopGenerator extends LoopGeneratorImpl
{
	double theta;
	final double r;
	final double thetaInc;
	CircleLoopGenerator(double inc, Point2D center, double r)
	{
		super(inc, center, r);
		this.r = r;
		theta = 0;
		thetaInc = Math.asin(inc/(2 * r));
	}

	@Override
	public Point2D next()
	{
		theta += thetaInc;
		if(theta > 2 * Math.PI)
		{
			rounds++;
			theta -= 2 * Math.PI;
		}
		current = new Point2D(r * Math.cos(theta), r * Math.sin(theta)).add(center);
		return current;
	}
}
