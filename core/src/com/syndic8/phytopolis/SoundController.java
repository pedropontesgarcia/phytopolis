package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.syndic8.phytopolis.util.OSUtils;

public class SoundController {

    private static SoundController soundControllerInstance;

    private final float[] volumes;
    private final FileHandle configFile;
    private final JsonValue settingsJson;
    private int masterVolumeIndex;
    private int musicVolumeIndex;
    private int fxVolumeIndex;

    public SoundController() {
        volumes = new float[]{0, 0.25f, 0.50f, 0.75f, 1.0f};
        configFile = Gdx.files.absolute(OSUtils.getConfigFile());
        JsonReader settingsJsonReader = new JsonReader();
        settingsJson = settingsJsonReader.parse(configFile);
        masterVolumeIndex = settingsJson.getInt("masterVolumeIndex");
        musicVolumeIndex = settingsJson.getInt("musicVolumeIndex");
        fxVolumeIndex = settingsJson.getInt("fxVolumeIndex");
    }

    public static SoundController getInstance() {
        if (soundControllerInstance == null) {
            soundControllerInstance = new SoundController();
        }
        return soundControllerInstance;
    }

    public void updateOption(SoundOption opn) {
        switch (opn) {
            case MASTER_VOLUME:
                masterVolumeIndex = (masterVolumeIndex + 1) % volumes.length;
                saveOptions();
                break;
            case MUSIC_VOLUME:
                musicVolumeIndex = (musicVolumeIndex + 1) % volumes.length;
                saveOptions();
                break;
            case FX_VOLUME:
                fxVolumeIndex = (fxVolumeIndex + 1) % volumes.length;
                saveOptions();
                break;
        }
    }

    private void saveOptions() {
        settingsJson.get("masterVolumeIndex").set(masterVolumeIndex, null);
        settingsJson.get("musicVolumeIndex").set(musicVolumeIndex, null);
        settingsJson.get("fxVolumeIndex").set(fxVolumeIndex, null);
        configFile.writeString(settingsJson.prettyPrint(JsonWriter.OutputType.json,
                                                        0), false);
    }

    public String getOptionValueString(SoundOption opn) {
        switch (opn) {
            case MASTER_VOLUME:
                return (int) (volumes[masterVolumeIndex] * 100) + " %";
            case MUSIC_VOLUME:
                return (int) (volumes[musicVolumeIndex] * 100) + " %";
            case FX_VOLUME:
                return (int) (volumes[fxVolumeIndex] * 100) + " %";
        }
        return "ERROR";
    }

    public enum SoundOption {
        MASTER_VOLUME, MUSIC_VOLUME, FX_VOLUME
    }

}
