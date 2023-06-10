package FXObjects;

import javafx.geometry.Point2D;
import lwon.data.Dictionary;
import lwon.data.Text;

import static Utils.Utils.textValue;

class InitCond {
    public double x, y, t;

    public InitCond(double x, double y, double t) {
        this.x = x;
        this.y = y;
        this.t = t;
    }

    public InitCond(Point2D p) {
        this.t = 0;
        this.x = p.getX();
        this.y = p.getY();
    }

    public InitCond(Dictionary lwon) {
        this.t = Double.parseDouble(textValue(lwon.get("t").get(0)));
        this.x = Double.parseDouble(textValue(lwon.get("x").get(0)));
        this.y = Double.parseDouble(textValue(lwon.get("y").get(0)));
    }

    public Dictionary toLwon() {
        var builder = new Dictionary.Builder();
        builder.put("x", new Text(Double.toString(x), null));
        builder.put("y", new Text(Double.toString(y), null));
        builder.put("t", new Text(Double.toString(t), null));
        return builder.build(null);
    }
}
