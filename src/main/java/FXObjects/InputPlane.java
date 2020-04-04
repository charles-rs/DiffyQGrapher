package FXObjects;

import AST.Maths;
import AST.Node;
import Exceptions.EvaluationException;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.ejml.simple.SimpleMatrix;

public class InputPlane extends CoordPlane
{
	private double a = 0;
	private double b = 0;
	private OutputPlane op;
	private TextField aField, bField;
	private Color saddleBifColor = Color.BLUE;

	public InputPlane(double side, TextField aField, TextField bField, OutputPlane op)
	{

		super(side);
		this.op = op;
		this.aField = aField;
		this.bField = bField;
		draw();
		setOnKeyPressed((e) ->
		{
			KeyCode temp = e.getCode();
			if(temp == right)
			{
				a += (xMax - xMin)/1000;
			}
			else if (temp == left)
			{
				a -= (xMax - xMin)/1000;
			}
			else if (temp == up)
			{
				b += (yMax - yMin)/1000;
			}
			else if (temp == down)
			{
				b -= (yMax - yMin)/1000;
			}
			e.consume();
			draw();
		});

		aField.textProperty().addListener((obs, s, t1) ->
		{
			try
			{
				a = Double.parseDouble(t1);
				draw();
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
				draw();
			} catch (NumberFormatException n)
			{
				if(b != 0.0)
				bField.setText(Double.toString(b));
			}
		});

	}
	@Override
	public void handleMouseClick(MouseEvent e)
	{
		if(!e.isConsumed())
		{
			a = scrToNormX(e.getX());
			b = scrToNormY(e.getY());
			draw();
		}
	}
	private void drawPoint()
	{
		gc.setFill(Color.RED);
		gc.fillOval(normToScrX(a) - 2, normToScrY(b) - 2, 4, 4);
		gc.setFill(Color.BLACK);
	}
	private void showValues()
	{
		aField.setText(Double.toString(a));
		bField.setText(Double.toString(b));
	}

	public double getA()
	{
		return a;
	}
	public double getB()
	{
		return b;
	}

	public void saddleBif(Point2D start)
	{
		gc.setStroke(saddleBifColor);
		Node dx = op.getDx();
		Node dy = op.getDy();
		Node det = Maths.minus(Maths.mult(dx.differentiate('x'), dy.differentiate('y')),
				Maths.mult(dx.differentiate('y'), dy.differentiate('x'))).collapse();
		Node derivative [][] = new Node[3][4];
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

		double first [] = new double[]{start.getX(), start.getY(), a, b};
		double second [] = bifHelp(first[0], first[1], first[2], first[3], dx, dy, det, derivative, 'a');
		while(inBounds(first[2], first[3]) && second != null)
		{
			gc.strokeLine(first[2], first[3], second[2], second[3]);
		}


		gc.setStroke(Color.BLACK);
	}
	private double bifHelp(double x, double y, double aTemp, double bTemp, Node n1, Node n2, Node n3, Node derivative[][], char cons) []
	{
		double t = op.getT();
		double temp[] = new double[4];
		try
		{
			double xt = x;
			double yt = y;
			double at = aTemp;
			double bt = bTemp;
			for(int j = 0; j < 10; j++)
			{
				SimpleMatrix init = new SimpleMatrix(3, 1);
				init.setColumn(0, 0, xt, yt);
				if (cons == 'a') init.set(3, 0, bt);
				else init.set(3, 0, at);
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
				if(Math.pow(init.get(0,0) - result.get(0,0), 2) +
						Math.pow(init.get(1,0) - result.get(1,0), 2) +
						Math.pow(init.get(2,0) - result.get(2,0), 2) < .0001)
					return temp;
			}
		} catch (EvaluationException e)
		{
			return null;
		}
		return null;
	}

	@Override
	public void draw()
	{
		super.draw();
		showValues();
		drawPoint();
	}


}
