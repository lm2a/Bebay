package com.lm2a.bbcleaned;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.lm2a.bbcleaned.itf.AudioClipListener;
import com.lm2a.bbcleaned.sound.AudioClipLogWrapper;
import com.lm2a.bbcleaned.util.OneDetectorManyObservers;
import com.lm2a.bbcleaned.util.RecordAudioTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView log;
    private TextView status;
    private RecordAudioTask recordAudioTask;
    SeekBar mCryThreshold, mVolume;
    private static final int MIN_CRY_THRESHOLD = 300;
    private static final int DEFAULT_CRY_THRESHOLD = 600;
    private static final int MAX_CRY_THRESHOLD = 1500;

    private static final int DEFAULT_VOLUME = 50;
    private static final int MAX_VOLUME = 100;

    public int mCry = DEFAULT_CRY_THRESHOLD, mVol = DEFAULT_VOLUME;
    public MediaPlayer mMediaPlayer = null;
    public ImageView mControl;
    TextSwitcher mTextSwitcher;
    public int mPosition = 0;

    public void setMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    public boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log = (TextView)findViewById(R.id.tv_resultlog);

        status = (TextView)findViewById(R.id.tv_recording_status);
        Button pitch = (Button)findViewById(R.id.btn_audio_pitch_startstop);
        pitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startTask(createAudioLogger(), "Audio Logger");
            }
        });
        Button stop = (Button)findViewById(R.id.btn_stop_audio_logger);
        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stopAll();
            }
        });

        final TextView cry = (TextView)findViewById(R.id.textView1);
        mCryThreshold = (SeekBar) findViewById(R.id.threshold);
        mCryThreshold.setMax(MAX_CRY_THRESHOLD);
        mCryThreshold.setProgress(DEFAULT_CRY_THRESHOLD);
        mCryThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                Log.i(TAG, "SeekBar value is " + progress);//value.setText("SeekBar value is " + progress);
                //int value = seekBar.getProgress(); //this is the value of the progress bar (1-100)
                mCry = seekBar.getProgress();
                cry.setText("Set Cry Threshold Volume To: "+progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

//        final TextView vol = (TextView)findViewById(R.id.textView2);
//        mVolume = (SeekBar) findViewById(R.id.volume);
//        mVolume.setMax(MAX_VOLUME);
//        mVolume.setProgress(DEFAULT_VOLUME);
//        mVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            public void onProgressChanged(SeekBar seekBar, int progress,
//                                          boolean fromUser) {
//                // TODO Auto-generated method stub
//                Log.i(TAG, "SeekBar value is " + progress);//value.setText("SeekBar value is " + progress);
//                int value = seekBar.getProgress(); //this is the value of the progress bar (1-100)
//                mVol = seekBar.getProgress();
//                vol.setText("Set Cry Threshold Volume To: "+value);
//            }
//
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//            }
//
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//            }
//        });

        mControl = (ImageView)findViewById(R.id.control);
        mControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayer.stop();
            }
        });

        //text switcher
        mTextSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher);
        mTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(MainActivity.this);
                textView.setTextSize(20);
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }
        });

        mTextSwitcher.setInAnimation(this, android.R.anim.fade_in);
        mTextSwitcher.setOutAnimation(this, android.R.anim.fade_out);


        onSwitch(null);
    }

    public void onSwitch(View view) {
        mTextSwitcher.setText(TEXTS[mPosition]);
        mPosition = (mPosition + 1) % TEXTS.length;
        Log.i(TAG, "song: "+mPosition);
    }





    private void startTask(AudioClipListener detector, String name)
    {
        stopAll();
        recordAudioTask = new RecordAudioTask(MainActivity.this, status, log, name);
        //wrap the detector to show some output
        mAudioClipLogWrapper = new AudioClipLogWrapper(log, this);
        mObservers = new ArrayList<AudioClipListener>();
        mObservers.add(mAudioClipLogWrapper);
        OneDetectorManyObservers wrapped =
                new OneDetectorManyObservers(detector, mObservers);
        recordAudioTask.execute(wrapped);
    }


    AudioClipLogWrapper mAudioClipLogWrapper;
    List<AudioClipListener> mObservers;

    public void playing(){
        recordAudioTask.suspend(true);
    }

    public void startAgain(){
        recordAudioTask.suspend(false);
//
//        shutDownTaskIfNecessary(recordAudioTask);
//        recordAudioTask = null;
//
//        recordAudioTask = new RecordAudioTask(MainActivity.this, status, log, "Audio Logger");
//
//        OneDetectorManyObservers wrapped =
//                new OneDetectorManyObservers(createAudioLogger(), mObservers);
//        recordAudioTask.execute(wrapped);
    }

    public void stopAll()
    {
        Log.d(TAG, "stop record audio");
        shutDownTaskIfNecessary(recordAudioTask);
    }


    private AudioClipListener createAudioLogger()
    {
        AudioClipListener audioLogger = new AudioClipListener()
        {
            @Override
            public boolean heard(short[] audioData, int sampleRate)
            {
                if (audioData == null || audioData.length == 0)
                {
                    return true;
                }

                // returning false means the recording won't be stopped
                // users have to manually stop it via the stop button
                return false;
            }
        };

        return audioLogger;
    }

    private void shutDownTaskIfNecessary(final AsyncTask task)
    {
        if ( (task != null) && (!task.isCancelled()))
        {
            if ((task.getStatus().equals(AsyncTask.Status.RUNNING))
                    || (task.getStatus()
                    .equals(AsyncTask.Status.PENDING)))
            {
                Log.d(TAG, "CANCEL " + task.getClass().getSimpleName());
                task.cancel(true);
            }
            else
            {
                Log.d(TAG, "task not running");
            }
        }
    }




    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        messageHandler.sendEmptyMessage(0);
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(playing){
                mControl.setVisibility(View.VISIBLE);
            }else{
                mControl.setVisibility(View.INVISIBLE);
            }
        }
    };


    private static final String[] TEXTS = { "Lullaby Good Night", "Twinkle",
            "Car travel"};
}
