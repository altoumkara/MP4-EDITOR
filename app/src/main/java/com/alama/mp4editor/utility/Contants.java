package com.alama.mp4editor.utility;

import android.os.Environment;

import java.io.File;

/**
 * Constant class containing the global constant needed in multiple place in our app
 *@author Alama Tounkara
 */
public class Contants {

    /**
     * I want the video to be saved in the external memory's 'Video' folder
     */
    public static final File EXTERNAL_VIDEO_PUBLIC_DIR = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);


}
