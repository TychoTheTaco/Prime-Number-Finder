package com.tycho.app.primenumberfinder.modules.about;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.tycho.app.primenumberfinder.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import easytasks.TaskAdapter;

/**
 * Created by tycho on 2/7/2018.
 */

public class ChangelogActivity extends AppCompatActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "ChangelogActivity";

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
        final ChangelogListAdapter changelogListAdapter = new ChangelogListAdapter();
        recyclerView.setAdapter(changelogListAdapter);
        recyclerView.setItemAnimator(null);

        final List<ChangelogListAdapter.ChangelogItem> changelogItems = readChangelog();
        Collections.reverse(changelogItems);
        for (ChangelogListAdapter.ChangelogItem item : changelogItems){
            changelogListAdapter.addItem(item);
        }
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

    private List<ChangelogListAdapter.ChangelogItem> readChangelog(){
        Pattern versionPattern = Pattern.compile("(\\d+\\/\\d+\\/\\d+).+?(\\d+\\..+)");
        final Pattern devVersionPattern = Pattern.compile("(\\?+\\/\\?+\\/\\d+).+?(\\d+\\..+)");
        Pattern itemPattern = Pattern.compile(".+");
        Matcher matcher;
        Matcher devMatcher;
        Matcher itemMatcher;

        final List<ChangelogListAdapter.ChangelogItem> changelogItems = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        String version = "unknown";

        String line;
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.changelog)));
            while ((line = bufferedReader.readLine()) != null){
                matcher = versionPattern.matcher(line);
                devMatcher = devVersionPattern.matcher(line);
                itemMatcher = itemPattern.matcher(line);
                if (matcher.find() || devMatcher.find()){
                    try{ version = matcher.group(2);}catch (Exception e){
                        version = devMatcher.group(2);
                    }
                    notes = new ArrayList<>();
                }else if (itemMatcher.find()){
                    notes.add(itemMatcher.group());
                }else{
                    Log.d(TAG, "String not recognized: " + line);
                    if (!notes.isEmpty()){
                        changelogItems.add(new ChangelogListAdapter.ChangelogItem(version, notes));
                    }
                }
            }
            if (!notes.isEmpty()){
                changelogItems.add(new ChangelogListAdapter.ChangelogItem(version, notes));
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return changelogItems;
    }
}
