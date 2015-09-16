package com.alama.mp4editor.utility;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;

import com.alama.mp4editor.tasks.MergeVideosTask;

/**
 * When phone orientation changes, the activity will get destroy and we might lose reference
 * to our activity, therefore this headless fragment is used  to avoid that.
 * we always give the new activity reference to our worker thread when a new activity is created
 *
 * @author Alama Tounkara
 */
public class HeadlessFragForMerging extends Fragment {


    public MergeVideosTask mMergeVideoTask;


    public void startTask(String workPath, ArrayList<String> videoToMerge) {
        if (mMergeVideoTask != null) {// cancel the task, even if its running
            mMergeVideoTask.cancel(true);

        }
        mMergeVideoTask = new MergeVideosTask(workPath, videoToMerge);
        mMergeVideoTask.execute();
    }


    /**
     * This will be called when activity onStop() is called.
     * we want to kill our task if the activity is stopped
     */
    @Override
    public void onStop() {
        if (mMergeVideoTask != null) {
            mMergeVideoTask.cancel(true);
        }
        super.onStop();
    }


    @Override
    public void onActivityCreated(Bundle onSavedInstanceState) {
        super.onActivityCreated(onSavedInstanceState);
        setRetainInstance(true);//make this fragment undestructable
    }

}




