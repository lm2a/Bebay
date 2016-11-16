package com.lm2a.bbcleaned.sound;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;

import com.lm2a.bbcleaned.MainActivity;
import com.lm2a.bbcleaned.R;
import com.lm2a.bbcleaned.itf.AudioClipListener;
import com.lm2a.bbcleaned.util.AudioTaskUtil;
import com.lm2a.bbcleaned.util.AudioUtil;
import com.lm2a.bbcleaned.util.ZeroCrossing;



public class AudioClipLogWrapper implements AudioClipListener
{
    private TextView log;
    //private boolean playing = false;

    private Activity context;
    
    private double previousFrequency = -1;

    public AudioClipLogWrapper(TextView log, Activity context)
    {
        this.log = log;
        this.context = context;
        mp = ((MainActivity)context).mMediaPlayer;
    }

    @Override
    public boolean heard(short[] audioData, int sampleRate)
    {
        if(((MainActivity)context).playing) {

            Log.i("Rec", "Playing");
            if(!mp.isPlaying()){
                ((MainActivity)context).setPlaying(false);
                ((MainActivity)context).startAgain();

            }
            return false;
        }else{


            final double zero = ZeroCrossing.calculate(sampleRate, audioData);
            final double volume = AudioUtil.rootMeanSquared(audioData);

            final boolean isLoudEnough = volume > LoudNoiseDetector.DEFAULT_LOUDNESS_THRESHOLD;
            //range threshold of 100
            final boolean isDifferentFromLast = Math.abs(zero - previousFrequency) > 100;

            final StringBuilder message = new StringBuilder();

            if (volume > ((MainActivity)context).mCry) {
                //((MainActivity)context).playing();
                message.append("CRYING, volume: ").append((int) volume);
                managerOfSound(((MainActivity)context).mPosition);
                ((MainActivity)context).setPlaying(true);
            } else {
                message.append("Environment volume: ").append((int) volume);
            }


            if (!isLoudEnough) {
                message.append(" (silence) ");
            }
            //message.append(" freqency: ").append((int) zero);
            if (isDifferentFromLast) {
                message.append(" (diff)");
            }

            previousFrequency = zero;

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AudioTaskUtil.appendToStartOfLog(log, message.toString());
                }
            });
            Log.i("Rec", "No playing");

            return false;
        }

    }


    MediaPlayer mp;

    /**
     * Manager of Sounds
     */
    protected void managerOfSound(int preferedSong) {
            switch (preferedSong){
                case 0:
                    mp = MediaPlayer.create(context, R.raw.car);
                    break;
                case 1: mp = MediaPlayer.create(context, R.raw.lullaby_goodnight);
                    break;
                case 2: mp = MediaPlayer.create(context, R.raw.twinkle);
                    break;
            }
        //mp.setVolume(((MainActivity)context).mVol, ((MainActivity)context).mVol);
        ((MainActivity)context).setMediaPlayer(mp);

            mp.start();
    }
}
