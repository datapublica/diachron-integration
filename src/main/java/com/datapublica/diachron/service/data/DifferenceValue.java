package com.datapublica.diachron.service.data;

/**
 * Created by loic on 16/12/2015.
 */
public class DifferenceValue<E> {
    private E before;
    private E after;

    public DifferenceValue(E before, E after) {
        this.before = before;
        this.after = after;
    }

    public E getBefore() {
        return before;
    }

    public E getAfter() {
        return after;
    }
}
