package com.alama.mp4editor.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.squareup.otto.Subscribe;

import com.alama.mp4editor.R;
import com.alama.mp4editor.utility.HeadlessFragForCrop;
import com.alama.mp4editor.utility.HeadlessFragForMerging;
import com.alama.mp4editor.utility.VideoChosenEvent;
import com.alama.mp4editor.utility.VideoSingleton;


/**
 *
 *  @author Alama Tounkara
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";


//    @InjectView(R.id.mergeBTN)
//    Button mButtonMerge;
//    @InjectView(R.id.cropBTN)
//    Button mButtonCrop;

    /**
     * When phone orientation changes, the activity will get destroy and we might lose reference
     * to our activity, therefore this headless fragment is used  to avoid that
     */
    private HeadlessFragForMerging mMergeHeadlessFragment;
    /**
     * When phone orientation changes, the activity will get destroy and we might lose reference
     * to our activity, therefore this headless fragment is used  to avoid that
     */
    private HeadlessFragForCrop mCropHeadlessFragment;
    //dialog box that pop up to allow user to chose videos that he wants to merge
    private VideoChoserDialog mVideoChoserDialog;
    //fragment manager
    private FragmentManager mManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_layout_port);

        //ButterKnife.inject(this);//inject views


        //creating the dialog box
        mVideoChoserDialog = new VideoChoserDialog();

        mManager = getFragmentManager();//getting our fragment manager
         /*
         * get reference to the fragment
		 */
        if (savedInstanceState == null) {// first time this activity is being created

            mMergeHeadlessFragment = new HeadlessFragForMerging();
            mManager.beginTransaction().add(mMergeHeadlessFragment, "MergeHeadlessFragment").commit();
            mCropHeadlessFragment = new HeadlessFragForCrop();
            mManager.beginTransaction().add(mCropHeadlessFragment, "CropHeadlessFragment").commit();
        } else {// DONT CREATE IT, JUST FIND IT
            mMergeHeadlessFragment = (HeadlessFragForMerging) mManager.findFragmentByTag("MergeHeadlessFragment");
            mCropHeadlessFragment = (HeadlessFragForCrop) mManager.findFragmentByTag("CropHeadlessFragment");

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //register my bus
        VideoSingleton.getInstance().registerMyBus(this);
    }


    @Override
    protected void onStop() {
        //unregister my bus
        VideoSingleton.getInstance().unRegisterMyBus(this);
        super.onStop();
    }


    /**
     * subscribe for VideoChosen events.This subscriber is made in order to start the asynctask
     * after user choses the videos he wants to merge
     *
     * @param videoChosenEvent
     */
    @Subscribe
    public void getStartTaskEvent(VideoChosenEvent videoChosenEvent) {
        if (videoChosenEvent.getType() == VideoChosenEvent.VideoSelected.START_TASK) {
            //here we check to see the event result code is 1. Because I gave the event a code of 1
            // when of posting(sending) it when the user click the dialog box 'ok' button
            if (videoChosenEvent.getResulCode() == 1) {
                mMergeHeadlessFragment.startTask(videoChosenEvent.getVideoPath(),
                        videoChosenEvent.getVideoFiles());
            } else if (videoChosenEvent.getResulCode() == 2) {
                //here we checked to see the event result code is 2. Because I gave the event a code of 2
                // when user click on the crop button in EditSettingFragment
                mCropHeadlessFragment.startTask(videoChosenEvent.getCropLength(), videoChosenEvent.getStartTime(),
                        videoChosenEvent.getVideoPath(), videoChosenEvent.getNewVideoPath());
            }
        }
    }

    /**
     * merge the selected videos into one video and play that video
     *
     * @param view is the merge button
     */
    public void mergeVideos(View view) {
        mVideoChoserDialog.setButtonPushCode(1);
        mVideoChoserDialog.show(getFragmentManager(), "VideoChoserDialog");
    }

    /**
     * select a video to display in the videoVideo
     *
     * @param view is the crop button
     */
    public void selectVideoToCrop(View view) {
        mVideoChoserDialog.setButtonPushCode(2);
        mVideoChoserDialog.show(mManager, "VideoChoserDialog");
    }


}
