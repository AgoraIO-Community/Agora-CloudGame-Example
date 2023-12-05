package me.add1.iris.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FeedType {
    int INVALID = 0;
    int LOAD_MORE = 0x1;
    int EMPTY = 0x2;
    int PRELOAD = 0x3;
    int OFFSET = 0x4;

    int DATA_MASK = 0x1000;
    int ACTIVE_MASK = 0x2000;
    int type() default INVALID;
}
