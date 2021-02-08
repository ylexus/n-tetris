package net.yudichev.ntetris.game;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.O;
import static net.yudichev.ntetris.game.ShapeConstants.X;

enum Shape {
    Q(pattern(
            row(X, X),
            row(X, X)
    )),
    Z(pattern(
            row(X, X, O),
            row(O, X, X)
    )),
    S(pattern(
            row(O, X, X),
            row(X, X, O)
    )),
    T(pattern(
            row(X, X, X),
            row(O, X, O)
    )),
    I(pattern(
            row(X, X, X, X)
    )),
    L(pattern(
            row(X, O),
            row(X, O),
            row(X, X)
    )),
    J(pattern(
            row(O, X),
            row(O, X),
            row(X, X)
    )),
    ;

    private final RectangularPattern thePattern;

    Shape(RectangularPattern thePattern) {
        this.thePattern = checkNotNull(thePattern);
    }

    public RectangularPattern getPattern() {
        return thePattern;
    }
}
