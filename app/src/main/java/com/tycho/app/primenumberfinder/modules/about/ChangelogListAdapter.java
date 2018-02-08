package com.tycho.app.primenumberfinder.modules.about;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 10/26/2016
 */

public class ChangelogListAdapter extends RecyclerView.Adapter<ChangelogListAdapter.ViewHolderNumberList>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "ChangelogListAdapter";

    /**
     * List of factors in this adapter.
     */
    private List<ChangelogItem> changelog = new ArrayList<>();

    @Override
    public ViewHolderNumberList onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changelog_list_item, parent, false);
        return new ViewHolderNumberList(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderNumberList holder, int position){

        final ChangelogItem changelogItem = changelog.get(position);

        holder.versionName.setText(changelogItem.versionName);

        final StringBuilder stringBuilder = new StringBuilder();
        for (String note : changelogItem.changes){
            stringBuilder.append(note);
            stringBuilder.append(System.lineSeparator());
        }
        holder.changes.setText(stringBuilder);

    }

    @Override
    public int getItemCount(){
        return changelog.size();
    }

    public void addItem(final ChangelogItem item){
        this.changelog.add(item);
        notifyDataSetChanged();
    }

    public static class ChangelogItem{

        private final String versionName;

        private final List<String> changes;

        public ChangelogItem(final String versionName, final List<String> changes){
            this.versionName = versionName;
            this.changes = changes;
        }
    }

    class ViewHolderNumberList extends RecyclerView.ViewHolder{

        private final TextView versionName;
        private final TextView changes;

        ViewHolderNumberList(final View view){
            super(view);
            versionName = view.findViewById(R.id.version_name);
            changes = view.findViewById(R.id.changes);
        }
    }
}
