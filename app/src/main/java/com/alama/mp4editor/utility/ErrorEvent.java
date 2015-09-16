package com.alama.mp4editor.utility;


/**
 * Event class representing common error events that may happen across our app
 *
 * @author Alama Tounkara
 */
public class ErrorEvent extends AbstractEvent {
    public enum VideoError{ERROR_MERGING,ERROR_CROPPING};
    //result code to differentiate events
    private int mResulCode;
    //the error msg to display to user
    private String mErrorMsg;

    public ErrorEvent(Enum type, int resultCode, String errorMsg) {
        super(type);
        this.mResulCode=resultCode;
        this.mErrorMsg = errorMsg;
    }

    public ErrorEvent(Enum type, int resultCode) {
        super(type);
        this.mResulCode=resultCode;
    }

    public ErrorEvent(Enum type, String errorMsg) {
        super(type);
        this.mErrorMsg = errorMsg;
    }


    public String getErrorMsg() {
        return mErrorMsg;
    }

    public int getResulCode() {
        return mResulCode;
    }


}
