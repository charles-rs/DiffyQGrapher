package Settings;


import javafx.scene.paint.Color;

import java.util.prefs.Preferences;

public class OutPlaneSettings
{
	public boolean writeCriticalText;
	public boolean staticInc;
	public boolean drawAxes;
	public double inc;
	public Color solutionColor;
	public Color isoclineColor;
	public Color horizIsoColor;
	public Color vertIsoColor;
	public Color stblSeparatrixColor;
	public Color unstblSeparatrixColor;
	public Color criticalColor;
	public Color attrLimCycleColor;
	public Color repLimCycleColor;
	public Color divBifConvColor;
	public Color divBifDivColor;
	private static final Color solutionColorDef = Color.BLACK;
	private static final Color isoclineColorDef = Color.BLUE;
	private static final Color horizIsoColorDef = Color.PURPLE;
	private static final Color vertIsoColorDef = Color.ORANGE;
	private static final Color stblSeparatrixColorDef = Color.ORANGERED;
	private static final Color unstblSeparatrixColorDef = Color.DARKCYAN;
	private static final Color criticalColorDef = Color.RED;
	private static final Color attrLimCycleColorDef = Color.GREEN;
	private static final Color repLimCycleColorDef = Color.MAGENTA;
	private static final Color divBifConvColorDef = Color.NAVY;
	private static final Color divBifDivColorDef = Color.HOTPINK;
	private OutPlaneSettings ()
	{}
	void resetColors()
	{
		solutionColor = solutionColorDef;
		isoclineColor = isoclineColorDef;
		horizIsoColor = horizIsoColorDef;
		vertIsoColor = vertIsoColorDef;
		stblSeparatrixColor = stblSeparatrixColorDef;
		unstblSeparatrixColor = unstblSeparatrixColorDef;
		criticalColor = criticalColorDef;
		attrLimCycleColor = attrLimCycleColorDef;
		repLimCycleColor = repLimCycleColorDef;
		divBifConvColor = divBifConvColorDef;
		divBifDivColor = divBifDivColorDef;
	}
	public static OutPlaneSettings fromPrefs()
	{
		OutPlaneSettings temp = new OutPlaneSettings();
		Preferences prefs = Preferences.userNodeForPackage(OutPlaneSettings.class);
		temp.writeCriticalText = prefs.getBoolean("writeCriticalText", true);
		temp.writeCriticalText = true;
		temp.staticInc = prefs.getBoolean("staticInc", false);
		temp.inc = prefs.getDouble("inc", .01);
		temp.solutionColor = Settings.getColor("solutionColor", solutionColorDef, prefs);
		temp.isoclineColor = Settings.getColor("isoclineColor", isoclineColorDef, prefs);
		temp.horizIsoColor = Settings.getColor("horizIsoColor", horizIsoColorDef, prefs);
		temp.vertIsoColor = Settings.getColor("vertIsoColor", vertIsoColorDef, prefs);
		temp.stblSeparatrixColor = Settings.getColor("stblSeparatrixColor", stblSeparatrixColorDef, prefs);
		temp.unstblSeparatrixColor = Settings.getColor("unstblSeparatrixColor", unstblSeparatrixColorDef, prefs);
		temp.criticalColor = Settings.getColor("criticalColor", criticalColorDef, prefs);
		temp.attrLimCycleColor = Settings.getColor("attrLimCycleColor", attrLimCycleColorDef, prefs);
		temp.repLimCycleColor = Settings.getColor("repLimCycleColor", repLimCycleColorDef, prefs);
		temp.divBifDivColor = Settings.getColor("divBifDivColor", divBifDivColorDef, prefs);
		temp.divBifConvColor = Settings.getColor("divBifConvColor", divBifConvColorDef, prefs);
		temp.drawAxes = prefs.getBoolean("drawAxes", true);
		return temp;
	}
	public void write()
	{
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.putBoolean("writeCriticalText", writeCriticalText);
		prefs.putBoolean("staticInc", staticInc);
		prefs.putDouble("inc", inc);
		Settings.putColor("solutionColor", solutionColor, prefs);
		Settings.putColor("isoclineColor", isoclineColor, prefs);
		Settings.putColor("horizIsoColor", horizIsoColor, prefs);
		Settings.putColor("vertIsoColor", vertIsoColor, prefs);
		Settings.putColor("stblSeparatrixColor", stblSeparatrixColor, prefs);
		Settings.putColor("unstblSeparatrixColor", unstblSeparatrixColor, prefs);
		Settings.putColor("criticalColor", criticalColor, prefs);
		Settings.putColor("attrLimCycleColor", attrLimCycleColor, prefs);
		Settings.putColor("repLimCycleColor", repLimCycleColor, prefs);
		Settings.putColor("divBifDivColor", divBifDivColor, prefs);
		Settings.putColor("divBifConvColor", divBifConvColor, prefs);
		prefs.putBoolean("drawAxes", drawAxes);
	}
}
