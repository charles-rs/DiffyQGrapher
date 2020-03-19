package FXObjects;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class InputPlane extends CoordPlane
{
	private double a = 0;
	private double b = 0;
	private TextField aField, bField;

	public InputPlane(double side, TextField aField, TextField bField)
	{

		super(side);
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
	@Override
	public void draw()
	{
		super.draw();
		showValues();
		drawPoint();
	}


}
