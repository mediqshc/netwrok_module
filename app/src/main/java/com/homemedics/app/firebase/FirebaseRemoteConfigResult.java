package com.homemedics.app.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException;

public class FirebaseRemoteConfigResult {

    private boolean isSuccessful;
    private Exception exception;

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public boolean isThrottled() {
        return exception instanceof FirebaseRemoteConfigFetchThrottledException;
    }

    public Exception getException() {
        return exception;
    }

    @NonNull
    @Override
    public String toString() {
        return "FirebaseRemoteConfigResult{" + "isSuccessful=" + isSuccessful +
                ", exception=" + exception + '}';
    }
}
