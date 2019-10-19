package com.digitalent.submission.movieworld.util;

public class JavaHelper {

    public static int long2int(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

}
