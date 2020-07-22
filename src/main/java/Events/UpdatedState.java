package Events;


import javafx.event.ActionEvent;
import javafx.event.EventType;

public class UpdatedState extends ActionEvent
{
	public final int code;
	public static final EventType<UpdatedState> UPDATE;
	public UpdatedState(int cd)
	{
		code = cd;
	}
	static
	{
		 UPDATE = new EventType<>(ActionEvent.ANY);
	}
}
