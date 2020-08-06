package FXObjects;

import AST.Maths;
import AST.Node;
import Exceptions.EvaluationException;
import Settings.InPlaneSettings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to represent the input parameter plane.
 */
public class InputPlane extends CoordPlane
{

	private InClickModeType clickMode;

	List<Pentagram> pentlist;


	private static DecimalFormat format = new DecimalFormat("#");

	private final Circle pt;
	/**
	 * a and b are the parameters, initialised to (0,0)
	 */
	private double a = 0;
	private double b = 0;
	/**
	 * carries a pointer to the outputplane for communication purposes
	 */
	private final OutputPlane op;
	/**
	 * text fields that display and let the user edit the parameters
	 */
	private final TextField aField, bField;
	/**
	 * colors of various bifurcations so that they are easily differentiable by the user
	 */
	private Color saddleBifColor = Color.BLUE;
	private Color hopfBifColor = Color.DARKRED;
	Color homoSaddleConColor = Color.PURPLE;
	Color heteroSaddleConColor = Color.GREEN;
	Color semiStableColor = Color.TURQUOISE;
	java.awt.Color awtSaddleBifColor = fromFXColor(saddleBifColor);
	java.awt.Color awtHopfBifColor = fromFXColor(hopfBifColor);
	java.awt.Color awtHomoSaddleConColor = fromFXColor(homoSaddleConColor);
	java.awt.Color awtHeteroSaddleConColor = fromFXColor(heteroSaddleConColor);
	java.awt.Color awtSemiStableColor = fromFXColor(semiStableColor);
	/**
	 * Lists to store all of the bifurcations
	 */
	List<double[]> saddleBifs;
	List<double[]> hopfBifs;
	List<SaddleCon> saddleCons;
	List<Point2D> degenSaddleCons;
	List<Point2D> degenHopf;
	List<SemiStableStart> semiStables;
	/**
	 * a separate thread that deals with drawing difficult bifurcations so as not to lock up the program
	 */
	Thread artist;
	/**
	 * canvas that is overlayed on the normal one for drawing saddle connection bifurcations.
	 * These are difficult bifurcations to draw, so we want to avoid redrawing them except when absolutely necessary
	 */
	boolean updateSaddleCons;
	public InPlaneSettings settings;

	/**
	 * Constructs an input plane.
	 * Events:
	 * left right up down keys move the selected point in the parameter space
	 * clicking selects a point
	 * typing in the text fields updates the values
	 * @param side The side length of the window
	 * @param aField pointer to the text field for a
	 * @param bField pointer to the text field for b
	 * @param op pointer to the output plane
	 */
	public InputPlane(double side, TextField aField, TextField bField, OutputPlane op, InPlaneSettings settings)
	{

		super(side);
		this.settings = settings;
		updateSettings();
		currentInstrCode = 100;
		clickMode = InClickModeType.MOVEPOINT;
//		getChildren().addAll(saddleCanvas);
		updateSaddleCons = false;
		this.op = op;
		this.aField = aField;
		this.bField = bField;
		saddleBifs = new ArrayList<>();
		hopfBifs = new ArrayList<>();
		saddleCons = new ArrayList<>();
		degenSaddleCons = new ArrayList<>();
		pentlist = new ArrayList<>();
		degenHopf = new ArrayList<>();
		semiStables = new ArrayList<>();
		pt = new Circle();
		pt.setRadius(2);
		pt.setFill(Color.RED);
		pt.setCenterX(normToScrX(0));
		pt.setCenterY(normToScrY(0));
		this.getChildren().addAll(pt);
		draw();
//		render();
		setOnKeyPressed((e) ->
		{
			KeyCode temp = e.getCode();
			double deltaA = (xMax.get() - xMin.get()) / 1000;
			double deltaB = (yMax.get() - yMin.get()) / 1000;
			if(e.isMetaDown() || e.isControlDown())
			{
				deltaA *= 20;
				deltaB *= 20;
			}
			if (temp == right)
			{
				a += deltaA;
//				a += (xMax.get() - xMin.get()) / 1000;
			} else if (temp == left)
			{
				a -= deltaA;
//				a -= (xMax.get() - xMin.get()) / 1000;
			} else if (temp == up)
			{
				b += deltaB;
//				b += (yMax.get() - yMin.get()) / 1000;
			} else if (temp == down)
			{
				b -= deltaB;
//				b -= (yMax.get() - yMin.get()) / 1000;
			}
			e.consume();
			render();
		});
		setOnKeyReleased(e ->
		{
			KeyCode temp = e.getCode();
			if(temp == left || temp == right || temp == up || temp == down)
			{
				showValues();
				render();
			}
		});
/*
		aField.textProperty().addListener((obs, s, t1) ->
		{
			try
			{
				a = Double.parseDouble(t1);
				render();
			} catch (NumberFormatException n)
			{
				//aField.setText(Double.toString(a));
			}
		});

		bField.textProperty().addListener((obs, s, t1) ->
		{
			try
			{
				b = Double.parseDouble(t1);
				render();
			} catch (NumberFormatException n)
			{
				if (b != 0.0)
					bField.setText(Double.toString(b));
			}
		});*/
//		addEventFilter(MouseEvent.MOUSE_RELEASED, me ->
//		{
//			if(zoomBox.isVisible() && zoomBox.getWidth() > 0 && zoomBox.getHeight() > 0)
//				drawSaddleCons();
//		});
		loading.toFront();

	}

	private void initColors()
	{
		awtSaddleBifColor = fromFXColor(saddleBifColor);
		awtHopfBifColor = fromFXColor(hopfBifColor);
		awtHomoSaddleConColor = fromFXColor(homoSaddleConColor);
		awtHeteroSaddleConColor = fromFXColor(heteroSaddleConColor);
		awtSemiStableColor = fromFXColor(semiStableColor);
	}

	private void updateSettings()
	{
		saddleBifColor = settings.saddleBifColor;
		hopfBifColor = settings.hopfBifColor;
		homoSaddleConColor = settings.homoSaddleConColor;
		heteroSaddleConColor = settings.heteroSaddleConColor;
		semiStableColor = settings.semiStableColor;
		initColors();
	}

	public void updateA(double a)
	{
		if(this.a != a)
		{
			this.a = a;
			render();
		}
	}
	public void updateB(double b)
	{
		if(this.b != b)
		{
			this.b = b;
			render();
		}
	}

	public void setClickMode(InClickModeType ty)
	{
		clickMode = ty;
		switch (ty)
		{
			case MOVEPOINT:
				fireUpdate(100);
				break;
			case PLACEPENT:
				fireUpdate(101);
				break;
			case EDITPENT:
				fireUpdate(102);
				break;
			case REMOVEPENT:
				fireUpdate(103);
				break;
		}
	}

	public void interrupt()
	{
		artist.interrupt();
	}
	@Override
	public void handleMouseClick(MouseEvent e)
	{
		if (!e.isConsumed())
		{
			switch (clickMode)
			{
				case MOVEPOINT:
					a = scrToNormX(e.getX());
					b = scrToNormY(e.getY());
					showValues();
					break;
				case PLACEPENT:
					getInfoAndAddPentagram(e.getX(), e.getY());
			}
			render();
		}
	}

	/**
	 * draws the point
	 */
	private void drawPoint()
	{
//		gc.setFill(Color.RED);
//		gc.fillOval(normToScrX(a) - 2, normToScrY(b) - 2, 4, 4);
//		gc.setFill(Color.BLACK);
		synchronized (pt)
		{
			pt.setCenterX(normToScrX(a));
			pt.setCenterY(normToScrY(b));
		}

	}

	/**
	 * updates the text fields to show the parameters
	 */
	private void showValues()
	{
		synchronized (aField)
		{
			aField.setText(Double.toString(a));
		}
		synchronized (bField)
		{
			bField.setText(Double.toString(b));
		}
	}

	/**
	 * gets a
	 * @return the a parameter
	 */
	public double getA()
	{
		return a;
	}

	/**
	 * gets b
	 * @return the b parameter
	 */
	public double getB()
	{
		return b;
	}

	/**
	 * Helper method for saddle node bifurcations. Uses newton's method to find them
	 * @param start the starting point
	 * @param add whether or not to add this one to the list of saddle node bifurcations
	 */
	private void saddleBifHelp(double start [], boolean add)
	{

		Node dx = op.getDx();
		Node dy = op.getDy();
		Node det = Maths.minus(Maths.mult(dx.diff('x'), dy.diff('y')),
				Maths.mult(dx.diff('y'), dy.diff('x'))).collapse();
		Node derivative[][] = new Node[3][4];
		derivative[0][0] = dx.diff('x');
		derivative[0][1] = dx.diff('y');
		derivative[0][2] = dx.diff('a');
		derivative[0][3] = dx.diff('b');

		derivative[1][0] = dy.diff('x');
		derivative[1][1] = dy.diff('y');
		derivative[1][2] = dy.diff('a');
		derivative[1][3] = dy.diff('b');

		derivative[2][0] = det.diff('x');
		derivative[2][1] = det.diff('y');
		derivative[2][2] = det.diff('a');
		derivative[2][3] = det.diff('b');
		double xInc = (xMax.get() - xMin.get()) / this.getWidth();
		double yInc = (yMax.get() - yMin.get()) / this.getHeight();
		boolean isA = true;
		double first[] = start;
		double second[];
		try
		{
			second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, det, derivative, 'a');
		} catch (SingularMatrixException s)
		{
			isA = false;
			second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, det, derivative, 'b');
		}
		if(second == null) return;
		if(add) saddleBifs.add(second);
		while (inBounds(first[2], first[3]))
		{
			first = second;
			if (isA)
				second = bifHelp(first[0], first[1], first[2] + xInc, first[3], dx, dy,
						det, derivative, 'a');
			else second = bifHelp(first[0], first[1], first[2], first[3] + yInc, dx, dy,
					det, derivative, 'b');
			if(second == null) break;
			drawLine(first[2], first[3], second[2], second[3], awtSaddleBifColor, 3);
		}
		first = start;
		try
		{
			second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, det, derivative, 'a');
		} catch (SingularMatrixException s)
		{
			isA = false;
			second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, det, derivative, 'b');
		}
		if(second == null) return;
		while (inBounds(first[2], first[3]))
		{
			first = second;
			if (isA)
				second = bifHelp(first[0], first[1], first[2] - xInc, first[3], dx, dy,
						det, derivative, 'a');
			else second = bifHelp(first[0], first[1], first[2], first[3] - yInc, dx, dy,
					det, derivative, 'b');
			if(second == null) break;
			drawLine(first[2], first[3], second[2], second[3], awtSaddleBifColor, 3);
		}

	}

	/**
	 * adds and draws a saddle node bifurcation
	 * @param start the starting point for calculation
	 */
	public void saddleBif(Point2D start)
	{
		saddleBifHelp(new double[]{start.getX(), start.getY(), a, b}, true);
		render();
	}

	/**
	 * adds and draws a hopf bifurcation
	 * @param start the starting point for calculation
	 */
	public void hopfBif(Point2D start)
	{
		hopfBifHelp(new double[]{start.getX(), start.getY(), a, b}, true);
		drawDegenHopf();
		render();
	}

	/**
	 * helper method for hopf bifurcations. uses newton's method to calculate
	 * @param start the starting point
	 * @param add whether or not to add it to the list of hopf bifurcations
	 */
	private void hopfBifHelp(double start [], boolean add)
	{
		Node dx = op.getDx().getVal();
		Node dy = op.getDy().getVal();
		Node tr = Maths.add(dx.diff('x'), dy.diff('y')).collapse();
		Node det = Maths.minus(Maths.mult(dx.diff('x'), dy.diff('y')),
				Maths.mult(dx.diff('y'), dy.diff('x'))).collapse();
		Node xDiv, yDiv;
		Node m20, m11, m02, m30, m12, v20, v11, v02, v21, v03;
		xDiv = dx.diff('y');
		m20 = dx.diff('x').diff('x').div(2).div(xDiv);
		m11 = dx.diff('x').diff('y').div(xDiv);
		m02 = dx.diff('y').diff('y').div(2).div(xDiv);
		m30 = dx.diff('x').diff('x').diff('x').div(2).div(xDiv);
//		m21 = dx.diff('x').diff('x').diff('y').div(2).div(xDiv);
		m12 = dx.diff('x').diff('y').diff('y').div(2).div(xDiv);
//		m03 = dx.diff('y').diff('y').diff('y').div(6).div(xDiv);


		yDiv = dy.diff('x').neg();
		v20 = dy.diff('x').diff('x').div(2).div(yDiv);
		v11 = dy.diff('x').diff('y').div(yDiv);
		v02 = dy.diff('y').diff('y').div(2).div(yDiv);
		v21 = dy.diff('x').diff('x').diff('y').div(2).div(yDiv);
		v03 = dy.diff('y').diff('y').diff('y').div(2).div(yDiv);

		Node L = m30.add(m12).add(v21).add(v03).sub(m20.mul(m11)).add(v11.mul(v02)).sub(m02.mul(v02).mul(2)).sub(m02.mul(m11)).add(m20.mul(v20).mul(2)).add(v11.mul(v20)).collapse();
		System.out.println(L.toLatex(new StringBuilder()));

		Node derivative[][] = new Node[3][4];
		derivative[0][0] = dx.diff('x');
		derivative[0][1] = dx.diff('y');
		derivative[0][2] = dx.diff('a');
		derivative[0][3] = dx.diff('b');

		derivative[1][0] = dy.diff('x');
		derivative[1][1] = dy.diff('y');
		derivative[1][2] = dy.diff('a');
		derivative[1][3] = dy.diff('b');

		derivative[2][0] = tr.diff('x');
		derivative[2][1] = tr.diff('y');
		derivative[2][2] = tr.diff('a');
		derivative[2][3] = tr.diff('b');
		double xInc = (xMax.get() - xMin.get()) / this.getWidth();
		double yInc = (yMax.get() - yMin.get()) / this.getHeight();
		boolean isA = true;
		double first[] = start;
		double second[];
		try
		{
			second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, tr, derivative, 'a');
		} catch (SingularMatrixException s)
		{
			isA = false;
			second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, tr, derivative, 'b');
		}
		if(second == null) return;
		if(add) hopfBifs.add(second);
		double t = op.getT();
		try
		{
			while (inBounds(first[2], first[3]) && det.eval(first, t) > 0.)
			{
				first = second;
				if (isA)
					second = bifHelp(first[0], first[1], first[2] + xInc, first[3], dx, dy, tr, derivative, 'a');
				else second = bifHelp(first[0], first[1], first[2], first[3] + yInc, dx, dy, tr, derivative, 'b');
				if(second == null) break;
				double firstEval = L.eval(first, t);
				double secondEval = L.eval(second, t);
//				if(firstEval == 0D || firstEval == -0D)
//					degenHopf.add(new Point2D(first[2], first[3]));
//				else if (secondEval == 0D || secondEval == -0D)
//					degenHopf.add(new Point2D(second[2], second[3]));
				if (Math.signum(firstEval) != Math.signum(secondEval))
				{
					Point2D prev = new Point2D(first[2], first[3]);
					Point2D next = new Point2D(second[2], second[3]);
					double factor = Math.abs(firstEval) / (Math.abs(secondEval) + Math.abs(firstEval));
					degenHopf.add(prev.add(next.subtract(prev).multiply(factor)));
					System.out.println("DEGEN DEGEN DEGEN");
				}
				drawLine(first[2], first[3], second[2], second[3], awtHopfBifColor, 3);
			}
			first = start;
			try
			{
				second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, tr, derivative, 'a');
			} catch (SingularMatrixException s)
			{
				isA = false;
				second = bifHelp(first[0], first[1], first[2], first[3], dx, dy, tr, derivative, 'b');
			}
			if(second == null) return;
			while (inBounds(first[2], first[3]) && det.eval(first[0], first[1], first[2], first[3], t) > 0.)
			{
				first = second;
				if (isA)
					second = bifHelp(first[0], first[1], first[2] - xInc, first[3], dx, dy, tr, derivative, 'a');
				else second = bifHelp(first[0], first[1], first[2], first[3] - yInc, dx, dy, tr, derivative, 'b');
				if (second == null) break;
				double firstEval = L.eval(first, t);
				double secondEval = L.eval(second, t);
//				if(firstEval == 0D || firstEval == -0D)
//					degenHopf.add(new Point2D(first[2], first[3]));
//				else if (secondEval == 0D || secondEval == -0D)
//					degenHopf.add(new Point2D(second[2], second[3]));
				if (Math.signum(firstEval) != Math.signum(secondEval))
				{
					Point2D prev = new Point2D(first[2], first[3]);
					Point2D next = new Point2D(second[2], second[3]);
					double factor = Math.abs(firstEval) / (Math.abs(secondEval) + Math.abs(firstEval));
					degenHopf.add(prev.add(next.subtract(prev).multiply(factor)));
					System.out.println("DEGEN DEGEN DEGEN");
				}

				drawLine(first[2], first[3], second[2], second[3], awtHopfBifColor, 3);
			}
		} catch (EvaluationException ignored) {}
	}

	/**
	 * Helper method for saddle node and hopf bifurcations, as they are a similar calculation.
	 * Essentially calculates one (1) point of bifurcation starting at the given input using Newton's method
	 * This is possible since saddle node and hopf bifurcations are solutions to three equations in 4 variables,
	 * so their solutions are probably a curve of dimension one, meaning we can use newton's method by fixing one of
	 * the variables.
	 * @param x the starting x
	 * @param y the starting y
	 * @param aTemp the a value to calculate for
	 * @param bTemp the b value to calculate for
	 * @param n1 the first equation
	 * @param n2 the second equation
	 * @param n3 the third equation
	 * @param derivative The jacobian of the system
	 * @param cons marks which variable is held constant
	 * @return the value for the non constant variable, given the constant one
	 */
	private double bifHelp(double x, double y, double aTemp, double bTemp, Node n1, Node n2, Node n3, Node derivative[][], char cons)[]
	{
		double t = op.getT();
		double temp[] = new double[4];
		try
		{
			double xt = x;
			double yt = y;
			double at = aTemp;
			double bt = bTemp;
			for (int j = 0; j < 10; j++)
			{
				SimpleMatrix init = new SimpleMatrix(3, 1);
				init.setColumn(0, 0, xt, yt);
				if (cons == 'a') init.set(2, 0, bt);
				else init.set(2, 0, at);
				SimpleMatrix deriv = new SimpleMatrix(3, 3);
				for (int i = 0; i < 3; i++)
				{
					deriv.setRow(i, 0, derivative[i][0].eval(xt, yt, at, bt, t),
							derivative[i][1].eval(xt, yt, at, bt, t));
					if (cons == 'a') deriv.set(i, 2, derivative[i][3].eval(xt, yt, at, bt, t));
					else deriv.set(i, 2, derivative[i][2].eval(xt, yt, at, bt, t));
				}
				SimpleMatrix inv = deriv.invert();
				SimpleMatrix val = new SimpleMatrix(3, 1);
				val.setColumn(0, 0, n1.eval(xt, yt, at, bt, t), n2.eval(xt, yt, at, bt, t),
						n3.eval(xt, yt, at, bt, t));
				SimpleMatrix result = init.minus(inv.mult(val));
				xt = temp[0] = result.get(0, 0);
				yt = temp[1] = result.get(1, 0);
				if (cons == 'a')
				{
					temp[2] = aTemp;
					bt = temp[3] = result.get(2, 0);
				} else
				{
					at = temp[2] = result.get(2, 0);
					temp[3] = bTemp;
				}
				if (Math.pow(init.get(0, 0) - result.get(0, 0), 2) +
						Math.pow(init.get(1, 0) - result.get(1, 0), 2) +
						Math.pow(init.get(2, 0) - result.get(2, 0), 2) < .0001)
					return temp;
			}
		} catch (EvaluationException | SingularMatrixException e)
		{
			return null;
		}
		return null;
	}

	@Override
	protected void updateForZoom()
	{
	}
	@Override
	protected void updateForResize()
	{
		drawPoint();
	}
	@Override
	public void render()
	{
		super.render();
//		showValues();
		drawPoint();
		drawPentagrams();
	}

	@Override
	public void draw()
	{
		super.draw();
		g.setStroke(new BasicStroke(2));

		for(double saddleBif []: saddleBifs)
		{
			saddleBifHelp(saddleBif, false);
		}
		for(double hopfBif []: hopfBifs)
		{
			hopfBifHelp(hopfBif, false);
		}
		drawDegenHopf();
		drawSaddleCons();
		drawSemiStables();
		drawDegenSaddleCons();
		render();
	}

	/**
	 * Draws the pentagrams to the screen
	 */
	private void drawPentagrams()
	{
		for(Pentagram p : pentlist)
		{
			drawPentagram(p);
		}
	}

	private void drawPentagram(Pentagram p)
	{
		p.setLayoutX(normToScrX(p.mathLoc.getX()) - 15);
		p.setLayoutY(normToScrY(p.mathLoc.getY()) - 15);
		p.setVisible(true);
	}

	/**
	 * draws all the saddle cons in the current list
	 * @implNote uses the artist thread, interrupts it if it is already busy, as whenever we draw saddle cons,
	 * we throw away whatever old saddle cons were being drawn
	 */
	public void drawSaddleCons()
	{
		degenSaddleCons.clear();
		if(artist != null)
			artist.interrupt();

		for(SaddleCon sad : saddleCons)
		{
			artist = new Thread(() ->
			{
				Platform.runLater(() -> loading.setVisible(true));
				synchronized (this)
				{
					op.renderSaddleCon(sad.pt, sad.s1, sad.s2, false, sad.transversal);
				}
				Platform.runLater(() -> loading.setVisible(false));
			});
			artist.setDaemon(true);
			artist.start();
		}
		drawDegenSaddleCons();
	}

	public void drawSemiStables()
	{
		for(SemiStableStart s : semiStables)
		{
			artist = new Thread(() ->
			{
				Platform.runLater(() -> loading.setVisible(true));
				synchronized (this)
				{
					op.renderSemiStable(s.lnSt, s.lnNd, s.start, false);
				}
				Platform.runLater(() -> loading.setVisible(false));
			});
			artist.setDaemon(true);
			artist.start();
		}
	}

	/**
	 * draws all of the points where a degenerate saddle connection occurs
	 */
//	dx/dt = b x + y + a (x^2 + x y) + x^3
//	dy/dt = y^2 - x
	public void drawDegenSaddleCons()
	{
		g.setColor(awtHomoSaddleConColor);
		for(Point2D p : degenSaddleCons)
		{
			g.drawOval(imgNormToScrX(p.getX()) - 6, imgNormToScrY(p.getY()) - 6, 12, 12);

		}
	}

	private void drawDegenHopf()
	{
		g.setColor(awtHopfBifColor);
		for(Point2D p : degenHopf)
		{
			g.drawOval(imgNormToScrX(p.getX()) - 6, imgNormToScrY(p.getY()) - 6, 12, 12);
		}
	}

	@Override
	public void clear()
	{
		saddleBifs.clear();
		hopfBifs.clear();
		saddleCons.clear();
		degenSaddleCons.clear();
		degenHopf.clear();
		semiStables.clear();
		for(Pentagram p : pentlist)
		{
			this.getChildren().remove(p);
		}
		pentlist.clear();
		draw();
	}

	@Override
	public boolean writePNG(File f)
	{
		BufferedImage temp = new BufferedImage(canv.getWidth(), canv.getHeight(), canv.getType());
		Graphics2D g2 = temp.createGraphics();
		g2.drawImage(canv, 0, 0, null);
		g2.setColor(java.awt.Color.BLACK);
		if(settings.drawAxes)
		{
			int x0 = imgNormToScrX(0);
			int y0 = imgNormToScrY(0);
			g2.drawLine(x0, 0, x0, temp.getHeight());
			g2.drawLine(0, y0, temp.getWidth(), y0);
		}
		if(settings.drawPent)
			for(Pentagram p : pentlist)
			{
				int w = imgNormToScrX(scrToNormX(p.border.getWidth()));
				int h = imgNormToScrY(scrToNormY(p.border.getHeight()));
				int x = imgNormToScrX(scrToNormX(p.getLayoutX()));
				int y = imgNormToScrY(scrToNormY(p.getLayoutY()));
				g2.drawRect(x, y, w, h);
				g2.setFont(new Font("Big", Font.PLAIN, 20));
				g2.drawString(String.valueOf(p.getSaddles()),
						x + w/2 - g2.getFontMetrics().stringWidth(p.saddle.getText())/2,
						y + h/2 + g2.getFontMetrics().getHeight()/2 - 5);
				g2.drawString(String.valueOf(p.getSinks()),
						x + 5,
						y + g2.getFontMetrics().getHeight());
				g2.drawString(String.valueOf(p.getSources()),
					x + w - g2.getFontMetrics().stringWidth(String.valueOf(p.getSources())) - 3,
						y + g2.getFontMetrics().getHeight());
				g2.drawString(String.valueOf(p.getAttrCycles()),
						x + 5,
						y + h - 5);
				g2.drawString(String.valueOf(p.getRepelCycles()),
						x + w - g2.getFontMetrics().stringWidth(String.valueOf(p.getRepelCycles())) - 3,
						y + h - 5);
			}
		try
		{
			ImageIO.write(temp, "png", f);
			return true;
		} catch (IOException | NullPointerException oof)
		{
			return false;
		}
	}

	private void getInfoAndAddPentagram(double x, double y)
	{
		{
			Pentagram P = new Pentagram(new Point2D(x, y), new Point2D(scrToNormX(x), scrToNormY(y)));
			int info [] = new int [5];
			Stage newWindow = new Stage();
			newWindow.setTitle("Enter Info for Pentagram");
			StackPane mainStack = new StackPane();
			Label
					lblSink = new Label("# Sinks"),
					lblSource = new Label("# Sources"),
					lblSad = new Label("# Saddles"),
					lblAttr = new Label("# Attracting Cycles"),
					lblRep = new Label("# Repelling Cycles");
			TextField
					txtSink = new TextField("0"),
					txtSource = new TextField("0"),
					txtSad = new TextField("0"),
					txtAttr = new TextField("0"),
					txtRep = new TextField("0");
			TextField [] lst = new TextField[] {txtSink, txtSource, txtSad, txtAttr, txtRep};
			for(TextField fld : lst)
			{
				fld.textProperty().addListener((observable, oldValue, newValue) ->
				{
					if (!newValue.matches("([1-9][0-9]+)|[0-9]"))
						fld.setText(oldValue);
				});
				fld.setOnMouseClicked(e ->
				{
					fld.selectAll();
				});
			}
			double w = 50;
			txtSink.setMaxWidth(w);
			txtSource.setMaxWidth(w);
			txtSad.setMaxWidth(w);
			txtAttr.setMaxWidth(w);
			txtRep.setMaxWidth(w);

			VBox
					vSink = new VBox(lblSink, txtSink),
					vSource = new VBox(lblSource, txtSource),
					vSad = new VBox(lblSad, txtSad),
					vAttr = new VBox(lblAttr, txtAttr),
					vRep = new VBox(lblRep, txtRep);

			vSink.setAlignment(Pos.TOP_LEFT);
			vSource.setAlignment(Pos.TOP_RIGHT);
			vSad.setAlignment(Pos.CENTER);
			vAttr.setAlignment(Pos.BOTTOM_LEFT);
			vRep.setAlignment(Pos.BOTTOM_RIGHT);
			vSink.setPickOnBounds(false);
			vSource.setPickOnBounds(false);
			vSad.setPickOnBounds(false);
			vAttr.setPickOnBounds(false);
			vRep.setPickOnBounds(false);

			mainStack.setPadding(new Insets(15));
			mainStack.getChildren().addAll(vSink, vSource, vSad, vAttr, vRep);
			newWindow.setMinWidth(350);
			newWindow.setMinHeight(200);

			Scene newScene = new Scene(mainStack);
			newWindow.setScene(newScene);

			newWindow.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent ->
			{
				if(keyEvent.getCode() == KeyCode.ENTER)
				{
					try
					{
						info[0] = Integer.parseInt(txtSink.getText());
					} catch (NumberFormatException ignored) {}
					try
					{
						info[1] = Integer.parseInt(txtSource.getText());
					} catch (NumberFormatException ignored) {}
					try
					{
						info[2] = Integer.parseInt(txtSad.getText());
					} catch (NumberFormatException ignored) {}
					try
					{
						info[3] = Integer.parseInt(txtAttr.getText());
					} catch (NumberFormatException ignored) {}
					try
					{
						info[4] = Integer.parseInt(txtRep.getText());
					} catch (NumberFormatException ignored) {}
					P.setInfo(null, info);
					P.updateTexts();
					pentlist.add(P);
					getChildren().add(P);
					P.addEventHandler(MouseEvent.MOUSE_CLICKED, me ->
					{
						if(clickMode == InClickModeType.REMOVEPENT)
						{
							getChildren().remove(P);
							pentlist.remove(P);
							setClickMode(InClickModeType.MOVEPOINT);
						}
						else if (clickMode == InClickModeType.EDITPENT)
						{
							getChildren().remove(P);
							pentlist.remove(P);
							getInfoAndAddPentagram(x, y);
							setClickMode(InClickModeType.MOVEPOINT);
						}
					});
					drawPentagram(P);
					newWindow.fireEvent(new WindowEvent(newWindow, WindowEvent.WINDOW_CLOSE_REQUEST));
				}
			});

			newWindow.setOnCloseRequest((e) ->
			{
				setClickMode(InClickModeType.MOVEPOINT);
			});
			newWindow.show();


		}
	}

}
