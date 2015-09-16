package com.alama.mp4editor.utility;

/**
 * @author Alama Tounkara
 */
public interface CallBackInterface {

    /**
     * callback method called from activity when asynstask onPreExecute() is executing
     */
    public void onPreExecute();
    /**
     * callback method called from activity when asynstask onPostExecute() is executing
     */
    public void onPostExecute(String workPath);
    /**
     * callback method call from activity when the video folder and video files are all set
     */
    public void onVideoFolderSet();

    /**
     * get the folder where the video are saved
     * @return video folder
     */
    public String getWorkingPath();

    /**
     * get the array of video names
     * @return array of video names
     */
    public String[] getVideosToMerge();
    /**
     * set the array of video names
     */
    public void setVideosToMerge(String [] videoNames);

}
