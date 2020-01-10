package com.ilummc.wayback.tasks;

import com.ilummc.wayback.policy.Policy;
import com.ilummc.wayback.storage.FtpStorage;
import com.ilummc.wayback.storage.LocalStorage;
import io.izzel.taboolib.module.locale.TLocale;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

class FtpUploadTransferTask implements Executable {

    private String detail = "WAIT_RUNNING";

    private long eta = 0;

    private double progress = 0.0;

    private final LocalStorage storage;

    private final FtpStorage ftpStorage;

    private final List<Policy> complete, connectionFail;

    private long size = 0;

    private AtomicLong done = new AtomicLong(0);

    private String name = "";

    private Task next;

    private FTPClient client;

    FtpUploadTransferTask next(Task task) {
        this.next = task;
        return this;
    }

    FtpUploadTransferTask(LocalStorage storage, FtpStorage ftpStorage, List<Policy> connectionFail, List<Policy> complete) {
        this.storage = storage;
        this.ftpStorage = ftpStorage;
        this.complete = complete;
        this.connectionFail = connectionFail;
    }

    @Override
    public void execute() throws Exception {
        eta = 30000;
        Optional<String> optional = storage.latestName();
        if (optional.isPresent()) {
            String s = optional.get();
            client = ftpStorage.getClient();
            for (File file : storage.list(s)) {
                size = file.length();
                name = file.getName();
                done.set(0);
                detail = "UPLOADING_FILE";
                client.upload(file, new FTPDataTransferListener() {
                    @Override
                    public void started() {

                    }

                    @Override
                    public void transferred(int i) {
                        done.addAndGet(i);
                    }

                    @Override
                    public void completed() {

                    }

                    @Override
                    public void aborted() {
                        connectionFail.forEach(policy -> policy.accept(FtpUploadTransferTask.this));
                    }

                    @Override
                    public void failed() {
                        connectionFail.forEach(policy -> policy.accept(FtpUploadTransferTask.this));
                    }
                });
            }
        }
        reset();
        client.disconnect(true);
        complete.forEach(policy -> policy.accept(this));
        if (next != null) next.create().schedule().addToQueue();
    }

    private void reset() {
        progress = size = eta = 0;
        done.set(0);
        detail = "WAIT_RUNNING";
        name = "";
        client = null;
    }

    @Override
    public boolean terminate() {
        try {
            client.abortCurrentDataTransfer(true);
            return true;
        } catch (IOException | FTPIllegalReplyException ignored) {
            return false;
        }
    }

    @Override
    public void forceTerminate() {
        try {
            client.abortCurrentDataTransfer(false);
            client.disconnect(false);
        } catch (IOException | FTPIllegalReplyException | FTPException ignored) {
        }
    }

    @Override
    public double progress() {
        return progress;
    }

    @Override
    public long eta() {
        return eta;
    }

    @Override
    public String detail() {
        return TLocale.asString("FTP." + detail, name, done.toString(), String.valueOf(size));
    }

    @Override
    public String name() {
        return TLocale.asString("TASKS.FTP_UPLOAD_NAME");
    }

}
