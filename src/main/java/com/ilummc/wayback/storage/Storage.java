package com.ilummc.wayback.storage;

import com.ilummc.wayback.data.Breakpoint;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public interface Storage {

    boolean init();

    long space();

    File createTempFile(File base, String suffix) throws IOException;

    Optional<Breakpoint> findLast();

}
