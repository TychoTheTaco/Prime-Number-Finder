package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.content.Context;

import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.FilesListAdapter;

import java.io.File;

/**
 * @author Tycho Bellers
 *         Date Created: 11/4/2016
 */
public class SavedFilesCard {

    /**
     * Title of the card
     */
    private String title;

    /**
     * The directory that this card is showing files from.
     */
    private final File directory;

    /**
     * The background color.
     */
    private int backgroundColor;

    /**
     * A user-assigned tag used to identify this card.
     */
    private final String tag;

    private FilesListAdapter filesListAdapter;

    public SavedFilesCard(final Context context, String tag, String title, int backgroundColor, final File directory){
        this.title = title;
        this.tag = tag;
        this.backgroundColor = backgroundColor;
        this.directory = directory;
        this.filesListAdapter = new FilesListAdapter(directory, context);
    }

    public FilesListAdapter getFilesListAdapter() {
        return filesListAdapter;
    }

    public String getTag(){
        return tag;
    }

    public int getBackgroundColor(){
        return backgroundColor;
    }

    public String getSubTitle(){
        return filesListAdapter.getItemCount() + " saved files";
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public File getDirectory() {
        return directory;
    }
}
