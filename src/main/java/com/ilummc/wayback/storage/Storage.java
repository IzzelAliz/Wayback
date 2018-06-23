package com.ilummc.wayback.storage;

import com.ilummc.wayback.data.Breakpoint;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Storage {

    boolean init();

    long space();

    File createTempFile(File base, String suffix) throws IOException;

    Optional<Breakpoint> findLast();

    Optional<Breakpoint> findNearest(LocalDateTime time);

    List<LocalDateTime> listAvailable();

}
