package Events;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;

/**
 * Event for when a saddle point is selected for bifurcation calculations.
 * Carries the point of the saddle in the variable pt.
 */

public class SaddleSelected extends ActionEvent
{
	public final Point2D pt;
	public SaddleSelected(Point2D pt)
	{
		this.pt = pt;
	}
}
