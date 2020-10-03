package com.tycho.app.primenumberfinder.modules;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.AbstractActivity;

/**
 * @author Tycho Bellers
 * Date Created: 3/26/2019
 */
public abstract class TaskConfigurationActivity extends AbstractActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = TaskConfigurationActivity.class.getSimpleName();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_configuration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.start:
                if (isConfigurationValid()) {
                    final Intent intent = new Intent();
                    buildReturnIntent(intent);
                    setResult(0, intent);
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.error_invalid_configuration), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Give the root view focus to prevent EditTexts from initially getting focus
        //findViewById(R.id.root).requestFocus();
    }


    protected abstract void buildReturnIntent(final Intent intent);

    protected abstract boolean isConfigurationValid();
}
