package blobs.world.pivoted;

import java.util.function.Function;

public interface PivotedBlobViewHomeDispatcher<EmptyRes, HomeRes> {
    EmptyRes with(PivotedBlobViewHome.Empty empty);
    HomeRes with(PivotedBlobViewHome.Home home);

    static PivotedBlobViewHomeDispatcher<PivotedBlobViewHome.Empty, PivotedBlobViewHome.Home> init() {
        return new PivotedBlobViewHomeDispatcher<>() {
            @Override
            public PivotedBlobViewHome.Empty with(PivotedBlobViewHome.Empty empty) {
                return empty;
            }

            @Override
            public PivotedBlobViewHome.Home with(PivotedBlobViewHome.Home home) {
                return home;
            }
        };
    }

    default <NewEmptyRes> PivotedBlobViewHomeDispatcher<NewEmptyRes, HomeRes> withEmpty(Function<EmptyRes, NewEmptyRes> emptyMap) {
        return new PivotedBlobViewHomeDispatcher<>() {
            @Override
            public NewEmptyRes with(PivotedBlobViewHome.Empty empty) {
                return emptyMap.apply(PivotedBlobViewHomeDispatcher.this.with(empty));
            }

            @Override
            public HomeRes with(PivotedBlobViewHome.Home home) {
                return PivotedBlobViewHomeDispatcher.this.with(home);
            }
        };
    }

    default <NewHomeRes> PivotedBlobViewHomeDispatcher<EmptyRes, NewHomeRes> withHome(Function<HomeRes, NewHomeRes> homeMap) {
        return new PivotedBlobViewHomeDispatcher<>() {
            @Override
            public EmptyRes with(PivotedBlobViewHome.Empty empty) {
                return PivotedBlobViewHomeDispatcher.this.with(empty);
            }

            @Override
            public NewHomeRes with(PivotedBlobViewHome.Home home) {
                return homeMap.apply(PivotedBlobViewHomeDispatcher.this.with(home));
            }
        };
    }
}
