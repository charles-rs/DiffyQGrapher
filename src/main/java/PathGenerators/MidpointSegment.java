package PathGenerators;

import javafx.geometry.Point2D;

/**
 * Class representing segments and their midpoints along paths.
 * Invariant: distance from left to center is the same as distance from right to center
 */
public class MidpointSegment
{
	public final Point2D left, right, center;
	public MidpointSegment(Point2D left, Point2D right, Point2D center)
	{
		this.left = left;
		this.right = right;
		this.center = center;
	}
}
