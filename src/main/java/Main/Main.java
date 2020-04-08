package Main;

import AST.Derivative;
import Evaluation.EvalType;
import Events.SaddleSelected;
import Events.SourceSelected;
import Exceptions.SyntaxError;
import FXObjects.ClickModeType;
import FXObjects.DerivativeGraph;
import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import Parser.Tokenizer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
		inputArea.setText("dx/dt = \ndy/dt = ");
		inputArea.setPrefRowCount(8);
		inputArea.setPrefColumnCount(20);
		VBox root = new VBox();
		MenuBar bar = new MenuBar();
		Menu file = new Menu("File");
		Menu options = new Menu("Options");
		Menu help = new Menu("Help");
		Menu view = new Menu("View");
		Menu draw = new Menu("Draw");
		Menu bifurcation = new Menu("Find Bifurcation");

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
		String strDrawGraph, strFindCritical, strDrawIso;
		strDrawGraph = "Draw Path";
		strFindCritical = "Find Critical Points";
		strDrawIso = "Draw Isocline";
		MenuItem drawPath = new MenuItem(strDrawGraph + " x");
		MenuItem findCritical = new MenuItem(strFindCritical);
		MenuItem drawIso = new MenuItem(strDrawIso);
		clickOpt.getItems().addAll(drawPath, findCritical, drawIso);

		Menu menDyDt = new Menu("dy/dt vs");
		Menu menDxDt = new Menu("dx/dt vs");
		MenuItem itmDxt, itmDyt, itmDxx, itmDyx, itmDxy, itmDyy;
		itmDxt = new MenuItem("t");
		itmDxx = new MenuItem("x");
		itmDxy = new MenuItem("y");
		itmDyt = new MenuItem("t");
		itmDyx = new MenuItem("x");
		itmDyy = new MenuItem("y");
		menDxDt.getItems().addAll(itmDxt, itmDxx, itmDxy);
		menDyDt.getItems().addAll(itmDyt, itmDyx, itmDyy);
		view.getItems().addAll(menDxDt, menDyDt);

		MenuItem separatrices = new MenuItem("Separatrices");
		MenuItem horizIso = new MenuItem("Horizontal Isocline");
		MenuItem vertIso = new MenuItem("Vertical Isocline");
		draw.getItems().addAll(separatrices, horizIso, vertIso);


		MenuItem saddleBif = new MenuItem("Saddle Node Bifurcation");
		MenuItem hopfBif = new MenuItem("Hopf Bifurcation");
		MenuItem sdlConBif = new MenuItem("Saddle Connection Bifurcation");
		bifurcation.getItems().addAll(saddleBif, hopfBif, sdlConBif);
		////////////////////////////////////////////////////////
		bar.getMenus().addAll(file, options, view, draw, bifurcation, help);

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
		OutputPlane outPlane = new OutputPlane(600, tField);
		InputPlane inPlane = new InputPlane(300, aField, bField, outPlane);
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
			inPlane.clear();
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
			drawIso.setText(strDrawIso);
			outPlane.clickMode = ClickModeType.DRAWPATH;
		});
		findCritical.setOnAction((e) ->
		{
			findCritical.setText(strFindCritical + " x");
			drawPath.setText(strDrawGraph);
			drawIso.setText(strDrawIso);
			outPlane.clickMode = ClickModeType.FINDCRITICAL;
		});
		drawIso.setOnAction((e) ->
		{
			drawIso.setText(strDrawIso + " x");
			drawPath.setText(strDrawGraph);
			findCritical.setText(strFindCritical);
			outPlane.clickMode = ClickModeType.DRAWISO;
		});
		root.setOnKeyPressed((k) ->
		{
			if(k.getCode() == KeyCode.T && k.isControlDown())
			{
				switch (outPlane.clickMode)
				{
					case DRAWPATH:
						findCritical.setText(strFindCritical + " x");
						drawPath.setText(strDrawGraph);
						outPlane.clickMode = ClickModeType.FINDCRITICAL;
						break;
					case FINDCRITICAL:
						drawPath.setText(strDrawGraph);
						drawIso.setText(strDrawIso + " x");
						findCritical.setText(strFindCritical);
						outPlane.clickMode = ClickModeType.DRAWISO;
						break;
					case DRAWISO:
						drawIso.setText(strDrawIso);
						findCritical.setText(strFindCritical);
						drawPath.setText(strDrawGraph + " x");
						outPlane.clickMode = ClickModeType.DRAWPATH;
				}
			}
		});
		itmDxt.setOnAction((e) ->
		{
			openSecondary(outPlane.getDx(), 't', inPlane.getA(), inPlane.getB(), 0, 0, 0);
		});
		itmDxx.setOnAction((e) ->
		{
			openSecondary(outPlane.getDx(), 'x', inPlane.getA(), inPlane.getB(), 0, 0, 0);
		});
		itmDxy.setOnAction((e) ->
		{
			openSecondary(outPlane.getDx(), 'y', inPlane.getA(), inPlane.getB(), 0, 0, 0);
		});
		itmDyt.setOnAction((e) ->
		{
			openSecondary(outPlane.getDy(), 't', inPlane.getA(), inPlane.getB(), 0, 0, 0);
		});
		itmDyx.setOnAction((e) ->
		{
			openSecondary(outPlane.getDy(), 'x', inPlane.getA(), inPlane.getB(), 0, 0, 0);
		});
		itmDyy.setOnAction((e) ->
		{
			openSecondary(outPlane.getDy(), 'y', inPlane.getA(), inPlane.getB(), 0, 0, 0);
		});
		separatrices.setOnAction((e) ->
		{
			outPlane.drawSeparatrices();
		});
		horizIso.setOnAction((e) ->
		{
			outPlane.clickMode = ClickModeType.DRAWHORIZISO;
		});
		vertIso.setOnAction((e) ->
		{
			outPlane.clickMode = ClickModeType.DRAWVERTISO;
		});
		saddleBif.setOnAction((e) ->
		{
			outPlane.clickMode = ClickModeType.SELECTSADDLE;

		});
		hopfBif.setOnAction((e) ->
		{
			outPlane.clickMode = ClickModeType.SELECTSOURCE;
		});
		sdlConBif.setOnAction((e) ->
		{
			outPlane.clickMode = ClickModeType.DRAWSEG;
		});
		EventHandler<ActionEvent> handler = e ->
		{
			if(e instanceof SaddleSelected)
			{
				if(((SaddleSelected) e).pt != null)
					inPlane.saddleBif(((SaddleSelected) e).pt);
				outPlane.clickMode = ClickModeType.DRAWPATH;
			}
			if(e instanceof SourceSelected)
			{
				if(((SourceSelected) e).pt != null)
					inPlane.hopfBif(((SourceSelected) e).pt);
				outPlane.clickMode = ClickModeType.DRAWPATH;
			}
		};
		outPlane.addEventHandler(ActionEvent.ANY, handler);



		primaryStage.setTitle("Differential Equations");
		primaryStage.setScene(new Scene(root, 1200, 800));

		//primaryStage.setMaximized(true);
		primaryStage.show();
	}


	public static void main(String[] args)
	{
		launch(args);
	}


	private void openSecondary(Derivative n, char var, double a, double b, double x, double y, double t)
	{
		Stage newWindow = new Stage();
		newWindow.setTitle("d" + n.getType() + "/dt vs " + var);
		TextField xInput = new TextField();
		TextField yInput = new TextField();
		TextField tInput = new TextField();
		xInput.setText(String.valueOf(x));
		yInput.setText(String.valueOf(y));
		tInput.setText(String.valueOf(t));
		Label xLabel = new Label("Enter x value:");
		Label yLabel = new Label("Enter y value:");
		Label tLabel = new Label("Enter t value:");
		HBox mainH = new HBox();
		VBox inputBox = new VBox();
		DerivativeGraph graph = new DerivativeGraph(300, n, var, a, b, x, y, t, xInput, yInput, tInput);
		HBox xBox = new HBox();
		HBox yBox = new HBox();
		HBox tBox = new HBox();
		xBox.getChildren().addAll(xLabel, xInput);
		yBox.getChildren().addAll(yLabel, yInput);
		tBox.getChildren().addAll(tLabel, tInput);
		inputBox.getChildren().addAll(xBox, yBox, tBox);
		mainH.getChildren().addAll(inputBox, graph);
		Scene newScene = new Scene(mainH);
		newWindow.setScene(newScene);
		newWindow.show();
		graph.draw();

	}
}
