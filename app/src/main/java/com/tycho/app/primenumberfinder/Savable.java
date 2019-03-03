package com.tycho.app.primenumberfinder;

public interface Savable {

    interface SaveListener{
        void onSaved();
        void onError();
    }

    void addSaveListener(final SaveListener listener);
    void removeSaveListener(final SaveListener listener);

    boolean save();

    boolean isSaved();
}
