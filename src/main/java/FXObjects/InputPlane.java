package FXObjects;

import AST.Maths;
import AST.Node;
import Exceptions.EvaluationException;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to represent the input parameter plane.
 */
public class InputPlane extends CoordPlane
{

	InClickModeType clickMode;

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
	java.awt.Color awtSaddleBifColor = fromFXColor(saddleBifColor);
	java.awt.Color awtHopfBifColor = fromFXColor(hopfBifColor);
	java.awt.Color awtHomoSaddleConColor = fromFXColor(homoSaddleConColor);
	java.awt.Color awtHeteroSaddleConColor = fromFXColor(heteroSaddleConColor);
	/**
	 * Lists to store all of the bifurcations
	 */
	List<double[]> saddleBifs;
	List<double[]> hopfBifs;
	List<SaddleCon> saddleCons;
	List<Point2D> degenSaddleCons;
	/**
	 * a separate thread that deals with drawing difficult bifurcations so as not to lock up the program
	 */
	Thread artist;
	/**
	 * canvas that is overlayed on the normal one for drawing saddle connection bifurcations.
	 * These are difficult bifurcations to draw, so we want to avoid redrawing them except when absolutely necessary
	 */
	Canvas saddleCanvas;
	boolean updateSaddleCons;


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
	public InputPlane(double side, TextField aField, TextField bField, OutputPlane op)
	{

		super(side);

		clickMode = InClickModeType.MOVEPOINT;
		saddleCanvas = new Canvas(side, side);
//		getChildren().addAll(saddleCanvas);
		updateSaddleCons = false;
		this.op = op;
		this.aField = aField;
		this.bField = bField;
		saddleBifs = new LinkedList<>();
		hopfBifs = new LinkedList<>();
		saddleCons = new LinkedList<>();
		degenSaddleCons = new LinkedList<>();
		saddleCanvas.setVisible(true);
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
			if (temp == right)
			{
				a += (xMax.get() - xMin.get()) / 1000;
			} else if (temp == left)
			{
				a -= (xMax.get() - xMin.get()) / 1000;
			} else if (temp == up)
			{
				b += (yMax.get() - yMin.get()) / 1000;
			} else if (temp == down)
			{
				b -= (yMax.get() - yMin.get()) / 1000;
			}
			e.consume();
			render();
		});

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
		});
//		addEventFilter(MouseEvent.MOUSE_RELEASED, me ->
//		{
//			if(zoomBox.isVisible() && zoomBox.getWidth() > 0 && zoomBox.getHeight() > 0)
//				drawSaddleCons();
//		});

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
					break;
				case PLACEPENT:
					//TODO do shit, open a pentagram dialog and get that shit on the screen
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
		gc.setStroke(saddleBifColor);

		Node dx = op.getDx();
		Node dy = op.getDy();
		Node det = Maths.minus(Maths.mult(dx.differentiate('x'), dy.differentiate('y')),
				Maths.mult(dx.differentiate('y'), dy.differentiate('x'))).collapse();
		Node derivative[][] = new Node[3][4];
		derivative[0][0] = dx.differentiate('x').collapse();
		derivative[0][1] = dx.differentiate('y').collapse();
		derivative[0][2] = dx.differentiate('a').collapse();
		derivative[0][3] = dx.differentiate('b').collapse();

		derivative[1][0] = dy.differentiate('x').collapse();
		derivative[1][1] = dy.differentiate('y').collapse();
		derivative[1][2] = dy.differentiate('a').collapse();
		derivative[1][3] = dy.differentiate('b').collapse();

		derivative[2][0] = det.differentiate('x').collapse();
		derivative[2][1] = det.differentiate('y').collapse();
		derivative[2][2] = det.differentiate('a').collapse();
		derivative[2][3] = det.differentiate('b').collapse();
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
		if(add) saddleBifs.add(second);
		while (inBounds(first[2], first[3]) && second != null)
		{
			first = second;
			if (isA)
				second = bifHelp(first[0], first[1], first[2] + xInc, first[3], dx, dy,
						det, derivative, 'a');
			else second = bifHelp(first[0], first[1], first[2], first[3] + yInc, dx, dy,
					det, derivative, 'b');
			gc.strokeLine(normToScrX(first[2]), normToScrY(first[3]), normToScrX(second[2]), normToScrY(second[3]));
			drawLine(first[2], first[3], second[2], second[3], awtSaddleBifColor);
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
		while (inBounds(first[2], first[3]) && second != null)
		{
			first = second;
			if (isA)
				second = bifHelp(first[0], first[1], first[2] - xInc, first[3], dx, dy,
						det, derivative, 'a');
			else second = bifHelp(first[0], first[1], first[2], first[3] - yInc, dx, dy,
					det, derivative, 'b');
			if(second == null) break;
			gc.strokeLine(normToScrX(first[2]), normToScrY(first[3]), normToScrX(second[2]), normToScrY(second[3]));
			drawLine(first[2], first[3], second[2], second[3], awtSaddleBifColor);
		}

		gc.setStroke(Color.BLACK);
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
		render();
	}

	/**
	 * helper method for hopf bifurcations. uses newton's method to calculate
	 * @param start the starting point
	 * @param add whether or not to add it to the list of hopf bifurcations
	 */
	private void hopfBifHelp(double start [], boolean add)
	{
		gc.setStroke(hopfBifColor);

		g.setColor(awtHopfBifColor);
		Node dx = op.getDx();
		Node dy = op.getDy();
		Node tr = Maths.add(dx.differentiate('x'), dy.differentiate('y')).collapse();
		Node det = Maths.minus(Maths.mult(dx.differentiate('x'), dy.differentiate('y')),
				Maths.mult(dx.differentiate('y'), dy.differentiate('x'))).collapse();
		Node derivative[][] = new Node[3][4];
		derivative[0][0] = dx.differentiate('x').collapse();
		derivative[0][1] = dx.differentiate('y').collapse();
		derivative[0][2] = dx.differentiate('a').collapse();
		derivative[0][3] = dx.differentiate('b').collapse();

		derivative[1][0] = dy.differentiate('x').collapse();
		derivative[1][1] = dy.differentiate('y').collapse();
		derivative[1][2] = dy.differentiate('a').collapse();
		derivative[1][3] = dy.differentiate('b').collapse();

		derivative[2][0] = tr.differentiate('x').collapse();
		derivative[2][1] = tr.differentiate('y').collapse();
		derivative[2][2] = tr.differentiate('a').collapse();
		derivative[2][3] = tr.differentiate('b').collapse();
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
		if(add) hopfBifs.add(second);
		double t = op.getT();
		try
		{
			while (inBounds(first[2], first[3]) && second != null && det.eval(first[0], first[1], first[2], first[3], t) > 0.)
			{
				first = second;
				if (isA)
					second = bifHelp(first[0], first[1], first[2] + xInc, first[3], dx, dy, tr, derivative, 'a');
				else second = bifHelp(first[0], first[1], first[2], first[3] + yInc, dx, dy, tr, derivative, 'b');
				gc.strokeLine(normToScrX(first[2]), normToScrY(first[3]), normToScrX(second[2]), normToScrY(second[3]));
				drawLine(first[2], first[3], second[2], second[3], awtHopfBifColor);
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
			while (inBounds(first[2], first[3]) && second != null && det.eval(first[0], first[1], first[2], first[3], t) > 0.)
			{
				first = second;
				if (isA)
					second = bifHelp(first[0], first[1], first[2] - xInc, first[3], dx, dy, tr, derivative, 'a');
				else second = bifHelp(first[0], first[1], first[2], first[3] - yInc, dx, dy, tr, derivative, 'b');
				if (second == null) break;
				gc.strokeLine(normToScrX(first[2]), normToScrY(first[3]), normToScrX(second[2]), normToScrY(second[3]));
				drawLine(first[2], first[3], second[2], second[3], awtHopfBifColor);
			}
		} catch (EvaluationException ignored) {}
		gc.setStroke(Color.BLACK);
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
		} catch (EvaluationException e)
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
	public void render()
	{
		super.render();
		showValues();
		drawPoint();
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
		drawSaddleCons();
		drawDegenSaddleCons();
		render();
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
		saddleCanvas.getGraphicsContext2D().clearRect(0, 0, c.getWidth(), c.getHeight());
		for(SaddleCon sad : saddleCons)
		{
			artist = new Thread(() ->
			{
				synchronized (this)
				{
					op.renderSaddleCon(sad.pt, sad.s1, sad.s2, false);
				}
			});
			artist.start();
		}
		drawDegenSaddleCons();
	}

	/**
	 * draws all of the points where a degenerate saddle connection occurs
	 */
//	dx/dt = b x + y + a (x^2 + x y) + x^3
//	dy/dt = y^2 - x
	public void drawDegenSaddleCons()
	{
		gc.setStroke(homoSaddleConColor);
		g.setColor(awtHomoSaddleConColor);
		for(Point2D p : degenSaddleCons)
		{
			gc.strokeOval(normToScrX(p.getX()) - 3, normToScrY(p.getY()) - 3, 6, 6);
			g.drawOval(imgNormToScrX(p.getX()) - 6, imgNormToScrY(p.getY()) - 6, 12, 12);

		}
		gc.setStroke(Color.BLACK);
	}

	@Override
	public void clear()
	{
		saddleBifs.clear();
		hopfBifs.clear();
		saddleCons.clear();
		saddleCanvas.getGraphicsContext2D().clearRect(0, 0, c.getWidth(), c.getHeight());
		draw();
	}

	@Override
	public boolean writePNG(File f)
	{
		BufferedImage temp = new BufferedImage(canv.getWidth(), canv.getHeight(), canv.getType());
		Graphics2D g2 = temp.createGraphics();
		g2.drawImage(canv, 0, 0, null);
		g2.setColor(java.awt.Color.BLACK);
		int x0 = imgNormToScrX(0);
		int y0 = imgNormToScrY(0);
		g2.drawLine(x0, 0, x0, temp.getHeight());
		g2.drawLine(0, y0, temp.getWidth(), y0);
		try
		{
			ImageIO.write(temp, "png", f);
			return true;
		} catch (IOException | NullPointerException oof)
		{
			return false;
		}
	}

}
