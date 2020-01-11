package com.ilummc.wayback.storage;

import com.google.common.collect.ImmutableList;
import com.ilummc.wayback.data.Breakpoint;
import com.ilummc.wayback.util.Jsons;
import io.izzel.taboolib.module.locale.TLocale;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FtpStorage implements ConfigurationSerializable, Storage {

    private String host, user, password, root;

    private int port = 22;

    private boolean ftps = false, ftpes = false;

    private transient FTPClient client;

    public static FtpStorage valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, FtpStorage.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public boolean init() {
        client = new FTPClient();
        try {
            if (ftps) client.setSecurity(FTPClient.SECURITY_FTPS);
            if (ftpes) client.setSecurity(FTPClient.SECURITY_FTPES);
            client.connect(host, port);
            client.login(user, password);
            client.createDirectory(root);
            client.changeDirectory(root);
            if (client.isCompressionSupported()) client.setCompressionEnabled(true);
            client.disconnect(true);
            return true;
        } catch (IOException | FTPIllegalReplyException e) {
            TLocale.Logger.error("FTP.FTP_INIT_ERR", e.getLocalizedMessage());
        } catch (FTPException e) {
            try {
                if (e.getCode() == 550) {
                    if (e.getMessage().toLowerCase().startsWith("already exists")) {
                        client.disconnect(true);
                        return true;
                    } else {
                        TLocale.Logger.error("FTP.NO_PERMISSION");
                        return false;
                    }
                }
            } catch (IOException | FTPIllegalReplyException | FTPException ignored) {
                return false;
            }
        }
        return false;
    }

    public void toBaseDir() throws Exception {
        client.changeDirectory(root);
    }

    public FTPClient getClient() throws Exception {
        client.connect(host, port);
        client.login(user, password);
        client.changeDirectory(root);
        return client;
    }

    private String parent(String dir) {
        return dir.substring(0, dir.lastIndexOf('/'));
    }

    @Override
    public long space() {
        return 0;
    }

    @Override
    public File createTempFile(File base, String suffix) {
        return null;
    }

    @Override
    public Optional<Breakpoint> findLast() {
        return Optional.empty();
    }

    @Override
    public Optional<Breakpoint> findNearest(LocalDateTime time) {
        return Optional.empty();
    }

    @Override
    public List<LocalDateTime> listAvailable() {
        return ImmutableList.of();
    }


}
