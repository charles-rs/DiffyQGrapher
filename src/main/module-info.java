module DiffEqGrapher.main {
	requires java.desktop;
	requires javafx.controls;
	requires javafx.swing;
	requires java.prefs;
	requires ejml.simple;
	requires ejml.core;


	opens Main to javafx.graphics;
}

/*

dx/dt = (y - a)cos b - (y - x^3 + 3x)sin b
dy/dt = (y - a)sin b + (y - x^3 + 3x)cos b
 */