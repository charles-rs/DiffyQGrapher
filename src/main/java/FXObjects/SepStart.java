package FXObjects;

import Evaluation.CritPointTypes;
import Evaluation.CriticalPoint;
import Exceptions.RootNotFound;
import javafx.geometry.Point2D;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;

/**
 * Class representing separatrices. able to return their initial point.
 * Three important pieces of info: the actual saddle point, which eigenvector, and whether to go with or
 * against that eigenvector. This is stored in an int called state. The least significant bit is the eigenvector,
 * with 0 corresponding to the negative one, and 1 to the positive and the second least significant is whether it it
 * positive or negative
 */

public class SepStart implements Cloneable {
    /**
     * The saddle we start at
     */
    public final CriticalPoint saddle;
    private final int state;

    /**
     * Private constructor used to construct a sepStart if we already have a valid state int
     *
     * @param s  the saddle point
     * @param st the state: must be valid
     */
    private SepStart(CriticalPoint s, int st) {
        this.saddle = s;
        this.state = st;
    }

    /**
     * Public constructor for a sepStart. Starts at s, uses the positive eigenvector if posEig
     * and the positive direction if posDir
     *
     * @param s      the starting saddle (should be a saddle, potentially changes to not a saddle. This is up to the user
     *               to catch)
     * @param posDir whether or not we go in the positive direction. Eigenvectors should be normalised to point right
     * @param posEig whether or not we use the positive eigenvector (the eigenvector associated with the positive
     *               eigenvalue)
     */
    public SepStart(CriticalPoint s, boolean posDir, boolean posEig) {
        int state1;
        saddle = s;

        if (saddle.matrix.getEigenVector(0).get(0) < 0)
            saddle.matrix.getEigenVector(0).set(saddle.matrix.getEigenVector(0).negative());
        if (saddle.matrix.getEigenVector(1).get(0) < 0)
            saddle.matrix.getEigenVector(1).set(saddle.matrix.getEigenVector(1).negative());
        state1 = 0;
        if (posEig) state1 = 1;
        if (posDir) state1 |= 2;

        state = state1;
    }

    /**
     * Extract whether or not this separatrix goes in the positive direction or not from the state
     *
     * @return the boolean that is whether or not the separatrix goes in the positive direction
     */
    public boolean posDir() {
        return 1 == (state >> 1 & 1);
    }

    /**
     * Extract whether or not this separatrix uses the positive eigenvector or not from the state
     *
     * @return the boolean that is whether or not the separatrix uses the positive eigenvector
     */
    public boolean posEig() {
        return 1 == (1 & state);
    }

    /**
     * gets the start of this separatrix, at distance inc from the saddle
     *
     * @param inc how far from the saddle to go
     * @return the start of the separatrix
     */
    public Point2D getStart(double inc) {
        if (saddle.type != CritPointTypes.SADDLE) {
            System.out.println("not a saddle");
        }
        SimpleBase<SimpleMatrix> eig;
        if (1 == (state & 1)) {
            if (saddle.matrix.getEigenvalue(0).getReal() > 0)
                eig = saddle.matrix.getEigenVector(0);
            else eig = saddle.matrix.getEigenVector(1);
        } else {
            if (saddle.matrix.getEigenvalue(1).getReal() > 0)
                eig = saddle.matrix.getEigenVector(0);
            else eig = saddle.matrix.getEigenVector(1);
        }

        if (1 == (state >> 1 & 1)) {
            return new Point2D(saddle.point.getX() + inc * eig.get(0), saddle.point.getY() + inc * eig.get(1));
        } else {
            return new Point2D(saddle.point.getX() - inc * eig.get(0), saddle.point.getY() - inc * eig.get(1));
        }

    }

    public double getInc(double inc) {
        if (this.posEig()) return inc;
        else return -inc;
    }

    /**
     * Flips the direction of the separatrix. Note: separatrices are immutable, so returns a new one
     *
     * @param other the separatrix to flip
     * @return the flipped separatrix
     */
    public static SepStart flip(final SepStart other) {
        return new SepStart(other.saddle, !other.posDir(), other.posEig());
    }

    /**
     * returns a new separatrix with it's saddle point updated to sad
     *
     * @param sad the new saddle
     * @return the updated separatrix
     */
    public SepStart updateSaddle(CriticalPoint sad) throws RootNotFound {

        if (sad.type == CritPointTypes.SADDLE) {
            if (sad.matrix.getEigenVector(0).minus(saddle.matrix.getEigenVector(0)).normF() >
                    sad.matrix.getEigenVector(0).minus(saddle.matrix.getEigenVector(0).negative()).normF()) {
                System.out.println("negated first");
                sad.matrix.getEigenVector(0).set(sad.matrix.getEigenVector(0).negative());
            }
            if (sad.matrix.getEigenVector(1).minus(saddle.matrix.getEigenVector(1)).normF() >
                    sad.matrix.getEigenVector(1).minus(saddle.matrix.getEigenVector(1).negative()).normF()) {
                System.out.println("negated second");
                sad.matrix.getEigenVector(1).set(sad.matrix.getEigenVector(1).negative());
            }
            return new SepStart(sad, this.state);
        } else throw new RootNotFound();
    }

    @Override
    public SepStart clone() {
        return new SepStart(this.saddle, this.state);
    }

    public SepStart update(Point2D p, OutputPlane o) throws RootNotFound {
        var new_sad = o.critical(saddle.point, p);
        return updateSaddle(new_sad);
    }
}
