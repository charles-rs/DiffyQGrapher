package Instr;

import Events.UpdatedState;
import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import Main.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
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
		f = InstructionsWindow.class.getResourceAsStream(Main.lang.toString() + ".txt");
		if(f == null) strings.put(-1, "well well well");
		try
		{
			Scanner in = new Scanner(f, StandardCharsets.UTF_8);
			int cd;
			StringBuilder sb;/*
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
			}*/
			String temp;
			String [] split;
			String [] split2;
			while(in.hasNext())
			{
				temp = in.nextLine();
				split = temp.split("~");
				try
				{
					cd = Integer.parseInt(split[0]);
					sb = new StringBuilder();
					split2 = split[1].split("%");
					for(int i = 0; i < split2.length - 1; i++)
					{
						sb.append(split2[i]);
						sb.append("\n");
					}
					sb.append(split2[split2.length - 1]);
					strings.put(cd, sb.toString());
				} catch (NumberFormatException ignored) {}
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
