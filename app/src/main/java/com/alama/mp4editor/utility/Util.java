package com.alama.mp4editor.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * Utility class to represent common methods
 *
 * @author Alama Tounkara
 */
public class Util {
    public static final String TAG = "Util";

    /**
     *
     * After we created the Movie, we have to place it into a nonvolatile memory.
     *
     * @param isoFile         is the isoFile
     * @param videoFolderPath is the path to our 'video' folder
     * @param outPutName is the name of the output file. Note that a time stamp will be appended
     *                   to this name in order to avoid name conflicts
     * @return the absolute path to the newly created video so we can start playing the video
     * in the video
     */
    public static String placeFileInNonVolatileDrive(IsoFile isoFile, String videoFolderPath,
                                                       String outPutName) {
        long time = new Date().getTime();
        String strTimeStamp = "" + time;
        String outputFolderPath;
        //getting our video folder on the secondary memory and save the new video file we just make
        File myMovie = new File(videoFolderPath, String.format(outPutName+"_%s", strTimeStamp));
        outputFolderPath = myMovie.getAbsolutePath().toString();
        Log.d(TAG, "outputFolderPath: " + outputFolderPath);

        FileOutputStream fileOutputStream = null;
        FileChannel filechannel = null;
        try {
            fileOutputStream = new FileOutputStream(myMovie);
            filechannel = fileOutputStream.getChannel();
            //filechannel.position(0);
            isoFile.getBox(filechannel);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "FILE NOT FOUND: " + e);
        } catch (IOException e) {
            Log.e(TAG, "IO ERROR: " + e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "IO ERROR: " + e);
                }
            }
            if (filechannel != null) {
                try {
                    filechannel.close();
                } catch (IOException e) {
                    Log.e(TAG, "IO ERROR: " + e);
                }
            }
        }
        //return the absolute path to the newly created video so we can start playing the video
        //in the video
        return outputFolderPath;
    }


    /**
     * display error msg
     * @param context the context in which you are calling this
     * @param message message to display
     */
    public static void displayError(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
