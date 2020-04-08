package FXObjects;

import AST.Derivative;
import AST.Maths;
import AST.Value;
import Evaluation.*;
import Events.SaddleSelected;
import Events.SourceSelected;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
	private Point2D line [] = new Point2D[2];
	private CriticalPoint currentPoint = null;

	private Color solutionColor = Color.BLACK;
	private Color isoclineColor = Color.BLUE;
	private Color horizIsoColor = Color.PURPLE;
	private Color vertIsoColor = Color.ORANGE;
	private Color stblSeparatrixColor = Color.ORANGERED;
	private Color unstblSeparatrixColor = Color.DARKCYAN;
	private Color criticalColor = Color.RED;
	private Color lnColor = Color.CADETBLUE;


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
		selectedSeps = new LinkedList<>();
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
		line = new Point2D[2];
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
				} catch (RootNotFound ignored) {}
				break;
			case SELECTSOURCE:
				try
				{
					Point2D p = getSource(pt);
					fireEvent(new SourceSelected(p));
					selectedCritPoints.add(p);
					drawSelectedCritPoints();
				} catch (RootNotFound ignored) {}
			case DRAWSEG:
				if(line[0] == null)
				{
					line[0] = pt;
				} else
				{
					line[1] = pt;
					gc.setStroke(lnColor);
					drawLine(line[0], line[1]);
					clickMode = ClickModeType.DRAWPATH;
					gc.setStroke(Color.BLACK);
					selectedCritPoints.clear();
					clickMode = ClickModeType.SELECTSEP;
				}
				break;
			case SELECTSEP:
				try
				{
					if(currentPoint != null && selectedSeps.size() < 2)
					{
						double tol = .00001;
						Point2D p1 = new Point2D(currentPoint.point.getX() + tol * currentPoint.matrix.getEigenVector(0).get(0),
								currentPoint.point.getY() + tol * currentPoint.matrix.getEigenVector(0).get(1));
						Point2D p2 = new Point2D(currentPoint.point.getX() - tol * currentPoint.matrix.getEigenVector(0).get(0),
								currentPoint.point.getY() - tol * currentPoint.matrix.getEigenVector(0).get(1));

						Point2D p3 = new Point2D(currentPoint.point.getX() + tol * currentPoint.matrix.getEigenVector(1).get(0),
								currentPoint.point.getY() + tol * currentPoint.matrix.getEigenVector(1).get(1));
						Point2D p4 = new Point2D(currentPoint.point.getX() - tol * currentPoint.matrix.getEigenVector(1).get(0),
								currentPoint.point.getY() - tol * currentPoint.matrix.getEigenVector(1).get(1));
						double d1 = pt.distance(p1);
						double d2 = pt.distance(p2);
						double d3 = pt.distance(p3);
						double d4 = pt.distance(p4);
						double min = Math.min(Math.min(d1, d2), Math.min(d3, d4));
						boolean firstPos = currentPoint.matrix.getEigenvalue(0).getReal() > 0;
						if(d1 == min)
							selectedSeps.add(new sepStart(p1, firstPos));
						else if(d2 == min)
							selectedSeps.add(new sepStart(p2, firstPos));
						else if(d3 == min)
							selectedSeps.add(new sepStart(p3, !firstPos));
						else
							selectedSeps.add(new sepStart(p4, !firstPos));
						if(selectedSeps.size() == 2)
							clickMode = ClickModeType.DRAWPATH;
					} else if(currentPoint == null)
					{
						currentPoint = critical(pt);
						selectedCritPoints.add(currentPoint.point);
					}
				} catch (RootNotFound ignored) {}
		}

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
		line = new Point2D[2];
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
		{}
	}
	private void drawSelectedCritPoints()
	{
		gc.setStroke(criticalColor);
		for(Point2D p : selectedCritPoints)
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
		if(line[1] != null)
		{
			gc.setStroke(lnColor);
			drawLine(line[0], line[1]);
		}
		drawSelectedCritPoints();
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
	private static class sepStart
	{
		public Point2D start;
		public boolean positive;

		public sepStart(Point2D s, boolean p)
		{
			start = s;
			positive = p;
		}
	}

}
