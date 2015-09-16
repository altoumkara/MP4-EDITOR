package com.alama.mp4editor.utility;

import android.app.Fragment;
import android.os.Bundle;

import com.alama.mp4editor.tasks.TrimVideoTask;

/**
 * When phone orientation changes, the activity will get destroy and we might lose reference
 * to our activity, therefore this headless fragment is used  to avoid that.
 * we always give the new activity reference to our worker thread when a new activity is created
 *
 * @author Alama Tounkara
 */
public class HeadlessFragForCrop extends Fragment {


    public TrimVideoTask mCropVideoTask;


    public void startTask(int length, double mStartTime, String mMediaPath, String fullPathToVideoFile) {
        if (mCropVideoTask != null) {// cancel the task, even if its running
            mCropVideoTask.cancel(true);

        }
        mCropVideoTask = new TrimVideoTask(length, mStartTime, mMediaPath, fullPathToVideoFile);
        mCropVideoTask.execute();
    }



    /**
     * This will be called when activity onStop() is called.
     * we want to kill our task if the activity is stopped
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mCropVideoTask != null) {
            mCropVideoTask.cancel(true);
        }

    }


    @Override
    public void onActivityCreated(Bundle onSavedInstanceState) {
        super.onActivityCreated(onSavedInstanceState);
        setRetainInstance(true);//make this fragment undestructable
    }

}




