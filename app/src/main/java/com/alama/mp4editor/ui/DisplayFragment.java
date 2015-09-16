package com.alama.mp4editor.ui;


import android.app.Fragment;
import android.content.res.Configuration;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.squareup.otto.Subscribe;

import java.util.Locale;

import com.alama.mp4editor.R;
import com.alama.mp4editor.utility.ErrorEvent;
import com.alama.mp4editor.utility.Util;
import com.alama.mp4editor.utility.VideoChosenEvent;
import com.alama.mp4editor.utility.VideoSingleton;

/**
 * A simple {@link Fragment} subclass.
 * <p/>
 * Solution number 3: is the solution to the screen orientation issue when video is playing
 * http://stackoverflow.com/questions/4434027/android-videoview-orientation-change-with-buffered-video
 *
 * @author Alama Tounkara
 */
public class DisplayFragment extends Fragment {
    public static final String TAG = "DisplayFragment";
    /**
     * MediaMetadataRetriever class provides a unified interface for retrieving
     * frame and meta data from an input media file.
     */
    MediaMetadataRetriever mMetadataRetriever;
    //videoview to display the video
    private VideoView mVideoView;
    //progress to display while the editing is performing in the background
    private ViewGroup mProgressLinearLayout;
    //total duration of the video view
    private long mDuration;

    public DisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        //register my bus
        VideoSingleton.getInstance().registerMyBus(this);
        mMetadataRetriever = new MediaMetadataRetriever();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display, container, false);
        mProgressLinearLayout = (ViewGroup) view.findViewById(R.id.taskProgressBar);
        mVideoView = (VideoView) view.findViewById(R.id.outputVideoView);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);//make this fragment undestructable
        // setting video control such as play, stop,forward, backward
        mVideoView.setMediaController(new MediaController(getActivity()));

    }


    /**
     * Solution number 3: is the solution to the screen orientation issue when video is playing
     * http://stackoverflow.com/questions/4434027/android-videoview-orientation-change-with-buffered-video
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    /**
     * subscribe for VideoChosen events. This subscriber is made in order to show the progress bar
     * while the asynctask is working on the video
     *
     * @param videoChosenEvent
     */
    @Subscribe
    public void getProgressBarEvent(VideoChosenEvent videoChosenEvent) {
        if (videoChosenEvent.getType() == VideoChosenEvent.VideoSelected.PROGRESS_BAR) {
            //here we check to see the event result code is 1. Because I gave the event a code of 1
            // when of posting(sending) it onPreExecute() method of the asynctask
            if (videoChosenEvent.getResulCode() == 1) {
                mVideoView.stopPlayback();
                mProgressLinearLayout.setVisibility(View.VISIBLE);
                mVideoView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * subscribe for VideoChosen events. this subscriber is made in order to handle the video
     * playing event after the asynctask finished the job
     *
     * @param videoChosenEvent
     */
    @Subscribe
    public void getMergingDoneEvent(VideoChosenEvent videoChosenEvent) {
        if (videoChosenEvent.getType() == VideoChosenEvent.VideoSelected.VIDEO_FOLDER_PATH) {
            mProgressLinearLayout.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(videoChosenEvent.getNewVideoPath());

            mVideoView.setVideoURI(uri);
            if (mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }

            //now play the video
            mVideoView.start();
            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "ERROR VIDEO: " + mp);
                    return false;
                }
            });


            //use the video file metadata to retrieve the length of each video
            mMetadataRetriever.setDataSource(videoChosenEvent.getNewVideoPath());
            mDuration = convertToSecond(mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            //here iam giving a code of 3 to this event. Therefore i can check the event received by
            // the subscribers to see if the code is exactly to 3 before processing the event
            //this will be sent to the getVideoPathEvent() in EditSettingFragment to process the video
            VideoSingleton.getInstance().postMsg(new VideoChosenEvent(
                            VideoChosenEvent.VideoSelected.VIDEO_FULL_PATH, 3,
                            videoChosenEvent.getVideoPath(),
                            videoChosenEvent.getNewVideoPath(), mDuration
                    )
            );

        }
    }


    /**
     * subscribe for Error events. this subscriber is made in order to handle any error
     * that may occur during the editing
     *
     * @param errorEvent is the error that occur during editing
     */
    @Subscribe
    public void getErrorEvent(ErrorEvent errorEvent) {
        mProgressLinearLayout.setVisibility(View.GONE);
        mVideoView.setVisibility(View.VISIBLE);
        if (errorEvent.getType() == ErrorEvent.VideoError.ERROR_MERGING) {
            Util.displayError(getActivity(), errorEvent.getErrorMsg());
        }else if (errorEvent.getType() == ErrorEvent.VideoError.ERROR_CROPPING) {
            Util.displayError(getActivity(), errorEvent.getErrorMsg());
        }else{}
    }


    /**
     * subscribe for VideoChosen events. This subscriber is made in order to set the video view
     * when using click on the 'ok' button from the dialog box after chosen a video to crop
     *
     * @param videoChosenEvent
     */
    @Subscribe
    public void getVideoPathEvent(VideoChosenEvent videoChosenEvent) {
        if (videoChosenEvent.getType() == VideoChosenEvent.VideoSelected.VIDEO_FULL_PATH) {
            //here we check to see the event result code is 2. Because I gave the event a code of 2
            // when using click on the 'ok' button from the dialog box after chosen a video to crop
            if (videoChosenEvent.getResulCode() == 2) {
                if (mVideoView.isPlaying()) {
                    mVideoView.stopPlayback();
                }
                Uri uri = Uri.parse(videoChosenEvent.getNewVideoPath());
                mProgressLinearLayout.setVisibility(View.GONE);
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(uri);
                mVideoView.addSubtitleSource(getResources().openRawResource(R.raw.sub_title),
                        MediaFormat.createSubtitleFormat("text/vtt", Locale.ENGLISH.getLanguage()));
                mVideoView.seekTo(10);


                Log.d(TAG, "videoChosenEvent.getNewVideoPath(): " + videoChosenEvent.getNewVideoPath());

                //use the video file metadata to retrieve the length of each video
                mMetadataRetriever.setDataSource(videoChosenEvent.getNewVideoPath());
                mDuration = convertToSecond(mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));


                //here iam giving a code of 3 to this event. Therefore i can check the event received by
                // the subscribers to see if the code is exactly to 3 before processing the event
                //this will be sent to the getVideoPathEvent() in EditSettingFragment to process the video
                VideoSingleton.getInstance().postMsg(new VideoChosenEvent(
                                VideoChosenEvent.VideoSelected.VIDEO_FULL_PATH, 3,
                                videoChosenEvent.getVideoPath(), videoChosenEvent.getNewVideoPath(),
                                mDuration
                        )
                );
            }
        }
    }

    /**
     * This method will be used to convert the duration time string(which is in milli second) gotten
     * from VideoView#getDuration() method, to a time which is in second value
     *
     * @param milliSecondTime string duration time
     * @return a second representation of the duration
     */
    private long convertToSecond(String milliSecondTime) {
        long timeInmillisec = Long.parseLong(milliSecondTime);
        long duration = timeInmillisec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);
        return seconds;
    }

}


