package com.alama.mp4editor.utility;

import java.util.ArrayList;

/**
 * Event class representing common events that may happen across our app
 *
 * @author Alama Tounkara
 */
public class VideoChosenEvent extends AbstractEvent {

    public enum VideoSelected{VIDEO_FOLDER_PATH, PROGRESS_BAR, START_TASK, BUTTON_PUSH, VIDEO_FULL_PATH};
    //result code to differentiate events
    private int mResulCode;
    //the path to the external video folder. i.e. /storage/emulated/0/Movies/VideoEditingFolder
    private String mVideoPath;
    //list of all the videos names in our video folder
    private ArrayList<String> mVideoFiles;
    //new video path
    private String mNewVideoPath;
    //start time of the video trimming
    private int mStartTime;
    //end time of the video trimming
    private int mCropLength;
    //Video total length
    private long mVideoLength;

    public VideoChosenEvent(Enum type, int resultCode, String path, ArrayList<String> videoFiles) {
        super(type);
        this.mResulCode=resultCode;
        this.mVideoPath = path;
        this.mVideoFiles = videoFiles;
    }

    public VideoChosenEvent(Enum type, int resultCode, String newVideoPath) {
        super(type);
        this.mNewVideoPath = newVideoPath;
        this.mResulCode=resultCode;
    }

    public VideoChosenEvent(Enum type, int resultCode, String videoFolder, String newVideoPath) {
        super(type);
        this.mVideoPath = videoFolder;
        this.mResulCode=resultCode;
        this.mNewVideoPath = newVideoPath;
    }

    public VideoChosenEvent(Enum type, int resultCode, String videoFolder, String newVideoPath, int startTime, int cropLength) {
        super(type);
        this.mVideoPath = videoFolder;
        this.mResulCode=resultCode;
        this.mNewVideoPath = newVideoPath;
        this.mStartTime = startTime;
        this.mCropLength = cropLength;
    }

    public VideoChosenEvent(Enum type, String videoFolder, String newVideoPath) {
        super(type);
        this.mVideoPath = videoFolder;
        this.mNewVideoPath = newVideoPath;
    }

    public VideoChosenEvent(Enum type, int resultCode, String videoFolder, String newVideoPath, long videoLength) {
        super(type);
        this.mVideoPath = videoFolder;
        this.mResulCode=resultCode;
        this.mNewVideoPath = newVideoPath;
        this.mVideoLength = videoLength;
    }

    public VideoChosenEvent(Enum type, int resultCode) {
        super(type);
        this.mResulCode=resultCode;
    }



    public VideoChosenEvent(Enum type, String newVideoPath) {
        super(type);
        this.mNewVideoPath = newVideoPath;
    }


    public VideoChosenEvent(Enum type) {
        super(type);
    }

    public String getNewVideoPath() {
        return mNewVideoPath;
    }

    public int getResulCode() {
        return mResulCode;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public ArrayList<String> getVideoFiles() {
        return mVideoFiles;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public int getCropLength() {
        return mCropLength;
    }

    public long getVideoLength() {
        return mVideoLength;
    }
}
