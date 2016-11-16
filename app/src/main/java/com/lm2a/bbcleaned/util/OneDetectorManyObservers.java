package com.lm2a.bbcleaned.util;

import com.lm2a.bbcleaned.itf.AudioClipListener;

import java.util.List;

public class OneDetectorManyObservers implements AudioClipListener
{
    private AudioClipListener detector;
    
    private List<AudioClipListener> observers;
    
    public OneDetectorManyObservers(AudioClipListener detector, List<AudioClipListener> observers)
    {
        this.detector = detector;
        this.observers = observers;
    }
    
    @Override
    public boolean heard(short[] audioData, int sampleRate)
    {
        for (AudioClipListener observer : observers)
        {
            observer.heard(audioData, sampleRate);
        }
        
        return detector.heard(audioData, sampleRate);
    }
}