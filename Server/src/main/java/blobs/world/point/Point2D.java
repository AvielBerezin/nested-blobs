package blobs.world.point;

public interface Point2D {
    <Res> Res dispatch(Point2DDispatcher<Res, Res> dispatcher);

    default Polar asPolar() {
        return this.dispatch(Point2DDispatcher.init().withCartesian(Cartesian::toPolar));
    }

    default Cartesian asCartesian() {
        return this.dispatch(Point2DDispatcher.init().withPolar(Polar::toCartesian));
    }

    Point2D negate();
    Point2D multiply(double factor);
}
