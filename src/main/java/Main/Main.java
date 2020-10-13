package Main;

import AST.Derivative;
import Evaluation.EvalType;
import Events.SaddleSelected;
import Events.HopfPointSelected;
import Events.UpdatedState;
import Exceptions.SyntaxError;
import FXObjects.ClickModeType;
import FXObjects.DerivativeGraph;
import FXObjects.InClickModeType;
import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import Instr.InstructionsWindow;
import Parser.Tokenizer;
import Settings.Settings;
import Settings.SettingsWindow;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Scanner;
import java.util.prefs.Preferences;

/**
 * absolute fucking mess.
 */

public class Main extends Application
{


	HBox mainH;
	VBox leftBox;
	Tokenizer tokyBoi;
	OutputPlane outPlane;
	private Menu file;
	private Menu options;
	private Menu help;
	private Menu view;
	private Menu draw;
	private Menu bifurcation;
	private Menu language;
	private Menu evalOpt;
	private Menu clickOpt;
	private Menu menDyDt;
	private Menu menDxDt;
	private MenuItem sets;
	private MenuItem saveInpt;
	private MenuItem saveOut;
	private MenuItem quit;
	private MenuItem euler;
	private MenuItem midEuler;
	private MenuItem rungeKutta;
	private MenuItem drawPath;
	private MenuItem findCritical;
	private MenuItem drawIso;
	private MenuItem linearisation;
	private MenuItem separatrices;
	private MenuItem horizIso;
	private MenuItem vertIso;
	private MenuItem pentagram;
	private MenuItem noMorePentagram;
	private MenuItem editPentagram;
	private MenuItem limCycle;
	private MenuItem basin;
	private MenuItem coBasin;
	private MenuItem saddleBif;
	private MenuItem hopfBif;
	private MenuItem sdlConBif;
	private MenuItem cycleBif;
	private Menu divBif;
	private MenuItem positiveDivBif, negativeDivBif;
	private MenuItem info;
	private MenuItem instructions;



	private MenuItem english;
	private MenuItem pirate;
	private MenuItem espanol;
	private MenuItem portugues;

	private Label tLabel;
	private Button btnClearOut;
	private Button resetZoom;
	private Button update;
	private Button btnClearIn;
	private Button btnInterruptSadCon;
	private Button btnResetInZoom;

	private String strEuler, strMidEuler, strRungeKutta;
	public static Language lang;
	public static int instructionCode = -1;
	private Preferences prefs;
	private Stage primaryStage;
	private Settings settings;
	@Override
	public void start(Stage primaryStage) throws Exception
	{

		prefs = Preferences.userNodeForPackage(getClass());

		this.primaryStage = primaryStage;
		try
		{
			lang = Language.fromString(prefs.get("language", ""));
		} catch (NullPointerException | SecurityException n)
		{
			lang = Language.ENGLISH;
		}
		settings = Settings.fromPrefs();
		AnchorPane anchor = new AnchorPane();
		TextArea inputArea = new TextArea();
		inputArea.setText("dx/dt = \ndy/dt = \n");
		inputArea.setPrefRowCount(8);
		inputArea.setPrefColumnCount(20);
		VBox root = new VBox();
		MenuBar bar = new MenuBar();

		file = new Menu();
		options = new Menu();
		help = new Menu();
		view = new Menu();
		draw = new Menu();
		bifurcation = new Menu();
		language = new Menu();

		//MENU ITEMS GO HERE
		/////////////////////////////////////////////////////////////

		saveInpt = new MenuItem();
		saveOut = new MenuItem();
		quit = new MenuItem();
		file.getItems().addAll(saveInpt, saveOut, quit);


		evalOpt = new Menu();
		options.getItems().addAll(evalOpt);


		euler = new MenuItem();
		midEuler = new MenuItem();
		rungeKutta = new MenuItem();
		evalOpt.getItems().addAll(euler, midEuler, rungeKutta);

		sets = new MenuItem();
		clickOpt = new Menu();
		options.getItems().addAll(clickOpt, sets);
		drawPath = new MenuItem();
		findCritical = new MenuItem();
		drawIso = new MenuItem();
		clickOpt.getItems().addAll(drawPath, findCritical, drawIso);

		menDyDt = new Menu();
		menDxDt = new Menu();
		MenuItem itmDxt, itmDyt, itmDxx, itmDyx, itmDxy, itmDyy;
		itmDxt = new MenuItem("t");
		itmDxx = new MenuItem("x");
		itmDxy = new MenuItem("y");
		itmDyt = new MenuItem("t");
		itmDyx = new MenuItem("x");
		itmDyy = new MenuItem("y");
		menDxDt.getItems().addAll(itmDxt, itmDxx, itmDxy);
		menDyDt.getItems().addAll(itmDyt, itmDyx, itmDyy);
		linearisation = new MenuItem();
		view.getItems().addAll(menDxDt, menDyDt, linearisation);

		separatrices = new MenuItem();
		horizIso = new MenuItem();
		vertIso = new MenuItem();
		pentagram = new MenuItem();
		noMorePentagram = new MenuItem();
		editPentagram = new MenuItem();
		limCycle = new MenuItem();
		basin = new MenuItem();
		coBasin = new MenuItem();
		draw.getItems().addAll(separatrices, horizIso, vertIso, new SeparatorMenuItem(),
				pentagram, noMorePentagram, editPentagram, new SeparatorMenuItem(),
				limCycle, basin, coBasin);


		saddleBif = new MenuItem();
		hopfBif = new MenuItem();
		sdlConBif = new MenuItem();
		cycleBif = new MenuItem();
		divBif = new Menu();
		positiveDivBif = new MenuItem();
		negativeDivBif = new MenuItem();
		divBif.getItems().addAll(positiveDivBif, negativeDivBif);
		bifurcation.getItems().addAll(saddleBif, hopfBif, sdlConBif, cycleBif, divBif);

		info = new MenuItem();
		instructions = new MenuItem();
		help.getItems().addAll(info, instructions);

		english = new MenuItem();
		pirate = new MenuItem();
		espanol = new MenuItem();
		portugues = new MenuItem();
		initLangNames();
		language.getItems().addAll(english, pirate, espanol, portugues);
		////////////////////////////////////////////////////////
		bar.getMenus().addAll(file, options, view, draw, bifurcation, help, language);

		mainH = new HBox();
		root.getChildren().addAll(bar, anchor);
		mainH.setSpacing(30);
		leftBox = new VBox();
		leftBox.setSpacing(10);
		Region r = new Region();
		HBox.setHgrow(r, Priority.SOMETIMES);

		HBox aBox, bBox, tBox;
		TextField aField, bField, tField;
		Label aLabel, bLabel;

		aLabel = new Label("a:");
		bLabel = new Label("b:");
		tLabel = new Label();
		aField = new TextField();
		bField = new TextField();
		tField = new TextField();
//		Pane inP, outP;
//		inP = new Pane();
//		outP = new Pane();
		outPlane = new OutputPlane(600, tField, settings.outPlaneSettings);
		InputPlane inPlane = new InputPlane(300, aField, bField, outPlane, settings.inPlaneSettings);
		outPlane.in = inPlane;
		HBox outPButtonBox = new HBox();

		btnClearOut = new Button();
		btnClearOut.setOnAction(actionEvent ->
		{
			outPlane.clear();
		});

		resetZoom = new Button();
		resetZoom.setOnAction(actionEvent ->
		{
			outPlane.resetZoom();
		});
		update = new Button();
		update.setOnAction(actionEvent ->
		{
			outPlane.clearObjects();
			outPlane.draw();
			outPlane.render();
			inPlane.clear();
			inPlane.draw();
			inPlane.render();
		});
		outPButtonBox.getChildren().addAll(btnClearOut, resetZoom, update);
		outPButtonBox.setAlignment(Pos.CENTER);
		outPButtonBox.setSpacing(10);
		HBox inPButtonBox = new HBox();

		btnClearIn = new Button();
		btnClearIn.setOnAction((actionEvent) ->
		{
			inPlane.clear();
		});

		btnInterruptSadCon = new Button();
		btnInterruptSadCon.setOnAction((actionEvent ->
		{
			inPlane.interrupt();
		}));
		btnResetInZoom = new Button();
		btnResetInZoom.setOnAction(actionEvent ->
		{
			inPlane.resetZoom();
		});
		inPButtonBox.getChildren().addAll(btnClearIn, btnInterruptSadCon, btnResetInZoom);
		inPButtonBox.setAlignment(Pos.CENTER);
		inPButtonBox.setSpacing(10);


		aBox = new HBox();
		bBox = new HBox();
		tBox = new HBox();
		aBox.getChildren().addAll(aLabel, aField);
		bBox.getChildren().addAll(bLabel, bField);
		tBox.getChildren().addAll(tLabel, tField);


		leftBox.getChildren().add(inputArea);
		leftBox.getChildren().addAll(aBox, bBox, tBox);
//		leftBox.getChildren().addAll(inP, inPButtonBox);
		leftBox.getChildren().addAll(inPlane, inPButtonBox);
//		inP.getChildren().add(inPlane);
		mainH.getChildren().add(leftBox);
		//mainH.getChildren().add(r);
		VBox rightBox = new VBox();
		rightBox.setSpacing(10);
		rightBox.setAlignment(Pos.TOP_CENTER);

//		rightBox.getChildren().addAll(outP, outPButtonBox);
		rightBox.getChildren().addAll(outPlane, outPButtonBox);
		rightBox.setFillWidth(false);
		leftBox.setFillWidth(false);
//		VBox.setVgrow(outPlane, Priority.NEVER);
//		VBox.setVgrow(inPlane, Priority.SOMETIMES);
//		HBox.setHgrow(leftBox, Priority.SOMETIMES);
//		HBox.setHgrow(rightBox, Priority.SOMETIMES);
		mainH.getChildren().add(rightBox);

//		outP.getChildren().add(outPlane);


		AnchorPane.setRightAnchor(mainH, 20.0);
		AnchorPane.setBottomAnchor(mainH, 20.0);
		AnchorPane.setLeftAnchor(mainH, 20.0);
		AnchorPane.setTopAnchor(mainH, 20.0);

		anchor.getChildren().addAll(mainH);
		TextField [] fields = new TextField[] {aField, bField, tField};
		for(TextField fld : fields)
		{
			fld.setOnMouseClicked(e -> fld.selectAll());
		}

		aField.textProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue.matches("-?[0-9]*(\\.)?[0-9]*"))
				aField.setText(oldValue);
			else
			{
				try
				{
					outPlane.updateA(Double.parseDouble(newValue));
					inPlane.updateA(Double.parseDouble(newValue));
				} catch (Exception e)
				{
					System.out.println("\"" + newValue + "\"");
				}
			}
		});
		bField.textProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue.matches("-?[0-9]*(\\.)?[0-9]*"))
				bField.setText(oldValue);
			else
			{
				try
				{
					outPlane.updateB(Double.parseDouble(newValue));
					inPlane.updateB(Double.parseDouble(newValue));
				} catch (Exception e)
				{
					System.out.println("\"" + newValue + "\"");
				}
			}
		});
		aField.setText("0.0");
		bField.setText("0.0");
		tField.textProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue.matches("-?[0-9]*(\\.)?[0-9]*"))
				tField.setText(oldValue);
			else
			{
				try
				{
					outPlane.updateT(Double.parseDouble(newValue));
				} catch (Exception e)
				{
					System.out.println("\"" + newValue + "\"");
				}
			}
		});
		inputArea.textProperty().addListener((obs, old, newVal) ->
		{
			if(newVal.length() > 0)
			{
				tokyBoi = new Tokenizer(new StringReader(inputArea.getText()));
				while(tokyBoi.hasNext())
				{
					try
					{
						Derivative temp = Parser.Parser.parseDerivative(tokyBoi);
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
//			drawPath.setText(strDrawGraph + " x");
//			findCritical.setText(strFindCritical);
//			drawIso.setText(strDrawIso);
			outPlane.setClickMode(ClickModeType.DRAWPATH);
		});
		findCritical.setOnAction((e) ->
		{
//			findCritical.setText(strFindCritical + " x");
//			drawPath.setText(strDrawGraph);
//			drawIso.setText(strDrawIso);
			outPlane.setClickMode(ClickModeType.FINDCRITICAL);
		});
		drawIso.setOnAction((e) ->
		{
//			drawIso.setText(strDrawIso + " x");
//			drawPath.setText(strDrawGraph);
//			findCritical.setText(strFindCritical);
			outPlane.setClickMode(ClickModeType.DRAWISO);
		});
		root.setOnKeyPressed((k) ->
		{
			if(k.getCode() == KeyCode.T && (k.isControlDown() || k.isMetaDown()))
			{
				switch (outPlane.getClickMode())
				{
					case DRAWPATH:
//						findCritical.setText(strFindCritical + " x");
//						drawPath.setText(strDrawGraph);
						outPlane.setClickMode(ClickModeType.FINDCRITICAL);
						break;
					case FINDCRITICAL:
//						drawPath.setText(strDrawGraph + " x");
//						findCritical.setText(strFindCritical);
						outPlane.setClickMode(ClickModeType.DRAWPATH);
						break;
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
		linearisation.setOnAction(e ->
		{
			outPlane.setClickMode(ClickModeType.LINEARISATION);
		});
		separatrices.setOnAction((e) ->
		{
			outPlane.drawSeparatrices();
		});
		horizIso.setOnAction((e) ->
		{
			outPlane.setClickMode(ClickModeType.DRAWHORIZISO);
		});
		vertIso.setOnAction((e) ->
		{
			outPlane.setClickMode(ClickModeType.DRAWVERTISO);
		});
		pentagram.setOnAction(e ->
		{
			inPlane.setClickMode(InClickModeType.PLACEPENT);
		});
		noMorePentagram.setOnAction(e ->
		{
			inPlane.setClickMode(InClickModeType.REMOVEPENT);
		});
		editPentagram.setOnAction(e ->
		{
			inPlane.setClickMode(InClickModeType.EDITPENT);
		});
		limCycle.setOnAction(e ->
		{
			outPlane.setClickMode(ClickModeType.FINDLIMCYCLE);
		});
		basin.setOnAction(e ->
		{
			outPlane.setClickMode(ClickModeType.DRAWBASIN);
		});
		coBasin.setOnAction(e ->
		{
			outPlane.setClickMode(ClickModeType.DRAWCOBASIN);
		});
		saddleBif.setOnAction((e) ->
		{
			outPlane.setClickMode(ClickModeType.SELECTSADDLE);
		});
		hopfBif.setOnAction((e) ->
		{
			outPlane.setClickMode(ClickModeType.SELECTHOPFPOINT);
		});
		sdlConBif.setOnAction((e) ->
		{
			outPlane.setClickMode(ClickModeType.SELECTSEP);
		});
		cycleBif.setOnAction(e ->
		{
			outPlane.setClickMode(ClickModeType.SEMISTABLE);
		});
		positiveDivBif.setOnAction(e ->
		{
			outPlane.drawDivBif(true);
		});
		negativeDivBif.setOnAction(e ->
		{
			outPlane.drawDivBif(false);
		});
		quit.setOnAction(e ->
		{
			primaryStage.close();
		});
		saveInpt.setOnAction(e ->
		{
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select File");
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			chooser.setInitialFileName("params.png");
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
			File selected = chooser.showSaveDialog(primaryStage);
			if (selected != null)
				inPlane.writePNG(selected);
		});
		saveOut.setOnAction(e ->
		{
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select File");
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			chooser.setInitialFileName("solutions.png");
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
			File selected = chooser.showSaveDialog(primaryStage);
			if (selected != null)
				outPlane.writePNG(selected);
		});
		instructions.setOnAction(e ->
		{
			InstructionsWindow temp = new InstructionsWindow(inPlane, outPlane);
			temp.setX(primaryStage.getWidth());
		});
		sets.setOnAction(e ->
		{
			new SettingsWindow(settings, lang);
			outPlane.updateSettings();
			inPlane.updateSettings();
		});
		english.setOnAction(e ->
		{
			lang = Language.ENGLISH;
			prefs.put("language", lang.toString());
			update();
			InstructionsWindow.update();
			outPlane.fireEvent(new UpdatedState(Main.instructionCode));
		});
		pirate.setOnAction(e ->
		{
			lang = Language.PIRATE;
			prefs.put("language", lang.toString());
			InstructionsWindow.update();
			update();
			outPlane.fireEvent(new UpdatedState(Main.instructionCode));
		});
		espanol.setOnAction(e ->
		{
			lang = Language.SPANISH;
			prefs.put("language", lang.toString());
			update();
			InstructionsWindow.update();
			outPlane.fireEvent(new UpdatedState(Main.instructionCode));
		});
		portugues.setOnAction(e ->
		{
			lang = Language.PORTUGUESE;
			prefs.put("language", lang.toString());
			update();
			InstructionsWindow.update();
			outPlane.fireEvent(new UpdatedState(Main.instructionCode));
		});
		EventHandler<ActionEvent> handler = e ->
		{
			if(e instanceof SaddleSelected)
			{
				if(((SaddleSelected) e).pt != null)
					inPlane.saddleBif(((SaddleSelected) e).pt);
				outPlane.setClickMode(ClickModeType.DRAWPATH);
			} else if(e instanceof HopfPointSelected)
			{
				if(((HopfPointSelected) e).pt != null)
					inPlane.hopfBif(((HopfPointSelected) e).pt);
				outPlane.setClickMode(ClickModeType.DRAWPATH);
			} else if (e instanceof UpdatedState)
			{
				instructionCode = ((UpdatedState) e).code;
			}
		};
		outPlane.addEventHandler(ActionEvent.ANY, handler);
		inPlane.addEventHandler(ActionEvent.ANY, handler);



		primaryStage.setTitle("Differential Equations");
		primaryStage.setScene(new Scene(root, 1200, 800));
		//primaryStage.setMaximized(true);
		update();
		primaryStage.show();
//		System.out.println(outP.getHeight());
	}

	private void initLangNames()
	{
		InputStream in = getClass().getResourceAsStream("default.txt");
		Scanner s = new Scanner(in, StandardCharsets.UTF_8);
		String temp;
		String [] split;
		while(s.hasNext())
		{
			temp = s.nextLine();
			split = temp.split("~");
			switch (split[0])
			{
				case "en":
					english.setText(split[1]);
					break;
				case "es":
					espanol.setText(split[1]);
					break;
				case "pt":
					portugues.setText(split[1]);
					break;
				case "pi":
					pirate.setText(split[1]);
					break;
			}
		}
	}
	private void update()
	{
		InputStream in = Main.class.getResourceAsStream(lang.toString() + ".txt");
		Scanner s = new Scanner(in, StandardCharsets.UTF_8);
		String temp;
		String [] split;
		while(s.hasNext())
		{
			temp = s.nextLine();
			split = temp.split("~");
			switch (split[0])
			{
				case "file":
					file.setText(split[1]);
					break;
				case "options":
					options.setText(split[1]);
					break;
				case "view":
					view.setText(split[1]);
					break;
				case "draw":
					draw.setText(split[1]);
					break;
				case "bifurcation":
					bifurcation.setText(split[1]);
					break;
				case "help":
					help.setText(split[1]);
					break;
				case "language":
					language.setText(split[1]);
					break;
				case "save in":
					saveInpt.setText(split[1]);
					break;
				case "save out":
					saveOut.setText(split[1]);
					break;
				case "quit":
					quit.setText(split[1]);
					break;
				case "title":
					primaryStage.setTitle(split[1]);
					break;
				case "euler":
					strEuler = split[1];
					if(outPlane.evalType == EvalType.Euler)
						euler.setText(strEuler + " x");
					else
						euler.setText(strEuler);
					break;
				case "mideuler":
					strMidEuler = split[1];
					if(outPlane.evalType == EvalType.MidEuler)
						midEuler.setText(strMidEuler + " x");
					else
						midEuler.setText(strMidEuler);
					break;
				case "runge":
					strRungeKutta = split[1];
					if(outPlane.evalType == EvalType.RungeKutta)
						rungeKutta.setText(strRungeKutta + " x");
					else
						rungeKutta.setText(strRungeKutta);
					break;
				case "evalopt":
					evalOpt.setText(split[1]);
					break;
				case "clickopt":
					clickOpt.setText(split[1]);
					break;
				case "drawpath":
					drawPath.setText(split[1]);
					break;
				case "critical":
					findCritical.setText(split[1]);
					break;
				case "isocline":
					drawIso.setText(split[1]);
					break;
				case "dx":
					menDxDt.setText(split[1]);
					break;
				case "dy":
					menDyDt.setText(split[1]);
					break;
				case "lin":
					linearisation.setText(split[1]);
					break;
				case "sep":
					separatrices.setText(split[1]);
					break;
				case "horiz":
					horizIso.setText(split[1]);
					break;
				case "vert":
					vertIso.setText(split[1]);
					break;
				case "pent":
					pentagram.setText(split[1]);
					break;
				case "rmpent":
					noMorePentagram.setText(split[1]);
					break;
				case "editpent":
					editPentagram.setText(split[1]);
					break;
				case "limcyc":
					limCycle.setText(split[1]);
					break;
				case "basin":
					basin.setText(split[1]);
					break;
				case "cobasin":
					coBasin.setText(split[1]);
					break;
				case "saddlenode":
					saddleBif.setText(split[1]);
					break;
				case "hopf":
					hopfBif.setText(split[1]);
					break;
				case "saddlecon":
					sdlConBif.setText(split[1]);
					break;
				case "semistable":
					cycleBif.setText(split[1]);
					break;
				case "divbif":
					divBif.setText(split[1]);
					break;
				case "posdivbif":
					positiveDivBif.setText(split[1]);
					break;
				case "negdivbif":
					negativeDivBif.setText(split[1]);
					break;
				case "info":
					info.setText(split[1]);
					break;
				case "instr":
					instructions.setText(split[1]);
					break;
				case "init":
					tLabel.setText(split[1]);
					break;
				case "clear":
					btnClearIn.setText(split[1]);
					btnClearOut.setText(split[1]);
					break;
				case "reset":
					resetZoom.setText(split[1]);
					btnResetInZoom.setText(split[1]);
					break;
				case "update":
					update.setText(split[1]);
					break;
				case "interrupt":
					btnInterruptSadCon.setText(split[1]);
					break;
				case "settings":
					sets.setText(split[1]);
					break;
			}
		}
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
		graph.render();
	}

	/**
	 * Opens a new window to get new bounds for saddle connections from the user.
	 * If there are number errors, ignores them and leaves that value unchanged.
	 */
	private void getSaddleBoundsAndSet()
	{
		Stage newWindow = new Stage();
		newWindow.setTitle("Set Saddle Connection Bounds");
		VBox mainV = new VBox();
		HBox xMinBox = new HBox();
		HBox xMaxBox = new HBox();
		HBox yMinBox = new HBox();
		HBox yMaxBox = new HBox();
		xMinBox.setSpacing(8);
		xMaxBox.setSpacing(8);
		yMinBox.setSpacing(8);
		yMaxBox.setSpacing(8);
		mainV.setSpacing(8);
		mainV.getChildren().addAll(
				xMinBox, new Separator(),
				xMaxBox, new Separator(),
				yMinBox, new Separator(),
				yMaxBox);
		Label xMinLbl = new Label ("X Min:");
		Label xMaxLbl = new Label ("X Max:");
		Label yMinLbl = new Label ("Y Min:");
		Label yMaxLbl = new Label ("Y Max:");
		TextField xMinField = new TextField();
		TextField xMaxField = new TextField();
		TextField yMinField = new TextField();
		TextField yMaxField = new TextField();
		xMinField.setText(String.valueOf(outPlane.dSaddleXMin));
		xMaxField.setText(String.valueOf(outPlane.dSaddleXMax));
		yMinField.setText(String.valueOf(outPlane.dSaddleYMin));
		yMaxField.setText(String.valueOf(outPlane.dSaddleYMax));

		xMinBox.getChildren().addAll(xMinLbl, xMinField);
		xMaxBox.getChildren().addAll(xMaxLbl, xMaxField);
		yMinBox.getChildren().addAll(yMinLbl, yMinField);
		yMaxBox.getChildren().addAll(yMaxLbl, yMaxField);

		Scene newScene = new Scene(mainV);
		newWindow.setScene(newScene);
		newWindow.addEventHandler(KeyEvent.ANY, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent keyEvent)
			{
				if(keyEvent.getCode() == KeyCode.ENTER)
				{
					newWindow.close();
				}
			}
		});
		newWindow.setOnCloseRequest((e) ->
		{
			try
			{
				outPlane.dSaddleXMax = Double.parseDouble(xMaxField.getText());
			} catch (NumberFormatException ignored) {}
			try
			{
				outPlane.dSaddleXMin = Double.parseDouble(xMinField.getText());
			} catch (NumberFormatException ignored) {}
			try
			{
				outPlane.dSaddleYMax = Double.parseDouble(yMaxField.getText());
			} catch (NumberFormatException ignored) {}
			try
			{
				outPlane.dSaddleYMin = Double.parseDouble(yMinField.getText());
			} catch (NumberFormatException ignored) {}
		});
		newWindow.show();


	}
}
