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

import java.util.ArrayList;

public class SoundController {

    private static SoundController soundControllerInstance;

    private final float[] volumes;
    private final FileHandle configFile;
    private final JsonValue settingsJson;
    private int masterVolumeIndex;
    private int musicVolumeIndex;
    private int fxVolumeIndex;
    MusicQueue music;
    ArrayList<SoundEffect> sounds;

    public SoundController() {
        volumes = new float[]{0, 0.25f, 0.50f, 0.75f, 1.0f};
        configFile = Gdx.files.absolute(OSUtils.getConfigFile());
        JsonReader settingsJsonReader = new JsonReader();
        settingsJson = settingsJsonReader.parse(configFile);
        masterVolumeIndex = settingsJson.getInt("masterVolumeIndex");
        musicVolumeIndex = settingsJson.getInt("musicVolumeIndex");
        fxVolumeIndex = settingsJson.getInt("fxVolumeIndex");

        AudioEngine engine = (AudioEngine)Gdx.audio;
        music = engine.newMusicBuffer( false, 48000 );
    }

    public static SoundController getInstance() {
        if (soundControllerInstance == null) {
            soundControllerInstance = new SoundController();
        }
        return soundControllerInstance;
    }

    /**
     * Adds music to the MusicQueue
     * @param a the music to add
     * return the position of the added music
     */
    public int addMusic(AudioSource a){
        music.addSource(a);
        return music.getNumberOfSources() - 1;
    }

    /**
     * Add a sound effect to the list of sounds
     * @param s the sound effect to be added
     * @return the position of the sound effect in the sounds list
     */
    public int addSoundEffect(SoundEffect s){
        sounds.add(s);
        return sounds.size() - 1;
    }

    public void setLooping(boolean b){
        music.setLooping(b);
    }

    /**
     * Plays the music at the given index
     * @param i the index of the song to be played
     */
    public void setMusic(int i){
        music.stop();
        music.jumpToSource(i);
        music.play();
    }

    /**
     * Plays the sound at the given index
     * @param i the index of the sound to be played
     */
    public void playSound(int i){
        sounds.get(i).play();
    }

    public void stopSound(int i){
        sounds.get(i).stop();
    }

    public void playMusic(){
        music.play();
    }

    public void stopMusic(){
        music.stop();
    }

    public AudioSource getPlayingMusic(){
        return music.getCurrent();
    }



    public void stopAll(){
        for (SoundEffect s : sounds){
            s.stop();
        }
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
