package com.ilummc.wayback.compress;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;

public interface Archive {

    void write(String name, InputStream input) throws Exception;

    File create(File base, LocalDateTime time) throws Exception;

}
