package Events;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;


public class SaddleSelected extends ActionEvent
{
	public Point2D pt;
	public SaddleSelected(Point2D pt)
	{
		this.pt = pt;
	}
}
