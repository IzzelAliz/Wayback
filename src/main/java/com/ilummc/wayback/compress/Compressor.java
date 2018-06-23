package com.ilummc.wayback.compress;

import com.ilummc.wayback.storage.Storage;

import java.io.File;

public interface Compressor {

    Archive createArchive(File base, Storage storage) throws Exception;

    String suffix();

    Archive from(File file) throws Exception;

}
