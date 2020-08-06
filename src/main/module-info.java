module DiffEqGrapher.main {
	requires java.desktop;
	requires javafx.controls;
	requires javafx.swing;
	requires java.prefs;
	requires ejml.simple;
	requires ejml.core;


	opens Main to javafx.graphics;
}