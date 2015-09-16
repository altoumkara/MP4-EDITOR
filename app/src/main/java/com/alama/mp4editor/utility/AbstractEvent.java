package com.alama.mp4editor.utility;

/**
 * This class is abstract and it is used of OTTO EVENTBUS. We are only using this class based of other event.
 * Otto eventbus can send event of any type.It doesnt tell you what an event(Object) should be. To make things
 * more consistent, I am using this class, that will be used to specify the type of event my Otto
 * eventBus can send.
 *  Created by alamatounkara on 9/8/15.
 */
public abstract class AbstractEvent {

    /**
     * define as Enum so each subclass can define its own type of event
     */
    private Enum mType;

    protected AbstractEvent(Enum type){
        this.mType = type;
    }

    public Enum getType() {
        return mType;
    }
}
