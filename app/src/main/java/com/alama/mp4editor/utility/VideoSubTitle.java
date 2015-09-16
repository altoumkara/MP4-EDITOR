package com.alama.mp4editor.utility;

import android.app.Activity;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.googlecode.mp4parser.srt.SrtParser;

import java.io.FileInputStream;
import java.io.IOException;

import com.alama.mp4editor.R;

/**
 * Class used to create a substitle for video
 *
 * @author Alama Tounkara
 */
public class VideoSubTitle {
    public static final String TAG = "VideoSubTitle";


    public static String build(String movieFilePath, String videoFolderPath, Activity activity) throws IOException {

        Movie countVideo = MovieCreator.build(new FileInputStream(movieFilePath).getChannel());

        TextTrackImpl subTitleEng = new TextTrackImpl();
        subTitleEng.getTrackMetaData().setLanguage("eng");



        subTitleEng.getSubs().add(new TextTrackImpl.Line(5000, 6000, "HELLO"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(8000, 9000, "HOW"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(12000, 13000, "ARE"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(16000, 17000, "YOU"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(19000, 20000, "DOING"));

        countVideo.addTrack(subTitleEng);

        TextTrackImpl subTitleDeu = SrtParser.parse(activity.getResources().openRawResource(R.raw.sub_title));

        subTitleDeu.getTrackMetaData().setLanguage("deu");
        countVideo.addTrack(subTitleDeu);

        IsoFile out = new DefaultMp4Builder().build(countVideo);
        return Util.placeFileInNonVolatileDrive(out, videoFolderPath, "subTitle");

    }


}

