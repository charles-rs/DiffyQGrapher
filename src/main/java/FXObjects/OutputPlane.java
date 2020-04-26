package FXObjects;

import AST.Derivative;
import AST.Maths;
import AST.Value;
import Evaluation.*;
import Events.SaddleSelected;
import Events.SourceSelected;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.misc.RrefGaussJordanRowPivot_DDRM;
import org.ejml.dense.row.misc.RrefGaussJordanRowPivot_FDRM;
import org.ejml.interfaces.linsol.ReducedRowEchelonForm;
import org.ejml.simple.SimpleMatrix;


import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class OutputPlane extends CoordPlane
{
	private double t = 0;
	private final List<initCond> initials;
	private final List<initCond> isoclines;
	private List<CriticalPoint> criticalPoints;
	private final List<Point2D> selectedCritPoints;
	private final List<Point2D> horizIsos;
	private final List<Point2D> vertIsos;
	private final List<Node> needsReset;
	private final List<sepStart> selectedSeps;
	double inc = .01;
	private double a, b;
	private Derivative dx, dy;
	public EvalType evalType;
	public ClickModeType clickMode = ClickModeType.DRAWPATH;
	private boolean drawSep = false;
	private CriticalPoint currentPoint = null;

	private Color solutionColor = Color.BLACK;
	private Color isoclineColor = Color.BLUE;
	private Color horizIsoColor = Color.PURPLE;
	private Color vertIsoColor = Color.ORANGE;
	private Color stblSeparatrixColor = Color.ORANGERED;
	private Color unstblSeparatrixColor = Color.DARKCYAN;
	private Color criticalColor = Color.RED;
	private Color lnColor = Color.CADETBLUE;
	public InputPlane in;


	public OutputPlane(double side, TextField tField)
	{
		super(side);
		evalType = EvalType.RungeKutta;
		initials = new LinkedList<>();
		criticalPoints = new LinkedList<>();
		isoclines = new LinkedList<>();
		needsReset = new LinkedList<>();
		horizIsos = new LinkedList<>();
		vertIsos = new LinkedList<>();
		selectedCritPoints = new LinkedList<>();
		selectedSeps = new ArrayList<>(2);
		draw();
		tField.setText(Double.toString(t));

		tField.setOnKeyPressed((e) ->
		{
			if (e.getCode() == KeyCode.ENTER)
			{
				try
				{
					t = Double.parseDouble(tField.getText());
					drawAxes();
				} catch (NumberFormatException n)
				{
					tField.setText(Double.toString(t));
				}
			}
		});
		setOnKeyPressed((e) ->
		{
			if (e.getCode() == left)
			{
				xMin -= (xMax - xMin) / 20;
				xMax -= (xMax - xMin) / 20;
			} else if (e.getCode() == right)
			{
				xMin += (xMax - xMin) / 20;
				xMax += (xMax - xMin) / 20;
			} else if (e.getCode() == up)
			{
				yMin += (yMax - yMin) / 20;
				yMax += (yMax - yMin) / 20;
			} else if (e.getCode() == down)
			{
				yMin -= (yMax - yMin) / 20;
				yMax -= (yMax - yMin) / 20;
			}
			KeyCode temp = e.getCode();
			if (temp == left || temp == right || temp == up || temp == down)
			{
				draw();
				e.consume();
			}
		});
	}

	public Derivative getDx()
	{
		return (Derivative) dx.clone();
	}

	public Derivative getDy()
	{
		return (Derivative) dy.clone();
	}

	public double getT()
	{
		return t;
	}


	public void clearObjects()
	{
		selectedCritPoints.clear();
		criticalPoints.clear();
		selectedSeps.clear();
		for (Node n : needsReset) n.setVisible(false);
		needsReset.clear();
	}

	public void updateA(double a)
	{
		this.a = a;
		draw();
	}

	public void updateB(double b)
	{
		this.b = b;
		draw();
	}

	public void updateDX(Derivative temp)
	{
		dx = temp;
	}

	public void updateDY(Derivative temp)
	{
		dy = temp;
	}

	@Override
	public void handleMouseClick(MouseEvent e)
	{
		double x = scrToNormX(e.getX());
		double y = scrToNormY(e.getY());
		initCond temp = new initCond(x, y, t);
		Point2D pt = new Point2D(x, y);
		switch (clickMode)
		{
			case DRAWPATH:
				initials.add(temp);
				drawGraph(temp, true);
				break;
			case FINDCRITICAL:
				try
				{
					CriticalPoint root = EvaluatorFactory.getEulerEval(dx, dy).findCritical(pt, a, b, t);
					criticalPoints.add(root);
					drawGraphs();
				} catch (RootNotFound r)
				{
					//TODO better output system
					System.out.println("Root not found");
				}
				break;
			case DRAWHORIZISO:
				drawHorizIso(pt);
				horizIsos.add(pt);
				clickMode = ClickModeType.DRAWPATH;
				break;
			case DRAWVERTISO:
				drawVertIso(new Point2D(x, y));
				vertIsos.add(pt);
				clickMode = ClickModeType.DRAWPATH;
				break;
			case DRAWISO:
				isoclines.add(temp);
				drawIso(temp);
				break;
			case SELECTSADDLE:
				try
				{
					Point2D p = getSaddle(pt);
					fireEvent(new SaddleSelected(p));
					selectedCritPoints.add(p);
					drawSelectedCritPoints();
				} catch (RootNotFound ignored)
				{
				}
				break;
			case SELECTSOURCE:
				try
				{
					Point2D p = getSource(pt);
					fireEvent(new SourceSelected(p));
					selectedCritPoints.add(p);
					drawSelectedCritPoints();
				} catch (RootNotFound ignored)
				{
				}
				break;
			case SELECTSEP:
				try
				{
					CriticalPoint p = critical(pt);
					if (selectedSeps.size() < 2)
					{
						double tol = (xMax - xMin)/c.getWidth();
						Point2D p1 = new Point2D(p.point.getX() + tol * p.matrix.getEigenVector(0).get(0),
								p.point.getY() + tol * p.matrix.getEigenVector(0).get(1));
						Point2D p2 = new Point2D(p.point.getX() - tol * p.matrix.getEigenVector(0).get(0),
								p.point.getY() - tol * p.matrix.getEigenVector(0).get(1));

						Point2D p3 = new Point2D(p.point.getX() + tol * p.matrix.getEigenVector(1).get(0),
								p.point.getY() + tol * p.matrix.getEigenVector(1).get(1));
						Point2D p4 = new Point2D(p.point.getX() - tol * p.matrix.getEigenVector(1).get(0),
								p.point.getY() - tol * p.matrix.getEigenVector(1).get(1));
						double d1 = pt.distance(p1);
						double d2 = pt.distance(p2);
						double d3 = pt.distance(p3);
						double d4 = pt.distance(p4);
						double min = Math.min(Math.min(d1, d2), Math.min(d3, d4));
						boolean firstPos = p.matrix.getEigenvalue(0).getReal() > 0;
						if (d1 == min)
							selectedSeps.add(new sepStart(p, true, 0));
						else if (d2 == min)
							selectedSeps.add(new sepStart(p, false, 0));
						else if (d3 == min)
							selectedSeps.add(new sepStart(p, true, 1));
						else
							selectedSeps.add(new sepStart(p, false, 1));
						draw();
						if (selectedSeps.size() == 2)
						{
							new Thread(() ->
							{
								synchronized (in)
								{
									renderSaddleCon(new Point2D(a, b), selectedSeps.get(0), selectedSeps.get(1), true);
									selectedSeps.clear();
								}
							}).start();

							selectedCritPoints.clear();
							clickMode = ClickModeType.DRAWPATH;
						}
					}
				} catch (RootNotFound ignored)
				{
				}
		}

	}

	private SimpleMatrix getDerivsOfSol(Point2D p, double a, double b)
	{
		AST.Node res[] = new AST.Node[2];
		AST.Node derivAB[][] = new AST.Node[2][2];
		derivAB[0][0] = dx.differentiate('a').collapse();
		derivAB[0][1] = dx.differentiate('b').collapse();
		derivAB[1][0] = dy.differentiate('a').collapse();
		derivAB[1][1] = dy.differentiate('b').collapse();

		AST.Node derivXY[][] = new AST.Node[2][2];
		derivXY[0][0] = dx.differentiate('x').collapse();
		derivXY[0][1] = dx.differentiate('y').collapse();
		derivXY[1][0] = dy.differentiate('x').collapse();
		derivXY[1][1] = dy.differentiate('y').collapse();

		SimpleMatrix dab = new SimpleMatrix(2, 2);
		SimpleMatrix dxy = new SimpleMatrix(2, 2);
		try
		{
			for (int i = 0; i < 2; i++)
			{
				for (int j = 0; j < 2; j++)
				{
					dab.set(i, j, derivAB[i][j].eval(p.getX(), p.getY(), a, b, 0));
					dxy.set(i, j, derivXY[i][j].eval(p.getX(), p.getY(), a, b, 0));
				}
			}
			return dab;//.invert().negative().mult(dxy);
		} catch (EvaluationException r)
		{
			return null;
		}
	}

//	private SimpleMatrix getDerivOfLine(sepStart s1, double a, double b) throws RootNotFound
//	{
//		SimpleMatrix V = SimpleMatrix.identity(2);
//		SimpleMatrix D = getDerivsOfSol(s1.start, a, b);
//		SimpleMatrix Vp = D.mult(V);
//		Point2D iSect = null;
//		double x = s1.start.getX();
//		double y = s1.start.getY();
//		double t0 = sepIntersect(s1, a, b, line, iSect);
//		Evaluator eval = EvaluatorFactory.getRungeKuttaEval(dx, dy);
//		Point2D next = s1.start;
//		if (s1.positive)
//			eval.initialise(x, y, 0, a, b, inc);
//		else
//			eval.initialise(x, y, 0, a, b, -inc);
//		while (eval.getT() < t0)
//		{
//			next = eval.next();
//			V = V.plus(Vp.scale(eval.getInc()));
//			Vp = getDerivsOfSol(next, a, b);
//		}
//		Point2D temp = line[0].subtract(line[1]);
//		SimpleMatrix fin = new SimpleMatrix(2, 1);
//		fin.set(0, 0, next.getX());
//		fin.set(1, 0, next.getY());
//		SimpleMatrix res = new SimpleMatrix(2,2);
//		res.set(0, 0, temp.getX() * V.get(0, 0)*temp.getX() + V.get(1, 0)*temp.getY());
//		res.set(0, 1, temp.getX() * V.get(0, 1) * temp.getX() + V.get(1,1)*temp.getY());
//		res.set(1, 0, temp.getY() * V.get(0, 0)*temp.getX() + V.get(1, 0)*temp.getY());
//		res.set(1, 1, temp.getY() * V.get(0, 1) * temp.getX() + V.get(1,1)*temp.getY());
//		return res;
//	}




	public void renderSaddleCon(Point2D start, sepStart s1, sepStart s2, boolean add)
	{
		in.saddleCanvas.getGraphicsContext2D().setStroke(in.saddleConColor);
		double aInc = 3 * (in.xMax - in.xMin) / in.c.getWidth();
		double bInc = 3 * (in.yMax - in.yMin) / in.c.getHeight();
		Point2D prev;
		Point2D next;
		Point2D temp;
		boolean justThrew = false;
		boolean isA = true;
		for (int i = 0; i < 2; i++)
		{
			try
			{
				prev = saddleConnection(s1, s2, isA, start.getX(), start.getY());
				if (add)
				{
					in.saddleCons.add(new SaddleCon(prev, s1, s2));
					add = false;
				}
			} catch (RootNotFound r)
			{
				try
				{
					isA = !isA;
					prev = saddleConnection(s1, s2, isA, start.getX(), start.getY());
					if (add)
					{
						in.saddleCons.add(new SaddleCon(prev, s1, s2));
						add = false;
					}
				} catch (RootNotFound r1)
				{
					return;
				}
			}

			while (in.inBounds(prev.getX(), prev.getY()))
			{
				if (isA) temp = new Point2D(prev.getX(), prev.getY() + bInc);
				else temp = new Point2D(prev.getX() + aInc, prev.getY());
				try
				{
					next = saddleConnection(s1, s2, isA, temp.getX(), temp.getY());
					//System.out.println(next);
					in.drawLine(prev, next, in.saddleCanvas);
					prev = next;
					justThrew = false;
				} catch (RootNotFound r)
				{
					if (justThrew) break;
					isA = !isA;
					justThrew = true;
				}
			}
			aInc = -aInc;
			bInc = -bInc;
		}
		in.gc.setStroke(Color.BLACK);
	}

	private double minDist(sepStart sep, Point2D other, double a, double b)
	{
		Evaluator eval = null;
		switch (evalType)
		{
			case Euler:
				eval = EvaluatorFactory.getEulerEval(dx, dy);
				break;
			case MidEuler:
				eval = EvaluatorFactory.getEulerMidEval(dx, dy);
				break;
			case RungeKutta:
				eval = EvaluatorFactory.getRungeKuttaEval(dx, dy);
		}
		double in;
		if(sep.positive) in = inc;
		else in = -inc;
		eval.initialise(sep.getStart((xMax - xMin)/c.getWidth()).getX(), sep.getStart((xMax - xMin)/c.getWidth()).getY(), 0, a, b, in);
		Point2D prev = sep.getStart((xMax - xMin)/c.getWidth());
		Point2D next = eval.next();
		boolean turnedAround = false;
		while (next.distance(other) < prev.distance(other) || !turnedAround)
		{
			prev = next;
			next = eval.next();
			if(!turnedAround && next.distance(other) < prev.distance(other))
				turnedAround = true;
		}
		return prev.distance(other);
	}

	private Point2D saddleConnection(sepStart s1, sepStart s2, boolean isA, double at, double bt) throws RootNotFound
	{
		long time = System.nanoTime();
		double inc = .1;
		double tol = .00000001;
		Point2D saddle1 = s1.saddle.point;
		Point2D saddle2 = s2.saddle.point;
		double dist1 = Double.MAX_VALUE;
		double dist2;
		double deriv;

		for(int i = 0; i < 10; i++)
		{
			dist1 = minDist(s1, saddle2, at, bt);
			if(isA) dist2 = minDist(s1, saddle2, at + inc, bt);
			else dist2 = minDist(s1, saddle2, at, bt + inc);
			deriv = (dist2 - dist1)/inc;
			if(deriv == 0.0)
			{
				System.out.println("throwing1");
				throw new RootNotFound();
			}
			if(isA) at = at - dist1/deriv;
			else bt = bt - dist1/deriv;
			inc = dist1/1000.;
			s1.saddle = critical(s1.saddle.point, at, bt);
			s2.saddle = critical(s2.saddle.point, at, bt);
			saddle1 = s1.saddle.point;
			saddle2 = s2.saddle.point;
			//System.out.println(i + "dist: " + dist1);
//			System.out.println("deriv: " + deriv);
//			System.out.println("a: " + at);
		}
		inc = .000001;
		while (dist1 > tol)
		{
			//System.out.println("dist: " + dist1);
			if(isA) at += inc;
			else bt += inc;
			dist2 = minDist(s1, saddle2, at, bt);
			if(dist2 - dist1 == 0 && dist1 > 2 * tol)
			{
				System.out.println("throwing");
				throw new RootNotFound();
			}
			if (dist2 > dist1)
			{
				//System.out.println("flipping");
				inc = -(inc * .5);
//				if(Math.abs(inc) <= .000000000000000001)
//				{
//					System.out.println("throwing");
//					throw new RootNotFound();
//				}
			}
			dist1 = dist2;
		}
			return new Point2D(at, bt);

//		SimpleMatrix start = new SimpleMatrix(2, 1);
//		start.setColumn(0, 0, at, bt);
//		SimpleMatrix deriv1 = getDerivOfLine(s1, at, bt);
//		SimpleMatrix deriv2 = getDerivOfLine(s2, at, bt);
//		SimpleMatrix D = deriv1.plus(deriv2);
//		Point2D isect1, isect2;
//		isect1 = null;
//		isect2 = null;
//		sepIntersect(s1, at, bt, line, isect1);
//		sepIntersect(s2, at, bt, line, isect2);
//		//maybedo fix null pointer exception here
//		double dist = isect1.distance(isect2);
//		SimpleMatrix prev = null;
//
//		for(int i = 0; i < 15; i++)
//		{
//			prev = start;
//			start = start.minus(D.invert().mult(start));
//			at = start.get(0,0);
//			bt = start.get(1,0);
//			deriv1 = getDerivOfLine(s1, at, bt);
//			deriv2 = getDerivOfLine(s2, at, bt);
//			D = deriv1.plus(deriv2);
//
//		}
//		if(Math.abs(prev.minus(start).get(0, 0)) < tol && Math.abs(prev.minus(start).get(1,0)) < tol)
//		{
//			System.out.println("found one");
//			return new Point2D(at, bt);
//		}
//		//else throw new RootNotFound();
//		else return null;

	}


	private double sepIntersect(sepStart s, double a, double b, Point2D[] ln, Point2D res) throws RootNotFound
	{
		double x, y;
		x = s.getStart((xMax - xMin)/c.getWidth()).getX();
		y = s.getStart((yMax - yMin)/c.getHeight()).getY();
		Evaluator eval;
		switch (evalType)
		{
			case Euler:
				eval = EvaluatorFactory.getEulerEval(dx, dy);
				break;
			case MidEuler:
				eval = EvaluatorFactory.getEulerMidEval(dx, dy);
				break;
			case RungeKutta:
			default:
				eval = EvaluatorFactory.getRungeKuttaEval(dx, dy);
		}
		Point2D prev;
		Point2D next;
		prev = new Point2D(x, y);
		Point2D temp;
		if (s.positive)
			eval.initialise(x, y, t, a, b, inc);
		else
			eval.initialise(x, y, t, a, b, -inc);
		while (eval.getT() < 100 + t)
		{
			next = eval.next();
			if (orientation(prev, ln) == 0)
			{
				res = prev;
				return eval.getT();
			} else if (Math.signum(orientation(prev, ln)) != Math.signum(orientation(next, ln)))
			{
				temp = prev.midpoint(next);
				double dot = (temp.getX() - ln[0].getX()) * (ln[1].getX() - ln[0].getX()) +
						(temp.getY() - ln[0].getY()) * (ln[1].getY() - ln[0].getY());
				if (0 <= dot && dot < Math.pow(ln[0].distance(ln[1]), 2))
					if (Math.signum(temp.getX() - ln[0].getX()) != Math.signum(temp.getX() - ln[1].getX()) &&
							Math.signum(temp.getY() - ln[0].getY()) != Math.signum(temp.getY() - ln[1].getY()))//!Double.isNaN(temp.getX()) && !Double.isNaN(temp.getY()))
					{
						//System.out.println(temp);
						res = prev.midpoint(next);
						return eval.getT();
					}
			}
			prev = next;
		}
		throw new RootNotFound();
	}

	private double orientation(Point2D p, Point2D ln[])
	{
		return (p.getX() - ln[0].getX()) * (ln[1].getY() - ln[0].getY()) -
				(p.getY() - ln[0].getY()) * (ln[1].getX() - ln[0].getX());
	}

	private Point2D getSource(Point2D start) throws RootNotFound
	{
		CriticalPoint temp = critical(start);
		if (temp.type != CritPointTypes.NODESOURCE && temp.type != CritPointTypes.SPIRALSOURCE)
			throw new RootNotFound();
		else return temp.point;
	}

	private Point2D getSaddle(Point2D start) throws RootNotFound
	{
		CriticalPoint temp = critical(start);
		if (temp.type != CritPointTypes.SADDLE) throw new RootNotFound();
		else return temp.point;
	}

	private CriticalPoint critical(Point2D start) throws RootNotFound
	{
		return EvaluatorFactory.getEulerEval(dx, dy).findCritical(start, a, b, t);
	}
	private CriticalPoint critical(Point2D start, double a, double b) throws RootNotFound
	{
		return EvaluatorFactory.getEulerEval(dx, dy).findCritical(start, a, b, 0);
	}

	private void updateCritical()
	{
		List<CriticalPoint> temp = new LinkedList<>();
		for (CriticalPoint c : criticalPoints)
		{
			try
			{
				temp.add(critical(c.point));
			} catch (RootNotFound ignored)
			{
			}
		}
		criticalPoints = temp;
	}

	private void labelCritical(CriticalPoint p)
	{
		if (inBounds(p.point))
		{
			c.getGraphicsContext2D().setFill(criticalColor);
			c.getGraphicsContext2D().fillOval(normToScrX(p.point.getX()) - 2.5, normToScrY(p.point.getY()) - 2.5, 5, 5);


			Label text = new Label(p.type.getStringRep());
			text.setPadding(new Insets(2));
			text.setBorder(new Border(new BorderStroke(criticalColor, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
			this.getChildren().add(text);
			text.setLayoutX(normToScrX(p.point.getX()) + 8);
			text.setLayoutY(normToScrY(p.point.getY()) - 24);
			final Text t = new Text(text.getText());

			if (text.getLayoutY() < 0)
			{
				text.setLayoutY(normToScrY(p.point.getY()) + 4);
			}
			if (text.getLayoutX() + t.getLayoutBounds().getWidth() + 4 > this.getWidth())
			{
				text.setLayoutX(normToScrX(p.point.getX()) - 12 - t.getLayoutBounds().getWidth());
			}
			text.setTextFill(criticalColor);

			text.setVisible(true);
			needsReset.add(text);
			c.getGraphicsContext2D().setFill(Color.BLACK);
		}
	}

	private void drawGraphs()
	{
		gc.setStroke(solutionColor);
		for (Node n : needsReset)
		{
			n.setVisible(false);
		}
		needsReset.clear();
		for (initCond i : initials)
		{
			drawGraph(i, true);
		}
		for (CriticalPoint p : criticalPoints)
		{
			labelCritical(p);
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawGraph(initCond init, boolean arrow)
	{
		drawGraphBack(init, arrow, '1');
	}

	private void drawGraphBack(initCond init, boolean arrow, char dir)
	{
		double x, y;
		x = init.x;
		y = init.y;
		t = init.t;
		Evaluator eval;
		switch (evalType)
		{
			case Euler:
				eval = EvaluatorFactory.getEulerEval(dx, dy);
				break;
			case MidEuler:
				eval = EvaluatorFactory.getEulerMidEval(dx, dy);
				break;
			case RungeKutta:
			default:
				eval = EvaluatorFactory.getRungeKuttaEval(dx, dy);
		}
		Point2D initialDir = eval.evaluate(x, y, a, b, t, inc);
		if (arrow) drawArrow(x, y, initialDir.getX(), initialDir.getY());
		Point2D prev;
		Point2D next;
		eval.initialise(x, y, t, a, b, inc);
		prev = new Point2D(x, y);
		if (dir != '-')
			while (eval.getT() < 100 + t)
			{
				next = eval.next();
				if (inBounds(prev) || inBounds(next))
					gc.strokeLine(normToScrX(prev.getX()), normToScrY(prev.getY()), normToScrX(next.getX()), normToScrY(next.getY()));
				prev = next;
			}
		eval.initialise(x, y, t, a, b, -inc);
		prev = new Point2D(x, y);
		if (dir != '+')
			while (eval.getT() > t - 100)
			{
				next = eval.next();
				if (inBounds(prev) || inBounds(next))
					gc.strokeLine(normToScrX(prev.getX()), normToScrY(prev.getY()), normToScrX(next.getX()), normToScrY(next.getY()));
				prev = next;
			}
	}

	private void drawIsoclines()
	{
		for (initCond c : isoclines)
		{
			drawIso(c);
		}
		for (Point2D pt : horizIsos)
		{
			drawHorizIso(pt);
		}
		for (Point2D pt : vertIsos)
		{
			drawVertIso(pt);
		}
	}

	private void drawIso(initCond init)
	{
		gc.setStroke(isoclineColor);
		try
		{
			double val = dy.eval(init.x, init.y, a, b, t) / dx.eval(init.x, init.y, a, b, t);
			AST.Node slope = Maths.minus(Maths.divide(dy, dx), new Value(val));
			drawIsoHelper(slope, new Point2D(init.x, init.y));


		} catch (EvaluationException ignored)
		{
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawHorizIso(Point2D pt)
	{
		gc.setStroke(horizIsoColor);
		try
		{
			AST.Node thing = Maths.divide(dy, dy.differentiate('y')).collapse();
			double yOld = pt.getY();
			double y = pt.getY();
			double x = pt.getX();
			for (int i = 0; i < 10; i++)
			{
				yOld = y;
				y = y - thing.eval(x, y, a, b, t);
			}
			if (Math.abs(y - yOld) < .000001)
			{
				drawIsoHelper(dy, new Point2D(x, y));
			} else
			{
				thing = Maths.divide(dy, dy.differentiate('x')).collapse();
				double xOld = pt.getX();
				y = pt.getY();
				x = pt.getX();
				for (int i = 0; i < 10; i++)
				{
					xOld = x;
					x = x - thing.eval(x, y, a, b, t);

				}
				if (Math.abs(x - xOld) < .000001)
				{
					drawIsoHelper(dy, new Point2D(x, y));
				}
			}
		} catch (EvaluationException ignored)
		{
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawVertIso(Point2D pt)
	{
		gc.setStroke(vertIsoColor);
		try
		{
			AST.Node thing = Maths.divide(dx, dx.differentiate('x')).collapse();
			double xOld = pt.getX();
			double y = pt.getY();
			double x = pt.getX();
			for (int i = 0; i < 10; i++)
			{
				xOld = x;
				x = x - thing.eval(x, y, a, b, t);

			}
			if (Math.abs(x - xOld) < .000001)
			{
				drawIsoHelper(dx, new Point2D(x, y));
			} else
			{
				thing = Maths.divide(dx, dx.differentiate('y')).collapse();
				double yOld = pt.getY();
				y = pt.getY();
				x = pt.getX();
				for (int i = 0; i < 10; i++)
				{
					yOld = y;
					y = y - thing.eval(x, y, a, b, t);
				}
				if (Math.abs(y - yOld) < .000001)
				{
					drawIsoHelper(dx, new Point2D(x, y));
				}
			}
		} catch (EvaluationException ignored)
		{
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawIsoHelper(AST.Node slope, Point2D init)
	{
		try
		{
			boolean firstTime = true;
			boolean isX = true;
			Point2D first, second;
			first = init;
			AST.Node slopeDeriv = slope.differentiate('y');
			AST.Node thing = Maths.divide(slope, slopeDeriv);
			double sign = 1;
			double tol = 30.;
			long time = System.nanoTime();
			double xinc = 1.5 * (xMax - xMin) / c.getWidth();
			double yinc = 1.5 * (yMax - yMin) / c.getHeight();
			for (int j = 0; j < 2; j++)
			{
				while (inBounds(first) && System.nanoTime() - time < 100000000)
				{
					if (isX)
					{
						double x = first.getX() + xinc;
						double yOld = first.getY();
						double y = first.getY();
						for (int i = 0; i < 10; i++)
						{
							yOld = y;
							y = y - thing.eval(x, y, a, b, t);
						}
						if (Math.abs(y - yOld) < .00001 && ((Math.abs((y - first.getY()) / (x - first.getX())) < tol) || firstTime))
						{
							firstTime = false;
							if (Math.abs((y - first.getY()) / (x - first.getX())) > 1)
							{
								isX = false;
								slopeDeriv = slope.differentiate('x');
								thing = Maths.divide(slope, slopeDeriv);
								sign = Math.signum((y - first.getY()) / (x - first.getX()));
							}
							second = new Point2D(x, y);
							gc.strokeLine(normToScrX(first.getX()), normToScrY(first.getY()), normToScrX(second.getX()), normToScrY(second.getY()));
							first = second;
						} else break;
					} else
					{
						if (sign == 0.0) break;
						double y = first.getY() + (yinc * sign);
						double xOld = first.getX();
						double x = first.getX();
						for (int i = 0; i < 10; i++)
						{
							xOld = x;
							x = x - thing.eval(x, y, a, b, t);
						}
						if (Math.abs(x - xOld) < .00001 && (Math.abs((y - first.getY()) / (x - first.getX())) > 1 / tol))
						{
							if (Math.abs((y - first.getY()) / (x - first.getX())) < 1)
							{
								isX = true;
								slopeDeriv = slope.differentiate('y');
								thing = Maths.divide(slope, slopeDeriv);
							}
							second = new Point2D(x, y);
							gc.strokeLine(normToScrX(first.getX()), normToScrY(first.getY()), normToScrX(second.getX()), normToScrY(second.getY()));
							first = second;
						} else break;
					}
				}
				xinc = -xinc;
				yinc = -yinc;
				first = init;
			}
		} catch (EvaluationException ignored)
		{
		}
	}

	private boolean inBounds(Point2D p)
	{
		return inBounds(p.getX(), p.getY());
	}

	private void drawArrow(double x, double y, double dx, double dy)
	{
		double angle = Math.toDegrees(Math.atan(-dy / dx));
		if (dx < 0) angle += 180;
		double xScr = normToScrX(x);
		double yScr = normToScrY(y);
		gc.save();
		gc.transform(new Affine(new Rotate(angle, xScr, yScr)));
		gc.strokeLine(xScr, yScr, xScr - 5, yScr + 3);
		gc.strokeLine(xScr, yScr, xScr - 5, yScr - 3);
		gc.restore();

	}

	public void drawSeparatrices()
	{
		drawSep = true;
		draw();
	}

	public void clear()
	{
		initials.clear();
		criticalPoints.clear();
		selectedSeps.clear();
		selectedCritPoints.clear();
		isoclines.clear();
		vertIsos.clear();
		horizIsos.clear();
		draw();
	}

	private void drawSep(CriticalPoint c)
	{
		double tol = .00000001;
		try
		{
			if (c.type == CritPointTypes.SADDLE)
			{
				if (c.matrix.getEigenvalue(0).getReal() > 0) gc.setStroke(stblSeparatrixColor);
				else gc.setStroke(unstblSeparatrixColor);
				initCond point1 = new initCond(c.point.getX() + tol * c.matrix.getEigenVector(0).get(0),
						c.point.getY() + tol * c.matrix.getEigenVector(0).get(1), t);
				initCond point3 = new initCond(c.point.getX() - tol * c.matrix.getEigenVector(0).get(0),
						c.point.getY() - tol * c.matrix.getEigenVector(0).get(1), t);
				drawGraphBack(point1, false, '+');
				drawGraphBack(point3, false, '+');
				if (c.matrix.getEigenvalue(1).getReal() > 0) gc.setStroke(stblSeparatrixColor);
				else gc.setStroke(unstblSeparatrixColor);
				initCond point2 = new initCond(c.point.getX() + tol * c.matrix.getEigenVector(1).get(0),
						c.point.getY() + tol * c.matrix.getEigenVector(1).get(1), t);
				initCond point4 = new initCond(c.point.getX() - tol * c.matrix.getEigenVector(1).get(0),
						c.point.getY() - tol * c.matrix.getEigenVector(1).get(1), t);
				drawGraphBack(point2, false, '-');
				drawGraphBack(point4, false, '-');
			}
		} catch (NullPointerException ignored)
		{
		}
	}

	private void drawSelectedCritPoints()
	{
		gc.setStroke(criticalColor);
		for (Point2D p : selectedCritPoints)
		{
			gc.strokeOval(normToScrX(p.getX()) - 8, normToScrY(p.getY()) - 8, 16, 16);
		}
		gc.setStroke(Color.BLACK);
	}

	@Override
	public void draw()
	{
		super.draw();
		updateCritical();
		drawGraphs();
		drawIsoclines();
		if (drawSep)
		{
			for (CriticalPoint c : criticalPoints)
			{
				drawSep(c);
			}

		}
		drawSelectedCritPoints();
		gc.setLineWidth(2);
		double inc = (xMax - xMin)/c.getWidth();
		for (sepStart s : selectedSeps)
		{
			if (s.saddle.matrix.getEigenvalue(s.eigenvector).getReal() > 0)
			{
				gc.setStroke(stblSeparatrixColor);
				drawGraphBack(new initCond(s.getStart(inc).getX(), s.getStart(inc).getY(), 0), false, '+');
			} else
			{
				gc.setStroke(unstblSeparatrixColor);
				drawGraphBack(new initCond(s.getStart(inc).getX(), s.getStart(inc).getY(), 0), false, '-');
			}
		}
		gc.setLineWidth(1);
		gc.setStroke(Color.BLACK);
	}

	public void resetZoom()
	{
		xMin = -5;
		xMax = 5;
		yMin = -5;
		yMax = 5;
		draw();
	}

	private static class initCond
	{
		public double x, y, t;

		public initCond(double x, double y, double t)
		{
			this.x = x;
			this.y = y;
			this.t = t;
		}

		public initCond(Point2D p)
		{
			this.t = t;
			this.x = p.getX();
			this.y = p.getY();
		}
	}
	@Deprecated
	private Point2D getIntersection(Point2D p1, Point2D p2, Point2D q1, Point2D q2) throws RootNotFound
	{
		double a1 = p2.getY() - p1.getY();
		double b1 = p2.getX() - p1.getX();
		double c1 = a1 * p1.getX() + b1 * p1.getY();

		double a2 = q2.getY() - q1.getY();
		double b2 = q2.getX() - q1.getX();
		double c2 = a2 * q1.getX() + b2 * q1.getY();

		double det = a1 * b2 - a2 * b1;
		if (det == 0) throw new RootNotFound();
		else
		{
			double x = (b2 * c1 - b1 * c2) / det;
			double y = (a1 * c2 - a2 * c1) / det;
			return new Point2D(x, y);
		}
	}

}
