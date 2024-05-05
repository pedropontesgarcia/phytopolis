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
import java.util.ArrayList;

public class SoundController {

    private static SoundController soundControllerInstance;

    private final FileHandle configFile;
    private final JsonValue settingsJson;
    private final float fxVolume;
    MusicQueue music;
    ArrayList<SoundEffect> sounds;
    private float masterVolume;
    private float musicVolume;
    private int musicQueuePos;

    public SoundController() {
        configFile = Gdx.files.absolute(OSUtils.getConfigFile());
        JsonReader settingsJsonReader = new JsonReader();
        settingsJson = settingsJsonReader.parse(configFile);
        masterVolume = settingsJson.getFloat("masterVolume");
        musicVolume = settingsJson.getFloat("musicVolume");
        fxVolume = settingsJson.getInt("fxVolume");

        AudioEngine engine = (AudioEngine) Gdx.audio;
        music = engine.newMusicBuffer(false, 44100);
        music.setVolume(musicVolume);
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

    public void setLooping(boolean b) {
        music.setLooping(b);
        music.setLoopBehavior(true);
    }

    /**
     * Plays the music at the given index
     *
     * @param i the index of the song to be played
     */
    public void setMusic(int i) {
        music.jumpToSource(i);
        musicQueuePos = i;
    }

    /**
     * Plays the sound at the given index
     *
     * @param i the index of the sound to be played
     */
    public void playSound(int i) {
        sounds.get(i).play();
    }

    public void stopSound(int i) {
        sounds.get(i).stop();
    }

    public void playMusic() {
        music.play();
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

    public void setMusicVolume(float value){
        music.setVolume(value);
    }

    public int getMusicQueuePos(){
        return musicQueuePos;
    }

    public boolean isMusicPlaying(){
        return music.isPlaying();
    }

    public void rewindMusic(){
        music.reset();
        setMusic(musicQueuePos);
        setLooping(true);
    }

    public void updateOption(SoundOption opn, float val) {
        switch (opn) {
            case MASTER_VOLUME:
                masterVolume = val;
                saveOptions();
                break;
            case MUSIC_VOLUME:
                musicVolume = val;
                music.setVolume(val);
                saveOptions();
                break;
            case FX_VOLUME:
                saveOptions();
                break;
        }
    }

    private void saveOptions() {
        DecimalFormat df = new DecimalFormat("#.000");
        settingsJson.get("masterVolume")
                .set(Double.parseDouble(df.format(masterVolume)), null);
        settingsJson.get("musicVolume")
                .set(Double.parseDouble(df.format(musicVolume)), null);
        settingsJson.get("fxVolume")
                .set(Double.parseDouble(df.format(fxVolume)), null);
        configFile.writeString(settingsJson.prettyPrint(JsonWriter.OutputType.json,
                                                        0), false);
    }

    public float getOptionValue(SoundOption opn) {
        switch (opn) {
            case MASTER_VOLUME:
                return masterVolume;
            case MUSIC_VOLUME:
                return musicVolume;
            case FX_VOLUME:
                return fxVolume;
        }
        return 0;
    }

    public boolean getIsLooping() {
        return music.isLooping();
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

    public enum SoundOption {
        MASTER_VOLUME, MUSIC_VOLUME, FX_VOLUME
    }

}
