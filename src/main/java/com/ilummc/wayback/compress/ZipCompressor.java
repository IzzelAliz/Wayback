package com.ilummc.wayback.compress;

import com.ilummc.wayback.storage.Storage;
import com.ilummc.wayback.util.Jsons;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipCompressor implements ConfigurationSerializable, Compressor {

    private int level = 9;

    private boolean encrypt = false;

    private String password;

    public static ZipCompressor valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, ZipCompressor.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public Archive createArchive(File base, Storage storage) throws Exception {
        return new GzipArchive(storage.createTempFile(base, ".zip"));
    }

    @Override
    public String suffix() {
        return "zip";
    }

    private class GzipArchive implements Archive {

        private ZipFile zipFile;

        private ZipParameters parameters = new ZipParameters();

        private File file;

        private GzipArchive(File file) throws Exception {
            this.file = file;
            if (file.exists()) file.delete();
            zipFile = new ZipFile(file);
            zipFile.setFileNameCharset("GBK");
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(level);
            parameters.setSourceExternalStream(true);
            if (encrypt) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                parameters.setPassword(password);
            }
        }

        @Override
        public void write(String name, InputStream input) throws Exception {
            parameters.setFileNameInZip(name);
            zipFile.addStream(input, parameters);
        }

        @Override
        public File create(File base, LocalDateTime time) throws Exception {
            Path target = new File(base, time.toString().replace(':', '_') + ".zip").toPath();
            Files.move(file.toPath(), target);
            return target.toFile();
        }
    }
}
