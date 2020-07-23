package Main;

import Events.UpdatedState;
import FXObjects.CoordPlane;
import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;


public class InstructionsWindow extends Stage
{
	private TextArea text;

	static HashMap<Integer, String> strings;
	public InstructionsWindow(InputPlane ip, OutputPlane op)
	{
		Pane p = new Pane();
		text = new TextArea();
		p.getChildren().addAll(text);
		text.prefWidthProperty().bind(widthProperty());
		text.prefHeightProperty().bind(heightProperty());
		text.setWrapText(true);

		setScene(new Scene(p));
		EventHandler<ActionEvent> handle = event ->
		{
			if(event instanceof UpdatedState)
			{
				text.setText(strings.get(((UpdatedState) event).code));
			}
		};
		text.setText(strings.get(Main.instructionCode));
		setWidth(500);
		setHeight(300);
		op.addEventHandler(ActionEvent.ANY, handle);
		ip.addEventHandler(ActionEvent.ANY, handle);
		setTitle("Instructions");
		show();
	}
	public static void update()
	{
		strings = new HashMap<>();
		InputStream f;
		switch (Main.lang)
		{
			case PIRATE:
				f = Main.class.getResourceAsStream("Instr/pi.txt");
				break;
			case ENGLISH:
			default:
				f = Main.class.getResourceAsStream("Instr/en.txt");
		}
		try
		{
			Scanner in = new Scanner(f);
			int cd;
			StringBuilder sb;
			in.useDelimiter("\n");
			while(in.hasNext())
			{
				sb = new StringBuilder();

				cd = in.nextInt();
				String tp = in.next();
				while(!tp.equals("~"))
				{
					sb.append(tp);
					sb.append("\n");
					tp = in.next();
				}
				strings.put(cd, sb.toString());
			}
			in.close();
		} catch (Exception ignored)
		{
			System.out.println("file not found");
		}
	}
	static
	{
		update();
	}
}
