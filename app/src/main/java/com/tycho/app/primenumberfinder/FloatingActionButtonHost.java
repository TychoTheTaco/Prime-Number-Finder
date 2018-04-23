package com.tycho.app.primenumberfinder;

import android.support.design.widget.FloatingActionButton;

public interface FloatingActionButtonHost {

    /**
     * Returns the {@linkplain FloatingActionButton} at the specified index. Index starts at 0 for
     * the lowest button.
     *
     * @param index The index of the FloatingActionButton.
     * @return The {@linkplain FloatingActionButton} at the specified index. Can be null depending
     * on implementations.
     */
    FloatingActionButton getFab(final int index);
}
