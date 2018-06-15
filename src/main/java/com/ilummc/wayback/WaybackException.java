package com.ilummc.wayback;

import java.io.PrintStream;
import java.io.PrintWriter;

public class WaybackException extends RuntimeException {

    private Throwable e;

    public WaybackException(Throwable e) {
        this.e = e;
    }

    public WaybackException() {
        super();
    }

    public WaybackException(String s) {
        super(s);
    }

    @Override
    public synchronized Throwable getCause() {
        return e;
    }

    @Override
    public void printStackTrace() {
        if (e != null)
            e.printStackTrace();
        else super.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        if (e != null) {
            e.printStackTrace(s);
        } else super.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        if (e != null) {
            e.printStackTrace(s);
        } else super.printStackTrace(s);
    }

}
