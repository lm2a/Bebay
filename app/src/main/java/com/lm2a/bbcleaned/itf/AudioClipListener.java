package com.lm2a.bbcleaned.itf;

public interface AudioClipListener
{
    public boolean heard(short[] audioData, int sampleRate);
}