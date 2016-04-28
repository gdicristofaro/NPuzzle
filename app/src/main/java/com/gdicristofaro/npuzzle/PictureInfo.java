package com.gdicristofaro.npuzzle;


public class PictureInfo {
    private String name ;
    private int imageNumber;

    public String getName() {
        return name;
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public PictureInfo(String name, int imageNumber) {
        this.name = name;
        this.imageNumber = imageNumber;
    }
}
