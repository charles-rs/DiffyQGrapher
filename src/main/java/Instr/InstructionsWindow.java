package Instr;

import Events.UpdatedState;
import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import Main.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;


public class InstructionsWindow extends Stage
{
	private TextArea text;

	static HashMap<Integer, String> strings;
	private InstructionsWindow(){}
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
		String file = "Instr" + Main.lang.toString() + ".txt";
		f = (new InstructionsWindow()).getClass().getResourceAsStream(Main.lang.toString() + ".txt");

		try
		{
			Scanner in = new Scanner(f, StandardCharsets.UTF_8);
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
			strings.put(-1, "oops");
			System.out.println("file not found");
		}
	}
	static
	{
		update();
	}
}
