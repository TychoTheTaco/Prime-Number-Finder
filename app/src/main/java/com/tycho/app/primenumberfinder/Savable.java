package com.tycho.app.primenumberfinder;

public interface Savable {

    void save();

    interface SavableCallbacks{
        void onSaved();
        void onError();
    }
}
