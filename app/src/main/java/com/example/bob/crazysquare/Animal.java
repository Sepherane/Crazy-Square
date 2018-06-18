package com.example.bob.crazysquare;

public class Animal {

    private int image;
    private int sound;

    public Animal(int image, int sound){
        this.image = image;
        this.sound = sound;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getSound(){
        return sound;
    }

    public void setSound(int sound) {
        this.sound = sound;
    }
}
