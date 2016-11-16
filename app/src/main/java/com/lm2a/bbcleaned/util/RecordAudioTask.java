package com.lm2a.bbcleaned.util;

import android.content.Context;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.lm2a.bbcleaned.R;
import com.lm2a.bbcleaned.itf.AudioClipListener;

public class RecordAudioTask extends AsyncTask<AudioClipListener, Void, Boolean>
{
    private static final String TAG = "RecordAudioTask";
    public static RecordAudioTask myRecordAudioTask;
    private TextView status;
    private TextView log;

    private Context context;
    private String taskName;

//    public void selfRestart() {
//        myRecordAudioTask = new RecordAudioTask();
//    }

    public RecordAudioTask(Context context, TextView status, TextView log, String taskName)
    {
        this.context = context;
        this.status = status;
        this.log = log;
        this.taskName = taskName;
    }

    @Override
    protected void onPreExecute()
    {
        status.setText(context.getResources().getString(R.string.audio_status_recording) + " for " + getTaskName());
        AudioTaskUtil.appendToStartOfLog(log, "started " + getTaskName());
        super.onPreExecute();
    }

    boolean mNoSuspended = true;

    public void suspend(boolean b){
        mNoSuspended = b;
    }
    @Override
    protected Boolean doInBackground(AudioClipListener... listeners)
    {
        if (listeners.length == 0)
        {
            return false;
        }

        AudioClipListener listener = listeners[0];

        AudioClipRecorder recorder = new AudioClipRecorder(listener, this);


        boolean heard = false;
        for (int i = 0; i < 10; i++)
        {
            try
            {
                if(mNoSuspended) {
                    heard =
                            recorder.startRecordingForTime(1000,
                                    AudioClipRecorder.RECORDER_SAMPLERATE_8000,
                                    AudioFormat.ENCODING_PCM_16BIT);
                }
                break;
            } catch (IllegalStateException ie)
            {
                // failed to setup, sleep and try again
                // if still can't set it up, just fail
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                }
            }
        }

        //collect the audio
        return heard;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        Log.d(TAG, "After execute got result: " + result);
        //update the UI with what happened
        //add to log
        //redraw perhaps
        if (result)
        {
            AudioTaskUtil.appendToStartOfLog(log, getTaskName() + " detected " + AudioTaskUtil.getNow());
        }
        else
        {
            AudioTaskUtil.appendToStartOfLog(log, "stopped");
        }

        //log the result
        setDoneMessage();
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled()
    {
        Log.d(TAG, "OnCancelled");
        //the recorder should have shut down, this method
        //needs to just clean up resources
        AudioTaskUtil.appendToStartOfLog(log, "cancelled " + getTaskName());
        setDoneMessage();
        super.onCancelled();
    }

    private void setDoneMessage()
    {
        status.setText(context.getResources().getString(R.string.audio_status_stopped));
    }

    public String getTaskName()
    {
        return taskName;
    }
}