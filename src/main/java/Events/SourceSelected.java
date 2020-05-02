package Events;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;

public class SourceSelected extends ActionEvent
{
	public Point2D pt;
	public SourceSelected(Point2D pt)
	{
		this.pt = pt;
	}
}
