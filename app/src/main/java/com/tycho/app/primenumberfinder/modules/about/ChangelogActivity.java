package com.tycho.app.primenumberfinder.modules.about;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.AbstractActivity;

/**
 * Created by tycho on 2/7/2018.
 */

public class ChangelogActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ChangelogActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.changelog_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up RecyclerView
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ChangelogListAdapter changelogListAdapter = new ChangelogListAdapter(Changelog.readChangelog(getResources().openRawResource(R.raw.changelog)));
        recyclerView.setAdapter(changelogListAdapter);
        recyclerView.setItemAnimator(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
