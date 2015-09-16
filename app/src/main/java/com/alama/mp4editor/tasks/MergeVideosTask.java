package com.alama.mp4editor.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alama.mp4editor.utility.ErrorEvent;
import com.alama.mp4editor.utility.Util;
import com.alama.mp4editor.utility.VideoChosenEvent;
import com.alama.mp4editor.utility.VideoSingleton;

/**
 * This Asynctask is used to merge video file together.
 * We created this mergin task in a different thread to avoid freezing the main thread.
 * <p/>
 * <bold> Here is the steps to merge videos into one video</bold>
 * STEP A ==>>
 * first create an array of movie object that contains all the information as video
 * and audio tracks of such video
 * STEP B ==>>
 * In this step, we read and concatenate the sounds and video tracks.
 * As a result, for each video object:
 * 1--> we get every video track and put(stack) them in a list
 * 2--> and we get every audio track and put(stack) them in a list
 * <p/>
 * STEP C ==>>
 * After concatenating every video and audio track, you create a new movie placeholder
 * for your android mobile app.
 * <p/>
 * STEP D ==>>
 * After we created the Movie, we have to it to a nonvolatile memory.
 *
 * @author Alama Tounkara
 */
public class MergeVideosTask extends AsyncTask<String, Integer, String> {
    public static final String TAG = "MergeVideosTask";
    //the path to our VIDEO folder where the video files are located
    private String mVideosFolder;
    //the list that contains the video names
    private ArrayList<String> mVideosToMerge;

    /**
     * the constructor initialize 3 variables
     *
     * @param videoFolderPath  is the working path folder
     * @param videoListToMerge is the list containing the video names.
     */
    public MergeVideosTask(String videoFolderPath, ArrayList<String> videoListToMerge) {
        this.mVideosFolder = videoFolderPath;
        this.mVideosToMerge = videoListToMerge;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //here iam giving a code of 1 to this event. Therefore i can check the event received by
        // the subscribers to see if the code is exactly to 1 before processing the event
        VideoSingleton.getInstance().postMsg(new VideoChosenEvent(VideoChosenEvent.VideoSelected.PROGRESS_BAR, 1));
    }

    @Override
    protected String doInBackground(String... params) {
        //number of video files to merge
        int count = mVideosToMerge.size();
        for (int i = 0; i < count; i++) {
            Log.d(TAG, "mVideosToMerge.length: " + mVideosToMerge.get(i));
        }

        /**
         * STEP A ==>>
         *      first create an array of movie object that contains all the information as video
         *      and audio tracks of such video
         */
        Movie[] mainMovie = this.createMovieOBJ(count);


        /**
         * STEP B ==>>
         *      In this step, we read and concatenate the sounds and video tracks.
         *      As a result, for each video object:
         *          1--> we get every video track and put(stack) them in a list
         *          2--> and we get every audio track and put(stack) them in a list
         */
        List<Track> videoTracks = new LinkedList<Track>(); //list containing video tracks
        List<Track> audioTracks = new LinkedList<Track>();//list containing audio tracks
        //for each Movie track
        for (Movie movie : mainMovie) {
            if(movie==null){
                return null;
            }else {
                List<Track> tracks = movie.getTracks();
                if((tracks == null)&&(tracks.size()<=0)){
                    return null;
                }else {
                    //get all the track for each movie object
                    for (Track track : tracks) {
                        Log.e(TAG, "track.getHandler(): " + track.getHandler());
                        //put all audio track for each movie in a list
                        if (track.getHandler().equals("soun")) {
                            audioTracks.add(track);

                        }
                        //put all video track for each movie in a list
                        if (track.getHandler().equals("vide")) {

                            videoTracks.add(track);
                        }
                        if (track.getHandler().equals("")) {

                        }

                    }
                }
            }

        }


        /**
         * STEP C ==>>
         *      After concatenating every video and audio track, you create a new movie placeholder
         *      for your android mobile app.
         */
        Movie result = createMoviePlaceHolder(audioTracks, videoTracks);

        //sometime this(result) will throw an error and crash your app.
        //it usually happen when user try to merge 2 or more video together with different codec
        if (result != null) {

            /**
             * STEP D ==>>
             *      After we created the Movie, we have to place it into a nonvolatile memory.
             */
            IsoFile isoFile = null;
            try {
                isoFile = new DefaultMp4Builder().build(result);
                //return the path to the newly created movie
                /**
                 * STEP D ==>>
                 * After we created the Movie, we have to place it into a nonvolatile memory.
                 * return the path to the newly created movie we can start playing the video
                 * in the video
                 */
                return Util.placeFileInNonVolatileDrive(isoFile, mVideosFolder, "merge_output");

            } catch (IOException e) {
                Log.e(TAG, "IO ERROR: " + e);
            }
        }


        return null;
    }

    /**
     * STEP A ==>>
     * first create an array of movie object that contains all the information as video
     * and audio tracks of such video
     *
     * @param numbOfMovie is the number total of movie we are planning to merger together
     * @return a movie array containing all the video and audio tracks
     */
    private Movie[] createMovieOBJ(int numbOfMovie) {
        Movie[] movie = new Movie[numbOfMovie];
        /**
         * this loop will get all the movie files from 'mVideosFolder' and
         * generate the movie objects and adding it to the array
         */
        for (int i = 0; i < numbOfMovie; i++) {

            //create new File using 'mVideosFolder' directory path and 'mVideosToMerge.get(i)' file name
            File videoFile = new File(mVideosFolder, mVideosToMerge.get(i));
            if (videoFile.exists()) {
                FileInputStream fileInputStream = null;
                Log.e(TAG, "videoFile: " + videoFile.getAbsolutePath().toString());

                /**
                 * <p>
                 * A {@code FileChannel} defines the methods for reading, writing, memory
                 * mapping, and manipulating the logical state of a platform file. This type
                 * does not have a method for opening files, since this behavior has been
                 * delegated to the {@link java.io.FileInputStream},
                 * {@link java.io.FileOutputStream} and {@link java.io.RandomAccessFile} types.
                 *  <p>
                 */
                FileChannel fileChannel = null;
                try {
                    fileInputStream = new FileInputStream(videoFile); //reading the file
                    fileChannel = fileInputStream.getChannel();
                    //generating the movie object and adding it to the array
                    movie[i] = MovieCreator.build(fileChannel);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FILE IS NOT FILE: " + e);
                } catch (IOException e) {
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
            }

        }
        return movie;

    }

    /**
     * STEP C ==>>
     * After concatenating every video and audio track, i create a new movie placeholder
     * for my android mobile app.
     *
     * @param audioTracks is the list that contains all the audio tracks for the merged video
     * @param videoTracks is the list that contains all the video tracks for the merged video
     * @return a new movie placeholder for our app
     */
    private Movie createMoviePlaceHolder(List<Track> audioTracks, List<Track> videoTracks) {
        Movie result = new Movie();
        try {
            if (audioTracks.size() > 0) {

                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }
            return result; //return result
        } catch (IOException e) {
            Log.e(TAG, "IO ERROR: " + e);
        }
        return null;
    }


    @Override
    protected void onPostExecute(String workPath) {
        super.onPostExecute(workPath);
        if (workPath != null) { //return is the value return by the doInBackground() method
            Log.d(TAG, "onPostExecute(String workPath): " + workPath);

            VideoSingleton.getInstance().postMsg(new VideoChosenEvent(
                            VideoChosenEvent.VideoSelected.VIDEO_FOLDER_PATH, mVideosFolder,
                            workPath
                    )
            );
        } else {
            VideoSingleton.getInstance().postMsg(new ErrorEvent(
                            ErrorEvent.VideoError.ERROR_MERGING, "THESE VIDEOS CAN NOT BE MERGED." +
                            " THEY HAVE DIFFERENT CODEC..."
                    )
            );
        }

    }
}
