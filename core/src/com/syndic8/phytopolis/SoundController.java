package com.syndic8.phytopolis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.syndic8.phytopolis.util.OSUtils;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.audio.SoundEffect;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class SoundController {

    private static final float FXSCALE = 0.3f;
    private static SoundController soundControllerInstance;

    private final FileHandle configFile;
    private final JsonValue settingsJson;
    private final float masterVolume;
    private final DecimalFormat decimalFormat;
    MusicQueue music;
    ArrayList<SoundEffect> sounds;
    private float fxVolume;
    private float musicVolume;
    private int musicQueuePos;

    public enum SoundOption {
        MUSIC_VOLUME, FX_VOLUME
    }

    public SoundController() {
        configFile = Gdx.files.absolute(OSUtils.getConfigFile());
        JsonReader settingsJsonReader = new JsonReader();
        settingsJson = settingsJsonReader.parse(configFile);
        masterVolume = settingsJson.getFloat("masterVolume");
        musicVolume = settingsJson.getFloat("musicVolume");
        fxVolume = settingsJson.getFloat("fxVolume");

        AudioEngine engine = (AudioEngine) Gdx.audio;
        music = engine.newMusicBuffer(false, 44100);
        music.setVolume(musicVolume);
        sounds = new ArrayList<>();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        decimalFormat = new DecimalFormat("#.000", symbols);
    }

    public static SoundController getInstance() {
        if (soundControllerInstance == null) {
            soundControllerInstance = new SoundController();
        }
        return soundControllerInstance;
    }

    /**
     * Adds music to the MusicQueue
     *
     * @param a the music to add
     *          return the position of the added music
     */
    public int addMusic(AudioSource a) {
        music.addSource(a);
        return music.getNumberOfSources() - 1;
    }

    /**
     * Add a sound effect to the list of sounds
     *
     * @param s the sound effect to be added
     * @return the position of the sound effect in the sounds list
     */
    public int addSoundEffect(SoundEffect s) {
        sounds.add(s);
        return sounds.size() - 1;
    }

    /**
     * Plays the sound at the given index
     *
     * @param i the index of the sound to be played
     */
    public void playSound(int i) {
        sounds.get(i).play(fxVolume * FXSCALE);
    }

    public void stopSound(int i) {
        sounds.get(i).stop();
    }

    public void playMusic() {
        music.setVolume(musicVolume);
        music.play();
    }

    public void pauseMusic() {
        music.pause();
    }

    public void stopMusic() {
        music.stop();
    }

    public AudioSource getPlayingMusic() {
        return music.getCurrent();
    }

    public void stopAll() {
        for (SoundEffect s : sounds) {
            s.stop();
        }
    }

    public int getMusicQueuePos() {
        return musicQueuePos;
    }

    public boolean isMusicPlaying() {
        return music.isPlaying();
    }

    public void rewindMusic() {
        music.reset();
        setMusic(musicQueuePos);
        setLooping(true);
    }

    /**
     * Plays the music at the given index
     *
     * @param i the index of the song to be played
     */
    public void setMusic(int i) {
        musicQueuePos = i;
        music.setVolume(musicVolume);
        music.jumpToSource(i);
    }

    public void setLooping(boolean b) {
        music.setLooping(b);
        music.setLoopBehavior(true);
    }

    public float getUserMusicVolume() {
        return musicVolume;
    }

    public void setActualMusicVolume(float value) {
        music.setVolume(value);
    }

    public void updateOption(SoundOption opn, float val) {
        switch (opn) {
            case MUSIC_VOLUME:
                musicVolume = val;
                music.setVolume(val);
                saveOptions();
                break;
            case FX_VOLUME:
                fxVolume = val;
                saveOptions();
                break;
        }
    }

    private void saveOptions() {
        settingsJson.get("masterVolume")
                .set(Double.parseDouble(decimalFormat.format(masterVolume)),
                     null);
        settingsJson.get("musicVolume")
                .set(Double.parseDouble(decimalFormat.format(musicVolume)),
                     null);
        settingsJson.get("fxVolume")
                .set(Double.parseDouble(decimalFormat.format(fxVolume)), null);
        configFile.writeString(settingsJson.prettyPrint(JsonWriter.OutputType.json,
                                                        0), false);
    }

    public float getOptionValue(SoundOption opn) {
        switch (opn) {
            case MUSIC_VOLUME:
                return musicVolume;
            case FX_VOLUME:
                return fxVolume;
        }
        return 0;
    }

    //    public String getOptionValueString(SoundOption opn) {
    //        switch (opn) {
    //            case MASTER_VOLUME:
    //                return (int) (volumes[masterVolumeIndex] * 100) + " %";
    //            case MUSIC_VOLUME:
    //                return (int) (volumes[musicVolumeIndex] * 100) + " %";
    //            case FX_VOLUME:
    //                return (int) (volumes[fxVolumeIndex] * 100) + " %";
    //        }
    //        return "ERROR";
    //    }

    public boolean getIsLooping() {
        return music.isLooping();
    }

}
