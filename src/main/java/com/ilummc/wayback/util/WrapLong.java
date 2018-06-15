package com.ilummc.wayback.util;

public class WrapLong {

    private long l;

    public WrapLong(long l) {
        this.l = l;
    }

    public void increment() {
        l++;
    }

    public void increment(long l) {
        this.l += l;
    }

    public long get() {
        return l;
    }


}
