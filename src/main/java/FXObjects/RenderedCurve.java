package FXObjects;

import javafx.geometry.Point2D;
import lwon.data.Array;
import lwon.data.Dictionary;
import lwon.data.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static Utils.Utils.*;

public class RenderedCurve {
    Point2D start;
    Color color;
    public List<Point2D> left = new ArrayList<>(), right = new ArrayList<>();

    public RenderedCurve(Dictionary lwon) {
        start = pointFromLwon((Array) lwon.get("start").get(0));
        color = new Color(Integer.parseInt(textValue(lwon.get("color").get(0))));
        left = pointsOfArray((Array) lwon.get("left").get(0));
        right = pointsOfArray((Array) lwon.get("right").get(0));
    }

    public RenderedCurve() {
    }


    public static Array arrayOfPoints(List<Point2D> pts) {
        var builder = new Array.Builder();
        for (int i = 0; i < pts.size(); ++i) {
            builder.set(new int[]{i}, pointToLwon(pts.get(i)));
        }
        return builder.build(null);
    }

    public static List<Point2D> pointsOfArray(Array arr) {
        var lst = new ArrayList<Point2D>();
        for (var elem : arr) {
            if (elem instanceof Array v)
                lst.add(pointFromLwon(v));
            else break;
        }
        return lst;
    }

    public Dictionary toLwon() {
        var builder = new Dictionary.Builder();
        builder.put("start", pointToLwon(start));
        builder.put("color", new Text(Integer.toString(color.getRGB()), null));
        builder.put("left", arrayOfPoints(left));
        builder.put("right", arrayOfPoints(right));
        return builder.build(null);
    }


}
