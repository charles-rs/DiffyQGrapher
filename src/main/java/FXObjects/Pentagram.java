package FXObjects;


import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Pentagram extends Parent
{
	private byte info [];
	public final Rectangle border = new Rectangle();
	public final Point2D mathLoc;
	public final Text
			source = new Text(),
			sink = new Text(),
			saddle = new Text(),
			attCyc = new Text(),
			repCyc = new Text();

	private void init()
	{
		updateTexts();
		this.getChildren().addAll(border, source, sink, saddle, attCyc, repCyc);
		border.setStroke(Color.BLACK);
		border.setFill(Color.TRANSPARENT);
		border.setWidth(30);
		border.setHeight(30);
//		border.xProperty().bind(this.layoutXProperty());
//		border.yProperty().bind(this.layoutYProperty());
//		source.xProperty().bind(border.xProperty().add(3));
//		source.yProperty().bind(border.yProperty().add(3));
		sink.setX(2);
		sink.setY(9);
		source.setX(border.getWidth() - 8);
		source.setY(9);
		attCyc.setX(2);
		attCyc.setY(border.getHeight() - 3);
		repCyc.setX(border.getWidth() - 8);
		repCyc.setY(border.getHeight() - 3);
		saddle.setX(border.getWidth()/2 - 3);
		saddle.setY(border.getHeight()/2 + 2);
//		attCyc.xProperty().bind(border.xProperty().add(3));
//		attCyc.yProperty().bind(border.yProperty().add(border.heightProperty()).subtract(10));
//		sink.xProperty().bind(border.xProperty().add(border.widthProperty()).subtract(10));
//		sink.yProperty().bind(border.yProperty().add(3));
//		repCyc.yProperty().bind(border.yProperty().add(border.heightProperty()).subtract(10));
//		repCyc.xProperty().bind(border.xProperty().add(border.widthProperty()).subtract(10));
		for(Node n : this.getChildren()) n.setVisible(true);
		setVisible(true);
	}
	public Pentagram (byte info [], Point2D litLoc, Point2D mathLoc)
	{
		assert (info != null);
		assert (info.length == 5);
		assert (litLoc != null);
		assert (mathLoc != null);
		this.mathLoc = mathLoc;
		this.info = info;
		this.layoutXProperty().set(litLoc.getX());
		this.layoutYProperty().set(litLoc.getY());
		init();
	}
	public Pentagram(Point2D litLoc, Point2D mathLoc)
	{
		assert litLoc != null;
		assert mathLoc != null;
		this.mathLoc = mathLoc;
		info = new byte [5];
		this.layoutXProperty().set(litLoc.getX());
		this.layoutYProperty().set(litLoc.getY());
		init();
	}
	public void updateTexts()
	{
		Font f = new Font(8);
		source.setText(String.valueOf(getSources()));
		source.setFont(f);
		sink.setText(String.valueOf(getSinks()));
		sink.setFont(f);
		saddle.setText(String.valueOf(getSaddles()));
		saddle.setFont(f);
		attCyc.setText(String.valueOf(getAttrCycles()));
		attCyc.setFont(f);
		repCyc.setText(String.valueOf(getRepelCycles()));
		repCyc.setFont(f);
	}

	/**
	 * method to set the info array all at once. Uses bInfo, and if it is null, uses nInfo, and converts to bytes
	 * @param bInfo a byte array to set as info
	 * @param nInfo an int array to set as info if bInfo is null
	 */
	public void setInfo(byte bInfo [], int[] nInfo)
	{
		if(bInfo != null)
		{
			assert bInfo.length == 5;
			this.info = bInfo;
		} else
		{
			assert nInfo != null;
			assert nInfo.length == 5;
			for (int i = 0; i < 5; i++)
			{
				this.info [i] = (byte) nInfo [i];
			}
		}
	}
	public int getSinks()
	{
		return info[0];
	}
	public int getSources()
	{
		return info[1];
	}
	public int getSaddles()
	{
		return info[2];
	}
	public int getAttrCycles()
	{
		return info[3];
	}
	public int getRepelCycles()
	{
		return info[4];
	}


}
