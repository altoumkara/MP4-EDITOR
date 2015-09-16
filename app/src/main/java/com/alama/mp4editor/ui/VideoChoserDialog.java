package com.alama.mp4editor.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import com.alama.mp4editor.R;
import com.alama.mp4editor.utility.Contants;
import com.alama.mp4editor.utility.VideoChosenEvent;
import com.alama.mp4editor.utility.VideoSingleton;

/**
 * This is the dialog box that will allow user to shose the videos he wants to merge
 *
 * @author Alama Tounkara
 */
public class VideoChoserDialog extends DialogFragment {
    public static final String TAG = "VideoChoserDialog";

    //list of all the videos names in our video folder
    private String[] mVideoFiles;
    private ArrayList<String> mVideosToMerge;
    //This is the code that tell us which button was clicked in order to start this dialog box
    private int mButtonPushCode;
    //this is the path to the video the user wants to crop
    private String mPathToNewVideo;
    //external public video folder's path
    private String mWorkingPath;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mVideosToMerge = new ArrayList<String>();
        initWorkingPath();//method defined bellow
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * This method will get called after the onCreate and onCreateView() of the fragment.
     * It will allow user to pick the videos he wants to merger together
     * Instead of (or in addition to) implementing onCreateView(LayoutInflater, ViewGroup, Bundle)
     * to generate the view hierarchy inside of a dialog, you may implement onCreateDialog(Bundle)
     * to create your own custom Dialog object.
     * We are using this method to
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //create the dialogBox
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if ((mVideoFiles != null) && (mVideoFiles.length > 0)) {//do we have any video file?
            //setting the title of the dialogBox
            builder.setTitle("CHOSE VIDEOS YOU WANT TO MERGE!");
            // I need to find out which kind of dialog box to display
            if (mButtonPushCode == 1) {//is merge button clicked to start this dialog box?
                //if true, that means the merge button was the one that was clicked to open this dialog
                //box, therefore we display a mulptiply choice dialogue box, so user can choose the videos
                //he want to merge together
                builder.setMultiChoiceItems(mVideoFiles, null, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        try {
                            if (isChecked) {
                                mVideosToMerge.add(mVideoFiles[which]);
                            } else {
                                if (mVideosToMerge.contains(mVideoFiles[which])) {
                                    mVideosToMerge.remove(mVideoFiles[which]);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "VIDEO CHECK ERROR: " + e);
                        }
                    }
                });

            } else if (mButtonPushCode == 2) { //is select button clicked to start this dialog box?
                //if true, that means the crop button was the one that was clicked to open this dialog
                //box, therefore we display a single choice dialogue box, so user can choose only ONE
                //video to crop
                mPathToNewVideo = mVideoFiles[0];//By default the video selected is the first element of the array
                builder.setSingleChoiceItems(mVideoFiles, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mPathToNewVideo = mVideoFiles[which];
                        } catch (Exception e) {
                            Log.e(TAG, "VIDEO CHECK ERROR: " + e);
                        }
                    }
                });
            } else {//does this dialog box got open somehow?
                //some other thing open this
            }
            //cancel button on the dialog box
            builder.setNegativeButton(R.string.cancelDialogText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            //ok button on the dialog box
            builder.setPositiveButton(R.string.okDialogText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (mButtonPushCode == 1) {
                        //here iam giving a code of 1 to this event. Therefore i can check the event received by
                        // the subscribers to see if the code is exactly to 1 before processing the event
                        VideoSingleton.getInstance().postMsg(new VideoChosenEvent(VideoChosenEvent.VideoSelected.START_TASK,
                                1, mWorkingPath, mVideosToMerge));
                    } else if (mButtonPushCode == 2) {
                        //here iam giving a code of 1 to this event. Therefore i can check the event received by
                        // the subscribers to see if the code is exactly to 1 before processing the event
                        //this will be sent to the o the getVideoPathEvent() in displayFragment to display the video
                        VideoSingleton.getInstance().postMsg(new VideoChosenEvent(
                                        VideoChosenEvent.VideoSelected.VIDEO_FULL_PATH,
                                        2, mWorkingPath, mWorkingPath + "/" + mPathToNewVideo
                                )
                        );
                    }
                }
            });

        } else {//we dont have any video to display
            //setting the title of the dialogBox
            builder.setTitle("NO VIDEO FOUND IN YOUR EXTERNAL VIDEO FOLDER!");
            //cancel button on the dialog box
            //ok button on the dialog box
            builder.setPositiveButton(R.string.okDialogText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });

        }


        //NOW BUILD THE ACTUAL DIALOG BOX
        Dialog dialog = builder.create();

        return dialog;

    }


    /**
     * This method will initialize our working path.
     * It will get the "Video" folder on external the drive.
     * Or Create it if it does not exist. and assign the full path
     * to our mWorkingPath variable
     */
    public void initWorkingPath() {
        if (!Contants.EXTERNAL_VIDEO_PUBLIC_DIR.exists()) {
            //create the working path if it doesnt exist
            Contants.EXTERNAL_VIDEO_PUBLIC_DIR.mkdirs();
        }
        //complete working path
        this.mWorkingPath = Contants.EXTERNAL_VIDEO_PUBLIC_DIR.getAbsolutePath().toString();

        File folder = new File(mWorkingPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                mVideosToMerge.add(listOfFiles[i].getName());
            }
        }
        mVideoFiles = new String[mVideosToMerge.size()];
        mVideoFiles = mVideosToMerge.toArray(mVideoFiles);
        mVideosToMerge = new ArrayList<String>();

    }


    /**
     * THIS will be call from the activity to set the code of which button was pushed to open
     * this dialog box
     *
     * @param mButtonPushCode is the code that each button has to provide while openning this
     *                        dialog box
     */
    public void setButtonPushCode(int mButtonPushCode) {
        this.mButtonPushCode = mButtonPushCode;
    }
}
