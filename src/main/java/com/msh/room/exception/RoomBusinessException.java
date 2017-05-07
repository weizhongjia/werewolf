package com.msh.room.exception;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class RoomBusinessException extends RuntimeException {
    public RoomBusinessException() {
        super();
    }

    public RoomBusinessException(String message) {
        super(message);
    }

    public RoomBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoomBusinessException(Throwable cause) {
        super(cause);
    }

    protected RoomBusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
