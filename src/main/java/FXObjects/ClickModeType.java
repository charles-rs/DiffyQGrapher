package FXObjects;

/**
 * Various modes for the output plane
 */
public enum ClickModeType {
    DRAWPATH,
    DRAWISO,
    DRAWHORIZISO,
    DRAWVERTISO,
    SELECTSADDLE,
    SELECTHOPFPOINT,
    SELECTSEP,
    FINDLIMCYCLE,
    SEMISTABLE,
    DRAWBASIN,
    DRAWCOBASIN,
    SELECTHOMOCENTER,
    FINDCRITICAL,
    SETTRAVERSAL,
    SETDIRECTION,
    LINEARISATION;

    @Override
    public String toString() {
        return switch (this) {
            case DRAWPATH -> "Solution";
            case DRAWISO -> "Isocline";
            case DRAWHORIZISO -> "Horizontal isocline";
            case DRAWVERTISO -> "Vertical isocline";
            case SELECTSADDLE -> "Select saddle";
            case SELECTHOPFPOINT -> "Select Hopf point";
            case SELECTSEP -> "Select separatrix";
            case FINDLIMCYCLE -> "Find limit cycle";
            case SEMISTABLE -> "Find Semistable limit cycle";
            case DRAWBASIN -> "Draw Basin";
            case DRAWCOBASIN -> "Draw Cobasin";
            case SELECTHOMOCENTER -> "Select center of saddle loop";
            case FINDCRITICAL -> "Find zero";
            case SETTRAVERSAL -> "Set traversal";
            case SETDIRECTION -> "Set direction";
            case LINEARISATION -> "View linearization";
        };
    }
}
