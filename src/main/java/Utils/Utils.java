package Utils;

import javafx.geometry.Point2D;
import lwon.data.Array;
import lwon.data.DataObject;
import lwon.data.Text;

public class Utils {
    public static Array pointToLwon(Point2D pt) {
        var builder = new Array.Builder();
        builder.set(new int[]{0}, new Text(Double.toString(pt.getX()), null));
        builder.set(new int[]{1}, new Text(Double.toString(pt.getY()), null));
        return builder.build(null);
    }

    public static String textValue(DataObject dobj) {
        return ((Text) dobj).value();
    }

    public static Point2D pointFromLwon(Array arr) {

        return new Point2D(Double.parseDouble(textValue((arr.get(new int[]{0})))),
                Double.parseDouble(textValue(arr.get(new int[]{1}))));
    }
}
