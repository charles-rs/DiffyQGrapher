package Settings;

import javafx.scene.paint.Color;

import java.util.prefs.Preferences;

public class Settings
{
	public OutPlaneSettings outPlaneSettings;
	public InPlaneSettings inPlaneSettings;
	public Settings(OutPlaneSettings out, InPlaneSettings in)
	{
		this.outPlaneSettings = out;
		this.inPlaneSettings = in;
	}
	public static Settings fromPrefs()
	{
		return new Settings(OutPlaneSettings.fromPrefs(), InPlaneSettings.fromPrefs());
	}
	public void write()
	{
		outPlaneSettings.write();
		inPlaneSettings.write();
	}
	static Color getColor(String name, Color def, Preferences prefs)
	{
		double r, g, b, a;
		r = prefs.getDouble(name + "R", -1);
		g = prefs.getDouble(name + "G", -1);
		b = prefs.getDouble(name + "B", -1);
		a = prefs.getDouble(name + "A", -1);
		if(Math.min(r, Math.min(g, Math.min(b, a))) >= 0)
			return new Color(r, g, b, a);
		else
		{
			return def;
		}
	}
	static void putColor(String name, Color col, Preferences prefs)
	{
		prefs.putDouble(name + "R", col.getRed());
		prefs.putDouble(name + "G", col.getGreen());
		prefs.putDouble(name + "B", col.getBlue());
		prefs.putDouble(name + "A", col.getOpacity());
	}

}
