package me.add1.iris;

public final class Check {

    public static final boolean ON = true;

    private Check() {
        if (Check.ON) Check.shouldNeverHappen();
    }

    public static void isNull(Object o) {
        isTrue(o == null);
    }

    public static void isNotNull(Object o) {
        isTrue(o != null);
    }

    public static void isTrue(boolean condition) {
        if (Check.ON && !condition) {
            throw new AssertionError();
        }
    }

    public static void shouldNeverHappen() {
        isTrue(false);
    }
}
