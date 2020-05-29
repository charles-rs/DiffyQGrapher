package Events;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;

/**
 * Event when either a source or a sink is selected for hopf bifurcation calculation.
 * Carries the location of the critical point in the variable pt
 */
public class SourceOrSinkSelected extends ActionEvent
{
	public final Point2D pt;
	public SourceOrSinkSelected(Point2D pt)
	{
		this.pt = pt;
	}
}
