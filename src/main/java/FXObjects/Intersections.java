package FXObjects;

import javafx.geometry.Point2D;
import Exceptions.RootNotFound;

public class Intersections {
    /**
     * test if q lies on segment pr if they are colinear
     * 
     * @param p start
     * @param q point to test
     * @param r end
     * @return whether or not q is on pr
     */
    public static boolean onSegment(Point2D p, Point2D q, Point2D r) {
        return q.getX() <= Math.max(p.getX(), r.getX()) && q.getX() >= Math.min(p.getX(), r.getX())
                && q.getY() <= Math.max(p.getY(), r.getY())
                && q.getY() >= Math.min(p.getY(), r.getY());
    }

    /**
     * To find orientation of ordered triplet (p, q, r). The function returns following values 0 -->
     * p, q and r are colinear 1 --> Clockwise 2 --> Counterclockwise
     */
    public static int orientation(Point2D p, Point2D q, Point2D r) {
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX())
                - (q.getX() - p.getX()) * (r.getY() - q.getY());

        if (val == 0/* < Math.ulp(1.0) */)
            return 0; // colinear

        return (val > 0) ? 1 : 2; // clock or counterclock wise
    }

    /**
     * The main function that returns true if line segment 'p1q1' and 'p2q2' intersect.
     * 
     * @param p1 first start
     * @param q1 first end
     * @param p2 second start
     * @param q2 second end
     * @return the intersection
     * @throws RootNotFound if they dont intersect
     */

    public static Point2D getIntersection(Point2D p1, Point2D q1, Point2D p2, Point2D q2)
            throws RootNotFound {
        // Find the four orientations needed for general and
        // special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return new Point2D(
                    ((p1.getX() * q1.getY() - p1.getY() * q1.getX()) * (p2.getX() - q2.getX())
                            - (p1.getX() - q1.getX())
                                    * (p2.getX() * q2.getY() - p2.getY() * q2.getX()))
                            / ((p1.getX() - q1.getX()) * (p2.getY() - q2.getY())
                                    - (p1.getY() - q1.getY()) * (p2.getX() - q2.getX())),
                    ((p1.getX() * q1.getY() - p1.getY() * q1.getX()) * (p2.getY() - q2.getY())
                            - (p1.getY() - q1.getY())
                                    * (p2.getX() * q2.getY() - p2.getY() * q2.getX()))
                            / ((p1.getX() - q1.getX()) * (p2.getY() - q2.getY())
                                    - (p1.getY() - q1.getY()) * (p2.getX() - q2.getX())));

        // Special Cases
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1))
            return p2;

        // p1, q1 and q2 are colinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1))
            return q2;

        // p2, q2 and p1 are colinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2))
            return p1;

        // p2, q2 and q1 are colinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2))
            return q1;

        throw new RootNotFound();
    }
}
