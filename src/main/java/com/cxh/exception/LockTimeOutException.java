package com.cxh.exception;

public class LockTimeOutException extends RuntimeException  {

    public LockTimeOutException(String cause){
        super(cause);
    }
}
