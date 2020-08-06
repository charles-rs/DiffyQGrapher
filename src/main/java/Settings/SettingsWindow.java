package Settings;

import Instr.InstructionsWindow;
import Main.Language;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SettingsWindow extends Stage
{
	CheckBox staticInc;
	Label lblInc, lbltDist;
	TextField fldInc, fldTDist;
	CheckBox boxWriteCrit, boxDrawPent, boxDrawAxesIn, boxDrawAxesOut;
	Button save, resetOut, resetIn;
	Settings settings;
	ColorPicker solPick, isoPick, horizIsoPick, vertisoPick, stblSepPick,
			unstbleSepPick, criticalPick, attrCycPick, repCycPick,
			saddleNodePick, hopfPick, homoSaddleConPick, heteroSaddleConPick, semiStablePick,
			divDivPick, divConvPick;
	Label lblSolPick, lblIsoPick, lblHorizIsoPick, lblVertIsoPick, lblStblSepPick,
			lblUnstbleSepPick, lblCriticalPick, lblAttrCycPick, lblRepCycPick,
			lblSaddleNodePick, lblHopfPick, lblHomoSaddleConPick, lblHeteroSaddleConPick, lblSemiStablePick,
			lblDivDivPick, lblDivConvPick;

	public SettingsWindow(Settings s, Language l)
	{
		this.settings = s;
		GridPane main = new GridPane();
		main.setPadding(new Insets(15));
		main.setVgap(15);
		main.setHgap(15);
		staticInc = new CheckBox();
		staticInc.setAllowIndeterminate(false);
		staticInc.setSelected(s.outPlaneSettings.staticInc);
		GridPane.setConstraints(staticInc, 0, 1);
		lblInc = new Label();
		fldInc = new TextField(String.valueOf(s.outPlaneSettings.inc));
		GridPane.setConstraints(lblInc, 0, 2);
		GridPane.setConstraints(fldInc, 1, 2);
		fldInc.setDisable(!s.outPlaneSettings.staticInc);
		fldInc.disableProperty().bind(staticInc.selectedProperty().not());
		lbltDist = new Label();
		fldTDist = new TextField(String.valueOf(s.outPlaneSettings.tDist));
		GridPane.setConstraints(lbltDist, 0, 3);
		GridPane.setConstraints(fldTDist, 1, 3);
		boxWriteCrit = new CheckBox();
		boxWriteCrit.setAllowIndeterminate(false);
		boxWriteCrit.setSelected(s.outPlaneSettings.writeCriticalText);
		GridPane.setConstraints(boxWriteCrit, 0, 4);
		boxDrawAxesOut = new CheckBox();
		boxDrawAxesOut.setAllowIndeterminate(false);
		boxDrawAxesOut.setSelected(s.outPlaneSettings.drawAxes);
		GridPane.setConstraints(boxDrawAxesOut, 0, 0);
		boxDrawPent = new CheckBox();
		boxDrawPent.setAllowIndeterminate(false);
		boxDrawPent.setSelected(s.inPlaneSettings.drawPent);
		GridPane.setConstraints(boxDrawPent, 3, 0);
		boxDrawAxesIn = new CheckBox();
		boxDrawAxesIn.setAllowIndeterminate(false);
		boxDrawAxesIn.setSelected(s.inPlaneSettings.drawAxes);
		GridPane.setConstraints(boxDrawAxesIn, 3, 1);
		save = new Button();
		save.setOnAction(e ->
		{
			save();
		});
		GridPane.setConstraints(save, 5, 16);
		solPick = new ColorPicker();
		lblSolPick = new Label();
		GridPane.setConstraints(solPick, 1, 15);
		GridPane.setConstraints(lblSolPick, 0, 15);
		isoPick = new ColorPicker();
		lblIsoPick = new Label();
		GridPane.setConstraints(isoPick, 1, 5);
		GridPane.setConstraints(lblIsoPick, 0, 5);
		horizIsoPick = new ColorPicker();
		lblHorizIsoPick = new Label();
		GridPane.setConstraints(horizIsoPick, 1, 6);
		GridPane.setConstraints(lblHorizIsoPick, 0, 6);
		vertisoPick = new ColorPicker();
		lblVertIsoPick = new Label();
		GridPane.setConstraints(vertisoPick, 1, 7);
		GridPane.setConstraints(lblVertIsoPick, 0, 7);
		stblSepPick = new ColorPicker();
		lblStblSepPick = new Label();
		GridPane.setConstraints(stblSepPick, 1, 8);
		GridPane.setConstraints(lblStblSepPick, 0, 8);
		unstbleSepPick = new ColorPicker();
		lblUnstbleSepPick = new Label();
		GridPane.setConstraints(unstbleSepPick, 1, 9);
		GridPane.setConstraints(lblUnstbleSepPick, 0, 9);
		criticalPick = new ColorPicker();
		lblCriticalPick = new Label();
		GridPane.setConstraints(criticalPick, 1, 10);
		GridPane.setConstraints(lblCriticalPick, 0, 10);
		attrCycPick = new ColorPicker();
		lblAttrCycPick = new Label();
		GridPane.setConstraints(attrCycPick, 1, 11);
		GridPane.setConstraints(lblAttrCycPick, 0, 11);
		repCycPick = new ColorPicker();
		lblRepCycPick = new Label();
		GridPane.setConstraints(repCycPick, 1, 12);
		GridPane.setConstraints(lblRepCycPick, 0, 12);
		divConvPick = new ColorPicker();
		lblDivConvPick = new Label();
		GridPane.setConstraints(divConvPick, 1, 13);
		GridPane.setConstraints(lblDivConvPick, 0, 13);
		divDivPick = new ColorPicker();
		lblDivDivPick = new Label();
		GridPane.setConstraints(divDivPick, 1, 14);
		GridPane.setConstraints(lblDivDivPick, 0, 14);

		saddleNodePick = new ColorPicker();
		lblSaddleNodePick = new Label();
		GridPane.setConstraints(saddleNodePick, 3, 3);
		GridPane.setConstraints(lblSaddleNodePick, 2, 3);
		hopfPick = new ColorPicker();
		lblHopfPick = new Label();
		GridPane.setConstraints(hopfPick, 3, 4);
		GridPane.setConstraints(lblHopfPick, 2, 4);
		homoSaddleConPick = new ColorPicker();
		lblHomoSaddleConPick = new Label();
		GridPane.setConstraints(homoSaddleConPick, 3, 5);
		GridPane.setConstraints(lblHomoSaddleConPick, 2, 5);
		heteroSaddleConPick = new ColorPicker();
		lblHeteroSaddleConPick = new Label();
		GridPane.setConstraints(heteroSaddleConPick, 3, 6);
		GridPane.setConstraints(lblHeteroSaddleConPick, 2, 6);
		semiStablePick = new ColorPicker();
		lblSemiStablePick = new Label();
		GridPane.setConstraints(semiStablePick, 3, 7);
		GridPane.setConstraints(lblSemiStablePick, 2, 7);

		resetOut = new Button();
		GridPane.setConstraints(resetOut, 1, 16);
		resetOut.setOnAction(e ->
		{
			settings.outPlaneSettings.resetColors();
			updateColorPickers();
		});
		resetIn = new Button();
		GridPane.setConstraints(resetIn, 3, 8);
		resetIn.setOnAction(e ->
		{
			settings.inPlaneSettings.resetColors();
			updateColorPickers();
		});


		updateColorPickers();

		main.getChildren().addAll(staticInc, lblInc, fldInc, boxWriteCrit, boxDrawAxesOut, lbltDist, fldTDist,
				save, resetOut, resetIn, boxDrawAxesIn, boxDrawPent,
				solPick, isoPick, horizIsoPick, vertisoPick, stblSepPick,
				unstbleSepPick, criticalPick, attrCycPick, repCycPick,
				saddleNodePick, hopfPick, homoSaddleConPick, heteroSaddleConPick, semiStablePick,
				divConvPick, divDivPick,
				lblHorizIsoPick, lblVertIsoPick, lblSolPick, lblIsoPick, lblStblSepPick,
				lblUnstbleSepPick, lblCriticalPick, lblAttrCycPick, lblRepCycPick,
				lblSaddleNodePick, lblHopfPick, lblHomoSaddleConPick, lblHeteroSaddleConPick, lblSemiStablePick,
				lblDivConvPick, lblDivDivPick);

		setTexts(l);
		this.addEventHandler(KeyEvent.KEY_PRESSED, k ->
		{
			if(k.getCode() == KeyCode.S && (k.isControlDown() || k.isMetaDown()))
				save();
		});
		setScene(new Scene(main));
		showAndWait();
	}
	private void updateColorPickers()
	{
		solPick.setValue(settings.outPlaneSettings.solutionColor);
		isoPick.setValue(settings.outPlaneSettings.isoclineColor);
		horizIsoPick.setValue(settings.outPlaneSettings.horizIsoColor);
		vertisoPick.setValue(settings.outPlaneSettings.vertIsoColor);
		stblSepPick.setValue(settings.outPlaneSettings.stblSeparatrixColor);
		unstbleSepPick.setValue(settings.outPlaneSettings.unstblSeparatrixColor);
		criticalPick.setValue(settings.outPlaneSettings.criticalColor);
		attrCycPick.setValue(settings.outPlaneSettings.attrLimCycleColor);
		repCycPick.setValue(settings.outPlaneSettings.repLimCycleColor);
		divConvPick.setValue(settings.outPlaneSettings.divBifConvColor);
		divDivPick.setValue(settings.outPlaneSettings.divBifDivColor);

		saddleNodePick.setValue(settings.inPlaneSettings.saddleBifColor);
		hopfPick.setValue(settings.inPlaneSettings.hopfBifColor);
		homoSaddleConPick.setValue(settings.inPlaneSettings.homoSaddleConColor);
		heteroSaddleConPick.setValue(settings.inPlaneSettings.heteroSaddleConColor);
		semiStablePick.setValue(settings.inPlaneSettings.semiStableColor);
	}
	private void save()
	{
		settings.outPlaneSettings.writeCriticalText = boxWriteCrit.isSelected();
		settings.outPlaneSettings.staticInc = staticInc.isSelected();
		settings.outPlaneSettings.drawAxes = boxDrawAxesOut.isSelected();
		settings.inPlaneSettings.drawPent = boxDrawPent.isSelected();
		settings.inPlaneSettings.drawAxes = boxDrawAxesIn.isSelected();
		try
		{
			settings.outPlaneSettings.inc = Double.parseDouble(fldInc.getText());
		} catch (NumberFormatException ignored) {}
		try
		{
			settings.outPlaneSettings.tDist = Double.parseDouble(fldTDist.getText());
		} catch (NumberFormatException ignored) {}
		settings.outPlaneSettings.solutionColor = solPick.getValue();
		settings.outPlaneSettings.isoclineColor = isoPick.getValue();
		settings.outPlaneSettings.horizIsoColor = horizIsoPick.getValue();
		settings.outPlaneSettings.vertIsoColor = vertisoPick.getValue();
		settings.outPlaneSettings.stblSeparatrixColor = stblSepPick.getValue();
		settings.outPlaneSettings.unstblSeparatrixColor = unstbleSepPick.getValue();
		settings.outPlaneSettings.criticalColor = criticalPick.getValue();
		settings.outPlaneSettings.attrLimCycleColor = attrCycPick.getValue();
		settings.outPlaneSettings.repLimCycleColor = repCycPick.getValue();
		settings.outPlaneSettings.divBifConvColor = divConvPick.getValue();
		settings.outPlaneSettings.divBifDivColor = divDivPick.getValue();

		settings.inPlaneSettings.saddleBifColor = saddleNodePick.getValue();
		settings.inPlaneSettings.hopfBifColor = hopfPick.getValue();
		settings.inPlaneSettings.homoSaddleConColor = homoSaddleConPick.getValue();
		settings.inPlaneSettings.heteroSaddleConColor = heteroSaddleConPick.getValue();
		settings.inPlaneSettings.semiStableColor = semiStablePick.getValue();

		settings.write();
		close();
	}
	private void setTexts(Language l)
	{
		InputStream in = Settings.class.getResourceAsStream(l.toString() + ".txt");
		Scanner s = new Scanner(in, StandardCharsets.UTF_8);
		String temp;
		String [] split;
		while(s.hasNext())
		{
			temp = s.nextLine();
			split = temp.split("~");
			if(split.length == 2)
			switch (split[0])
			{
				case "staticinc":
					staticInc.setText(split[1]);
					break;
				case "inc":
					lblInc.setText(split[1]);
					break;
				case "writecrit":
					boxWriteCrit.setText(split[1]);
					break;
				case "savepent":
					boxDrawPent.setText(split[1]);
					break;
				case "axesout":
					boxDrawAxesOut.setText(split[1]);
					break;
				case "axesin":
					boxDrawAxesIn.setText(split[1]);
					break;
				case "save":
					save.setText(split[1]);
					break;
				case "title":
					setTitle(split[1]);
					break;
				case "solpick":
					solPick.setPromptText(split[1]);
					lblSolPick.setText(split[1]);
					break;
				case "isopick":
					isoPick.setPromptText(split[1]);
					lblIsoPick.setText(split[1]);
					break;
				case "horizisopick":
					horizIsoPick.setPromptText(split[1]);
					lblHorizIsoPick.setText(split[1]);
					break;
				case "vertisopick":
					vertisoPick.setPromptText(split[1]);
					lblVertIsoPick.setText(split[1]);
					break;
				case "stablepick":
					stblSepPick.setPromptText(split[1]);
					lblStblSepPick.setText(split[1]);
					break;
				case "unstablepick":
					unstbleSepPick.setPromptText(split[1]);
					lblUnstbleSepPick.setText(split[1]);
					break;
				case "criticalpick":
					criticalPick.setPromptText(split[1]);
					lblCriticalPick.setText(split[1]);
					break;
				case "attrcycpick":
					attrCycPick.setPromptText(split[1]);
					lblAttrCycPick.setText(split[1]);
					break;
				case "repcycpick":
					repCycPick.setPromptText(split[1]);
					lblRepCycPick.setText(split[1]);
					break;
				case "resetout":
					resetOut.setText(split[1]);
					break;
				case "saddlenode":
					saddleNodePick.setPromptText(split[1]);
					lblSaddleNodePick.setText(split[1]);
					break;
				case "hopf":
					hopfPick.setPromptText(split[1]);
					lblHopfPick.setText(split[1]);
					break;
				case "homocon":
					homoSaddleConPick.setPromptText(split[1]);
					lblHomoSaddleConPick.setText(split[1]);
					break;
				case "heterocon":
					heteroSaddleConPick.setPromptText(split[1]);
					lblHeteroSaddleConPick.setText(split[1]);
					break;
				case "semistable":
					semiStablePick.setPromptText(split[1]);
					lblSemiStablePick.setText(split[1]);
					break;
				case "divconv":
					divConvPick.setPromptText(split[1]);
					lblDivConvPick.setText(split[1]);
					break;
				case "divdiv":
					divDivPick.setPromptText(split[1]);
					lblDivDivPick.setText(split[1]);
					break;
				case "resetin":
					resetIn.setText(split[1]);
					break;
				case "tdist":
					lbltDist.setText(split[1]);
					break;
			}
		}
	}

}
