package com.alama.mp4editor.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alama.mp4editor.utility.ErrorEvent;
import com.alama.mp4editor.utility.Util;
import com.alama.mp4editor.utility.VideoChosenEvent;
import com.alama.mp4editor.utility.VideoSingleton;

/**
 * This class is use to shorten video  the length of the video
 * We created this task in a different to avoid frozing the main thread
 * <p/>
 *
 * @author Alama Tounkara
 */
public class TrimVideoTask extends AsyncTask<Void, Void, String> {
    public static final String TAG = "TrimVideoTask";


    //complete path of the video folder
    private String mVideFolderPath;
    //absolute path to the specific video file
    private String mFullPathToVideoFile;
    //start time in second of the video
    private double mStartTime;
    //end time of the video
    private double mEndTime;
    //number of time it take to trim the video after the start time
    private int mLength;


    public TrimVideoTask(int length, double mStartTime, String mMediaPath, String fullPathToVideoFile) {
        this.mLength = length;
        this.mStartTime = mStartTime;
        this.mEndTime = this.mStartTime + this.mLength;
        this.mFullPathToVideoFile = fullPathToVideoFile;
        this.mVideFolderPath = mMediaPath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //here iam giving a code of 1 to this event. Therefore i can check the event received by
        // the subscribers to see if the code is exactly to 1 before processing the event
        VideoSingleton.getInstance().postMsg(new VideoChosenEvent(VideoChosenEvent.VideoSelected.PROGRESS_BAR, 1));
    }

    @Override
    protected String doInBackground(Void... params) {
        return cropSelectedVideo();
    }

    /**
     * all the trimming happens in this method.
     * <p/>
     * STEP A ==>>
     * For each track in the video, we first get the start time for all the tracks.
     * <p/>
     * STEP B ==>>
     * For each track in the video, we crop the tracks with a 'start time' and 'end time'
     * and add it to the Movie object.
     * <p/>
     * STEP C ==>>
     * Finally write the newly created movie to the disk.
     */
    private String cropSelectedVideo() {
        FileInputStream fileInputStream = null;
        FileChannel fileChannel = null;
        try {

            File videoFile = new File(mFullPathToVideoFile);
            fileInputStream = new FileInputStream(videoFile);
            fileChannel = fileInputStream.getChannel();
            Movie movie = MovieCreator.build(fileChannel);
            if(movie==null){
                return null;
            }else {
                List<Track> tracks = movie.getTracks();
                movie.setTracks(new LinkedList<Track>());

                boolean timeCorrected = false;
                if ((tracks == null) && (tracks.size() <= 0)) {
                    return null;
                } else {
                    /**
                     * here we try to find a track that has sync samples. Since we can only start decoding at such a
                     * sample we should make sure that the start of the fragment is exactly the such a frame.
                     */
                    for (Track track : tracks) {
                        if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                            if (timeCorrected) {
                                /**
                                 * This exception here can be false position in case we have multiple tracks with
                                 * sync sample at exactly the same position. E.g. a single movie containing
                                 * multiple qualities of the same video.
                                 */

                            } else {
                                mStartTime = correctTimeToNextSyncSample(track, mStartTime, false);
                                mEndTime = correctTimeToNextSyncSample(track, mEndTime, true);
                                timeCorrected = true;
                            }
                        }
                    }


                    for (Track track : tracks) {
                        long currentVidSample = 0;
                        double currentTime = 0;
                        long startVidSample = -1;
                        long endVidSample = -1;

                        for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                            TimeToSampleBox.Entry myEntry = track.getDecodingTimeEntries().get(i);

                            for (int j = 0; j < myEntry.getCount(); j++) {
                                //I am trying to find the start time and end time when the trimmimg occurs
                                if (currentTime <= mStartTime) {
                                    //our current video sample is before the starting time for the crop
                                    //if the startVidSample is equal to the length of the video,
                                    // an error happened, and we should throw an exception
                                    startVidSample = currentVidSample; //the new begining of the video will be set to this place of the video
                                } else if (currentTime <= mEndTime) {
                                    //our current video sample is after the starting time for the crop
                                    //but  before the end time of the crop
                                    endVidSample = currentVidSample;//the new end of the video will be set to this place of the video
                                } else {
                                    //our current video sample is after the end time of the cropping
                                    // we just stop this this loop
                                    break;
                                }

                                //getDelta() : the amount of time the current video sample covers
                                currentTime += (double) myEntry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                                currentVidSample++;
                            }
                        }
                        movie.addTrack(new CroppedTrack(track, startVidSample, endVidSample));
                    }
                }
            }

            IsoFile isoFile = new DefaultMp4Builder().build(movie);
            /**
             * STEP C ==>>
             * After we created the Movie, we have to place it into a nonvolatile memory.
             */
            return Util.placeFileInNonVolatileDrive(isoFile, mVideFolderPath, "trim_output");


        } catch (Exception e) {
            Log.e(TAG, "IO ERROR: " + e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "IO ERROR: " + e);
                }
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    Log.e(TAG, "IO ERROR: " + e);
                }
            }
        }
        return null;

    }


    /**
     * we try to get the nearest synchronized sample to our desired start time. Afterwards, we are ready to
     * crop each track of the video.
     */
    private double correctTimeToNextSyncSample(Track track, double cropVidPlace, boolean nextPlace) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
            TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
            for (int j = 0; j < entry.getCount(); j++) {
                if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                    // samples always start with 1 but we start with zero therefore +1
                    timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
                }
                currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cropVidPlace) {
                if (nextPlace) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }


    @Override
    protected void onPostExecute(String newVideoPath) {
        super.onPostExecute(newVideoPath);
        Log.d(TAG, "done newVideoPath: " + newVideoPath);

        if (newVideoPath != null) { //return is the value return by the doInBackground() method
            VideoSingleton.getInstance().postMsg(new VideoChosenEvent(
                            VideoChosenEvent.VideoSelected.VIDEO_FOLDER_PATH, mVideFolderPath,
                            newVideoPath
                    )
            );
        } else {
            VideoSingleton.getInstance().postMsg(new ErrorEvent(
                            ErrorEvent.VideoError.ERROR_MERGING, "THIS VIDEO CAN NOT BE TRIMMED..."
                    )
            );
        }
    }

}
