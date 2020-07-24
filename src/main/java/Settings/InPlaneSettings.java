package Settings;

import javafx.scene.paint.Color;

import java.util.prefs.Preferences;

public class InPlaneSettings
{
	private static final Color saddleBifColorDef = Color.BLUE;
	private static final Color hopfBifColorDef = Color.DARKRED;
	private static final Color homoSaddleConColorDef = Color.PURPLE;
	private static final Color heteroSaddleConColorDef = Color.GREEN;
	private static final Color semiStableColorDef = Color.TURQUOISE;
	public Color saddleBifColor;
	public Color hopfBifColor;
	public Color homoSaddleConColor;
	public Color heteroSaddleConColor;
	public Color semiStableColor;
	public boolean drawPent, drawAxes;
	private InPlaneSettings()
	{}
	void resetColors()
	{
		saddleBifColor = saddleBifColorDef;
		hopfBifColor = hopfBifColorDef;
		homoSaddleConColor = homoSaddleConColorDef;
		heteroSaddleConColor = heteroSaddleConColorDef;
		semiStableColor = semiStableColorDef;
	}
	public static InPlaneSettings fromPrefs()
	{
		InPlaneSettings temp =  new InPlaneSettings();
		Preferences prefs = Preferences.userNodeForPackage(InPlaneSettings.class);
		temp.saddleBifColor = Settings.getColor("saddleBifColor", saddleBifColorDef, prefs);
		temp.hopfBifColor = Settings.getColor("hopfBifColor", hopfBifColorDef, prefs);
		temp.homoSaddleConColor = Settings.getColor("homoSaddleConColor", homoSaddleConColorDef, prefs);
		temp.heteroSaddleConColor = Settings.getColor("heteroSaddleConColor", heteroSaddleConColorDef, prefs);
		temp.semiStableColor = Settings.getColor("semiStableColor", semiStableColorDef, prefs);
		temp.drawPent = prefs.getBoolean("drawPent", true);
		temp.drawAxes = prefs.getBoolean("drawAxes", true);
		return temp;
	}
	public void write()
	{
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		Settings.putColor("saddleBifColor", saddleBifColor, prefs);
		Settings.putColor("hopfBifColor", hopfBifColor, prefs);
		Settings.putColor("homoSaddleConColor", homoSaddleConColor, prefs);
		Settings.putColor("heteroSaddleConColor", heteroSaddleConColor, prefs);
		Settings.putColor("semiStableColor", semiStableColor, prefs);
		prefs.putBoolean("drawPent", drawPent);
		prefs.putBoolean("drawAxes", drawAxes);
	}
}
