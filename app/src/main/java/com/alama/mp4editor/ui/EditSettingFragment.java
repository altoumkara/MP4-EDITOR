package com.alama.mp4editor.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import com.alama.mp4editor.R;
import com.alama.mp4editor.utility.VideoChosenEvent;
import com.alama.mp4editor.utility.VideoSingleton;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Alama Tounkara
 */
public class EditSettingFragment extends Fragment {
    public static final String TAG = "EditSettingFragment";
    private final String START_TIME_KEY = "START_TIME_KEY";
    private final String END_TIME_KEY = "END_TIME_KEY";

    //start time of the video trimming
    private EditText mStartTime;
    //end time of the video trimming
    private EditText mEndTime;
    //the crop button use to crop the video
    private Button mCropButton;
    //the path to the external video folder. i.e. /storage/emulated/0/Movies/VideoEditingFolder
    private String mVideoPath;
    //absolute path to the specific video file
    private String mFullPathToVideoFile;
    //total duration of the video view
    private long mDuration = -1;


    public EditSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onStart() {
        super.onStart();
        //register my bus
        VideoSingleton.getInstance().registerMyBus(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_setting, container, false);
        mStartTime = (EditText) view.findViewById(R.id.startTimeTXT);
        mEndTime = (EditText) view.findViewById(R.id.endTimeTXT);
        //this will allow user to use the phone keyboard to crop the video
        mEndTime.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    cropVideo();//method defined bellow
                }
                return false;
            }
        });

        mCropButton = (Button) view.findViewById(R.id.cropBTN);
        mCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropVideo();//method defined bellow
            }
        });


        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mStartTime.setText(savedInstanceState.getString(START_TIME_KEY));
            mEndTime.setText(savedInstanceState.getString(END_TIME_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String startTime = mStartTime.getText().toString();
        String endTime = mEndTime.getText().toString();
        if ((startTime != null) && (startTime.equals(""))) {
            if ((endTime != null) && (endTime.equals(""))) {
                outState.putString(START_TIME_KEY, startTime);
                outState.putString(END_TIME_KEY, endTime);
            }

        }
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
            //here we check to see the event result code is 3. Because I gave the event a code of 3
            // from the displayFragment-getVideoPathEvent() method when sending this event
            if (videoChosenEvent.getResulCode() == 3) {
                mVideoPath = videoChosenEvent.getVideoPath();
                mFullPathToVideoFile = videoChosenEvent.getNewVideoPath();
                mDuration = videoChosenEvent.getVideoLength();
            }
        }
    }


    /**
     * This method will get the start time and end time where the cropping occurs.
     * then post(send) a msg(event) to start the asynctask that crop the video.
     * This event will be cached from MainActivity#getStartTaskEvent(VideoChosenEvent videoChosenEvent)
     */
    public void cropVideo() {
        if (mDuration > 0) {//does the user even select a video yet
            String start = mStartTime.getText().toString();
            String length = mEndTime.getText().toString();
            if ((start != null) && (!start.equals(""))) {
                if ((length != null) && (!length.equals(""))) {

                    int startTime = Integer.parseInt(start);
                    int lengthTime = Integer.parseInt(length);
                    Log.d(TAG, "startTime: " + startTime + "; mDuration: " + mDuration + "; lengthTime: " + lengthTime);

                    if ((startTime >= 0) && (startTime < mDuration)) {
                        if ((lengthTime > startTime) && (lengthTime <= mDuration)) {
                            //here iam giving a code of 2 to this event. Therefore i can check the event received by
                            // the subscribers to see if the code is exactly to 2 before processing the event
                            //getStartTaskEvent() method from th MainActivity will get this
                            VideoSingleton.getInstance().postMsg(new VideoChosenEvent(
                                            VideoChosenEvent.VideoSelected.START_TASK, 2,
                                            mVideoPath, mFullPathToVideoFile, startTime, lengthTime
                                    )
                            );
                        } else {
                            Toast.makeText(getActivity(), "Crop Length MUST be greater than start time " +
                                    "and less than video length ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Start time MUST be greater than 0 " +
                                "and less than Crop Length ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Crop Length MUST NOT be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Start time MUST NOT be empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No video was selected!", Toast.LENGTH_SHORT).show();

        }

    }



}
