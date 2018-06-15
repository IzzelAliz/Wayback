package com.ilummc.wayback.data;

import java.time.LocalDateTime;
import java.util.List;

public interface StorageDescription {

    LocalDateTime createdAt();

    List<FileChange> fileChanges();

    List<FileInfo> files();

}
