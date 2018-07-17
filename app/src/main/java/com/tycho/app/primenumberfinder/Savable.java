package com.tycho.app.primenumberfinder;

public interface Savable {
    boolean save();

    boolean isSaved();

    interface SaveListener{
        void onSaved();
        void onError();
    }
}
