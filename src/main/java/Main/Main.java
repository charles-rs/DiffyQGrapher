package Main;

import AST.Derivative;
import Evaluation.EvalType;
import Exceptions.SyntaxError;
import FXObjects.ClickModeType;
import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import Parser.Tokenizer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.StringReader;


public class Main extends Application
{


	HBox mainH;
	VBox leftBox;
	Tokenizer tokyBoi;
	@Override
	public void start(Stage primaryStage) throws Exception
	{

		AnchorPane anchor = new AnchorPane();
		TextArea inputArea = new TextArea();
		inputArea.setText("dy/dt = \ndx/dt = ");
		inputArea.setPrefRowCount(8);
		inputArea.setPrefColumnCount(20);
		VBox root = new VBox();
		MenuBar bar = new MenuBar();
		Menu file = new Menu("File");
		Menu options = new Menu("Options");
		Menu help = new Menu("Help");

		//MENU ITEMS GO HERE
		/////////////////////////////////////////////////////////////
		Menu evalOpt = new Menu("Change Evaluator");
		options.getItems().addAll(evalOpt);
		String strEuler, strMidEuler, strRungeKutta;
		strEuler = "Euler's Method";
		strRungeKutta = "Runge Kutta Method";
		strMidEuler = "Midpoint Euler's Method";
		MenuItem euler = new MenuItem(strEuler);
		MenuItem midEuler = new MenuItem(strMidEuler);
		MenuItem rungeKutta = new MenuItem(strRungeKutta + "  x");
		evalOpt.getItems().addAll(euler, midEuler, rungeKutta);

		Menu clickOpt = new Menu("Change Mode");
		options.getItems().addAll(clickOpt);
		String strDrawGraph, strFindCritical;
		strDrawGraph = "Draw Path";
		strFindCritical = "Find Critical Points";
		MenuItem drawPath = new MenuItem(strDrawGraph + " x");
		MenuItem findCritical = new MenuItem(strFindCritical);
		clickOpt.getItems().addAll(drawPath, findCritical);
		////////////////////////////////////////////////////////
		bar.getMenus().addAll(file, options, help);

		mainH = new HBox();
		root.getChildren().addAll(bar, anchor);
		mainH.setSpacing(30);
		leftBox = new VBox();
		leftBox.setSpacing(10);
		Region r = new Region();
		HBox.setHgrow(r, Priority.SOMETIMES);

		HBox aBox, bBox, tBox;
		TextField aField, bField, tField;
		Label aLabel, bLabel, tLabel;
		aLabel = new Label("a:");
		bLabel = new Label("b:");
		tLabel = new Label("Initial t:");
		aField = new TextField();
		bField = new TextField();
		tField = new TextField();
		Pane inP, outP;
		inP = new Pane();
		outP = new Pane();
		InputPlane inPlane = new InputPlane(300, aField, bField);
		OutputPlane outPlane = new OutputPlane(600, tField);
		HBox buttonBox = new HBox();
		Button clearOut = new Button("Clear");
		clearOut.setOnAction(actionEvent ->
		{
			outPlane.clear();
		});
		Button resetZoom = new Button("Reset Zoom");
		resetZoom.setOnAction(actionEvent ->
		{
			outPlane.resetZoom();
		});
		Button update = new Button("Update Graph");
		update.setOnAction(actionEvent ->
		{
			outPlane.clearObjects();
			outPlane.draw();
		});
		buttonBox.getChildren().addAll(clearOut, resetZoom, update);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setSpacing(10);


		aBox = new HBox();
		bBox = new HBox();
		tBox = new HBox();
		aBox.getChildren().addAll(aLabel, aField);
		bBox.getChildren().addAll(bLabel, bField);
		tBox.getChildren().addAll(tLabel, tField);


		leftBox.getChildren().add(inputArea);
		leftBox.getChildren().addAll(aBox, bBox, tBox);
		leftBox.getChildren().add(inP);
		inP.getChildren().add(inPlane);
		mainH.getChildren().add(leftBox);
		//mainH.getChildren().add(r);
		VBox rightBox = new VBox();
		rightBox.setSpacing(10);
		rightBox.setAlignment(Pos.TOP_CENTER);
		rightBox.getChildren().addAll(outP, buttonBox);
		mainH.getChildren().add(rightBox);
		outP.getChildren().add(outPlane);


		AnchorPane.setRightAnchor(mainH, 20.0);
		AnchorPane.setBottomAnchor(mainH, 20.0);
		AnchorPane.setLeftAnchor(mainH, 20.0);
		AnchorPane.setTopAnchor(mainH, 20.0);

		anchor.getChildren().addAll(mainH);


		aField.textProperty().addListener((obs, s, t1) ->
		{
			try
			{
				outPlane.updateA(Double.parseDouble(t1));
			} catch (NumberFormatException ignored) {}
		});
		bField.textProperty().addListener((obs, s, t1) ->
		{
			try
			{
				outPlane.updateB(Double.parseDouble(t1));
			} catch (NumberFormatException ignored) {}
		});

		inputArea.textProperty().addListener((obs, old, newVal) ->
		{
			if(newVal.length() > 0 && newVal.charAt(newVal.length() - 1) == '\n')
			{
				tokyBoi = new Tokenizer(new StringReader(inputArea.getText()));
				while(tokyBoi.hasNext())
				{
					try
					{
						Derivative temp = Parser.Parser.parseDerivative(tokyBoi);
						System.out.println(temp.prettyPrint(new StringBuilder()));
						switch (temp.getType())
						{
							case 'y':
								outPlane.updateDY(temp);
								break;
							case 'x':
								outPlane.updateDX(temp);
								break;
						}
					} catch (SyntaxError syntaxError)
					{
						//inputArea.setText(inputArea.getText() + "\n\n" + syntaxError.getMessage());
					}
				}
			}
		});


		euler.setOnAction((e) ->
		{
			euler.setText(strEuler + " x");
			midEuler.setText(strMidEuler);
			rungeKutta.setText(strRungeKutta);
			outPlane.evalType = EvalType.Euler;
		});

		midEuler.setOnAction((e) ->
		{
			euler.setText(strEuler);
			midEuler.setText(strMidEuler + "  x");
			rungeKutta.setText(strRungeKutta);
			outPlane.evalType = EvalType.MidEuler;
		});

		rungeKutta.setOnAction((e) ->
		{
			euler.setText(strEuler);
			midEuler.setText(strMidEuler);
			rungeKutta.setText(strRungeKutta + "  x");
			outPlane.evalType = EvalType.RungeKutta;
		});
		drawPath.setOnAction((e) ->
		{
			drawPath.setText(strDrawGraph + " x");
			findCritical.setText(strFindCritical);
			outPlane.clickMode = ClickModeType.DRAWPATH;
		});
		findCritical.setOnAction((e) ->
		{
			findCritical.setText(strFindCritical + " x");
			drawPath.setText(strDrawGraph);
			outPlane.clickMode = ClickModeType.FINDCRITICAL;
		});





		primaryStage.setTitle("Differential Equations");
		primaryStage.setScene(new Scene(root, 1200, 800));

		//primaryStage.setMaximized(true);
		primaryStage.show();
	}


	public static void main(String[] args)
	{
		launch(args);
	}
}
