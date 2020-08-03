module DiffEqGrapher.main {
	requires java.desktop;
	requires javafx.controls;
	requires javafx.swing;
	requires java.prefs;
	requires ejml.simple;


	opens Main to javafx.graphics;
}