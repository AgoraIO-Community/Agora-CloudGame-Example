package io.agora.cloudgame.model;

public class TrackInfo {
    public final String trace;
    public final String ref;
    public final String origin;

    private TrackInfo(String trace, String ref, String origin) {
        this.trace = trace;
        this.ref = ref;
        this.origin = origin;
    }

    public static TrackInfo obtain(String trace, String ref, String origin) {
        return new TrackInfo(trace, ref, origin);
    }
}
