package com.tycho.app.primenumberfinder.modules;

import android.view.Menu;
import android.view.MenuItem;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;

import easytasks.Task;

/**
 * @author Tycho Bellers
 * Date Created: 7/24/2018
 */
public abstract class ConfigurationActivity<T extends Task> extends AbstractActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ConfigurationActivity.class.getSimpleName();

    protected T task;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_configuration_menu, menu);

        if (task != null) {
            menu.findItem(R.id.start).setIcon(R.drawable.ic_save_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Give the root view focus to prevent EditTexts from initially getting focus
        findViewById(R.id.root).requestFocus();
    }

}
