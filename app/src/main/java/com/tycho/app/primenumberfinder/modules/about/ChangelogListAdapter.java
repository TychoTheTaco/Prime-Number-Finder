package com.tycho.app.primenumberfinder.modules.about;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;

/**
 * @author Tycho Bellers
 *         Date Created: 10/26/2016
 */

public class ChangelogListAdapter extends RecyclerView.Adapter<ChangelogListAdapter.ViewHolderNumberList>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ChangelogListAdapter.class.getSimpleName();

    private Changelog changelog;

    public ChangelogListAdapter(final Changelog changelog){
        this.changelog = changelog;
    }

    @NonNull
    @Override
    public ViewHolderNumberList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changelog_list_item, parent, false);
        return new ViewHolderNumberList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderNumberList holder, int position) {
        final Changelog.Release release = changelog.getReleases().get(changelog.getReleases().size() - 1 - position);

        holder.versionName.setText(release.getVersionName());

        holder.changes.setText(release.concatenate());
    }

    @Override
    public int getItemCount(){
        return changelog == null ? 0 : changelog.getReleases().size();
    }

    public void setChangelog(Changelog changelog) {
        this.changelog = changelog;
        notifyDataSetChanged();
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
