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

. I

dx/dt = (y - a x)cos b - (y^2 + y x^2 - 3x)sin b
dy/dt = (y - a x)sin b + (y^2 + y x^2 - 3x)cos b


 */