package com.datapublica.diachron.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class StreamUtil {
    public static <E> Stream<E> stream(Iterable<E> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
}
