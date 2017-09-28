package com.example.myapplication.bean;

import java.io.File;
import java.io.Serializable;

/**
 * Created by fupp on 2016/11/19.
 */
public class MusicObject implements Serializable {

    private File[] musics;

    public MusicObject(File[] musics) {
        this.musics = musics;
    }
    public File[] getMusics(){
        return musics;
    }
}
