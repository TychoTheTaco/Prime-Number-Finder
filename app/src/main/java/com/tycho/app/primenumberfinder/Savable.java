package com.tycho.app.primenumberfinder;

public interface Savable {
    boolean save();

    interface SaveListener{
        void onSaved();
        void onError();
    }
}
