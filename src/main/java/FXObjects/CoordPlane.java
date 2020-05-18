package FXObjects;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class CoordPlane extends Pane
{
	protected Canvas c;
	protected double xMin, xMax, yMin, yMax;
	protected GraphicsContext gc;
	static public CoordPlane selected;
	protected KeyCode right, left, up, down;
	protected Rectangle zoomBox;


	public CoordPlane (double side)
	{
		xMin = -5;
		yMin = -5;
		xMax = 5;
		yMax = 5;

		right = KeyCode.RIGHT;
		left = KeyCode.LEFT;
		up = KeyCode.UP;
		down = KeyCode.DOWN;

		setMinSize(100, 100);
		setWidth(side);
		setHeight(side);

		setPrefHeight(side);
		setPrefWidth(side);
		c = new Canvas(side, side);
		gc = c.getGraphicsContext2D();
		getChildren().add(c);
		c.widthProperty().bind(this.widthProperty());
		c.heightProperty().bind(this.heightProperty());

		/*
		this.widthProperty().addListener((obs, oldVal, newVal) ->
		{
			setHeight((Double) newVal);
			if(oldVal.doubleValue() != newVal.doubleValue())
			{
				System.out.println(oldVal);
				System.out.println(newVal);
				drawAxes();
			}
		});
		this.heightProperty().addListener((obs, oldVal, newVal) ->
		{
			setWidth((Double) newVal);
			if(oldVal.doubleValue() != newVal.doubleValue())
				drawAxes();
		});*/
		zoomBox = new Rectangle(0,0,0,0);
		zoomBox.setVisible(false);
		zoomBox.setStroke(Color.BLACK);
		zoomBox.setFill(Color.TRANSPARENT);
		this.getChildren().add(zoomBox);
		zoomBox.toFront();
		addEventFilter(MouseEvent.ANY, mouseEvent ->
		{
			if(this == selected)
			{

				if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED)
				{
					zoomBox.setX(mouseEvent.getX());
					zoomBox.setY(mouseEvent.getY());
					zoomBox.setVisible(true);

				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED && zoomBox.isVisible())
				{
					zoomBox.setWidth(mouseEvent.getX() - zoomBox.getX());
					zoomBox.setHeight(mouseEvent.getY() - zoomBox.getY());

				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED && zoomBox.getHeight() != 0
						&& zoomBox.getWidth() != 0)
				{
					zoomBox.setVisible(false);
					if(zoomBox.getWidth() > 5 && zoomBox.getHeight() > 5)
					{
						double xMinTemp, xMaxTemp, yMinTemp, yMaxTemp;
						if(mouseEvent.isControlDown())
						{
							xMaxTemp = (xMax - scrToNormX(zoomBox.getX() + zoomBox.getWidth())) * (xMax - xMin) / (scrToNormX(zoomBox.getX() + zoomBox.getWidth()) - scrToNormX(zoomBox.getX())) + xMax;
							xMinTemp = (xMin - scrToNormX(zoomBox.getX())) * (xMax - xMin) / (scrToNormX(zoomBox.getX() + zoomBox.getWidth()) - scrToNormX(zoomBox.getX())) + xMin;

							yMaxTemp = (yMax - scrToNormY(zoomBox.getY())) * (yMax - yMin) / (scrToNormY(zoomBox.getY() - zoomBox.getHeight()) - scrToNormY(zoomBox.getY())) + yMax;
							yMinTemp = (yMin - scrToNormY(zoomBox.getY() + zoomBox.getHeight())) * (yMax - yMin) / (scrToNormY(zoomBox.getY() - zoomBox.getHeight()) - scrToNormY(zoomBox.getY())) + yMin;
						} else
						{
							xMaxTemp = scrToNormX(zoomBox.getX() + zoomBox.getWidth());
							xMinTemp = scrToNormX(zoomBox.getX());
							yMaxTemp = scrToNormY(zoomBox.getY());
							yMinTemp = scrToNormY(zoomBox.getY() + zoomBox.getHeight());
						}
						zoomBox.setWidth(0);
						zoomBox.setHeight(0);
						xMin = xMinTemp;
						yMin = yMinTemp;
						xMax = xMaxTemp;
						yMax = yMaxTemp;
						draw();
					}
					mouseEvent.consume();
				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED)
				{
					handleMouseClick(mouseEvent);
				}
			}
			else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED)
			{
				selectMe(mouseEvent);
			}
		});
		setVisible(true);
		c.setVisible(true);

	}

	protected void selectMe(MouseEvent e)
	{
			requestFocus();
			CoordPlane temp = selected;
			selected = this;
			e.consume();
			if(temp != null)
				temp.drawBorders();
			drawBorders();
	}
	public void drawBorders()
	{
		if(this == selected)
		{
			gc.setFill(Color.BLUE);
		}
		gc.fillRect(0,0,2,c.getHeight());
		gc.fillRect(0,0,c.getWidth(),2);
		gc.fillRect(0, c.getHeight() - 2, c.getWidth(), 2);
		gc.fillRect(c.getWidth() - 2, 0, 2, c.getHeight());
		if(this == selected) gc.setFill(Color.BLACK);
	}

	public void draw()
	{
		drawAxes();
		drawBorders();
	}


	public void drawAxes()
	{
		String strxMin, strxMax, stryMin, stryMax;
		strxMin = String.valueOf(xMin);
		strxMax = String.valueOf(xMax);
		stryMin = String.valueOf(yMin);
		stryMax = String.valueOf(yMax);
		try
		{
			strxMin = strxMin.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		try
		{
			strxMax = strxMax.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		try
		{
			stryMin = stryMin.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		try
		{
			stryMax = stryMax.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		gc.clearRect(0, 0, c.getWidth(), c.getHeight());
		gc.strokeLine(0, normToScrY(0), c.getWidth(), normToScrY(0));
		gc.strokeLine(normToScrX(0), 0, normToScrX(0), c.getHeight());
		gc.fillText(strxMin, 2, c.getHeight()/2 - 2);
		gc.fillText(stryMin, c.getWidth()/2 + 2, c.getHeight() - 2);
		gc.fillText(stryMax, c.getWidth()/2 + 2, 12);
		gc.fillText(strxMax, c.getWidth() - 7 * strxMax.length(), c.getHeight()/2 - 4);
	}

	protected boolean inBounds(double x, double y)
	{
		return x <= xMax && x >= xMin && y <= yMax && y >= yMin;
	}

	public void handleMouseClick(MouseEvent e)
	{
		throw new UnsupportedOperationException();
	}

	protected double scrToNormX(double x)
	{
		return (x / c.getWidth() + (xMin/(xMax - xMin))) * (xMax - xMin);
	}
	protected double scrToNormY(double y)
	{
		return (y / c.getHeight() + (yMax / (yMin - yMax))) * (yMin - yMax);
	}
	protected double normToScrX(double x)
	{
		return (x / (xMax - xMin) - (xMin/(xMax - xMin))) * c.getWidth();
	}
	protected double normToScrY(double y)
	{
		return (y / (yMin - yMax) - (yMax / (yMin - yMax))) * c.getHeight();
	}
	protected void drawLine(double x1, double y1, double x2, double y2)
	{
		gc.strokeLine(normToScrX(x1), normToScrY(y1), normToScrX(x2), normToScrY(y2));
	}
	protected void drawLine(Point2D p1, Point2D p2)
	{
		drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
	protected void drawLine(double x1, double y1, double x2, double y2, Canvas can)
	{
		can.getGraphicsContext2D().strokeLine(normToScrX(x1), normToScrY(y1), normToScrX(x2), normToScrY(y2));
	}
	protected void drawLine(Point2D p1, Point2D p2, Canvas can)
	{
		drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), can);
	}
}
