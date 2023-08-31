package dev.dediamondpro.chatshot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

public class Config {
    private static final File configFile = new File("./config/chatshot.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Config INSTANCE = load();

    public boolean shadow = false;

    public int scale = 2;

    public boolean tooltip = true;

    public CopyType clickAction = CopyType.TEXT;

    public CopyType shiftClickAction = CopyType.IMAGE;

    public boolean saveImage = true;

    public boolean showCopyMessage = true;

    public enum CopyType {
        @SerializedName("0")
        TEXT,
        @SerializedName("1")
        IMAGE;
    }

    public static void save() {
        if (INSTANCE == null) return;
        try {
            String json = GSON.toJson(INSTANCE);
            FileUtils.write(configFile, json, (Charset) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Config load() {
        if (!configFile.exists()) return new Config();
        try {
            String json = FileUtils.readFileToString(configFile, (Charset) null);
            return GSON.fromJson(json, Config.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Config();
        }
    }
}
