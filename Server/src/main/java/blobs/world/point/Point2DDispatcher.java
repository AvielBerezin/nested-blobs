package blobs.world.point;

import java.util.function.Function;

public interface Point2DDispatcher<CartesianRes, PolarRes> {
    CartesianRes with(Cartesian cartesian);
    PolarRes with(Polar polar);

    static Point2DDispatcher<Cartesian, Polar> init() {
        return new Point2DDispatcher<>() {
            @Override
            public Cartesian with(Cartesian cartesian) {
                return cartesian;
            }

            @Override
            public Polar with(Polar polar) {
                return polar;
            }
        };
    }

    default <NewCartesianRes> Point2DDispatcher<NewCartesianRes, PolarRes> withCartesian(Function<CartesianRes, NewCartesianRes> cartesianMap) {
        return new Point2DDispatcher<>() {
            @Override
            public NewCartesianRes with(Cartesian cartesian) {
                return cartesianMap.apply(Point2DDispatcher.this.with(cartesian));
            }

            @Override
            public PolarRes with(Polar polar) {
                return Point2DDispatcher.this.with(polar);
            }
        };
    }

    default <NewPolarRes> Point2DDispatcher<CartesianRes, NewPolarRes> withPolar(Function<PolarRes, NewPolarRes> polarMap) {
        return new Point2DDispatcher<>() {
            @Override
            public CartesianRes with(Cartesian cartesian) {
                return Point2DDispatcher.this.with(cartesian);
            }

            @Override
            public NewPolarRes with(Polar polar) {
                return polarMap.apply(Point2DDispatcher.this.with(polar));
            }
        };
    }
}
