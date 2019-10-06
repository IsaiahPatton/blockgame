package com.zunozap.games.entities;

import java.awt.image.BufferedImage;

import com.zunozap.games.ResourceManager;

public class Cow extends Entity {

    @Override
    public String getName() {
        return "Cow";
    }

    @Override
    public BufferedImage getTexture() {
        return ResourceManager.getTexture("entities/cow.png");
    }

}