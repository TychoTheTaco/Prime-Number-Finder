package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.content.Context;

import com.tycho.app.primenumberfinder.SavedFileType;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesSmallListAdapter;

/**
 * @author Tycho Bellers
 *         Date Created: 11/4/2016
 */
//l
public class SavedFilesCard {

    private String title;
    private String subTitle;
    private int backgroundColor;

    private final String tag;

    private SavedFilesSmallListAdapter savedFilesAdapter;

    public SavedFilesCard(final String tag){
        this.tag = tag;
    }

    public SavedFilesCard(final Context context, String tag, String title, String subTitle, int backgroundColor, final SavedFileType fileType){
        this.title = title;
        this.subTitle = subTitle;
        this.tag = tag;
        this.backgroundColor = backgroundColor;
        this.savedFilesAdapter = new SavedFilesSmallListAdapter(context, fileType);
    }

    public SavedFilesSmallListAdapter getSavedFilesAdapter(){
        return savedFilesAdapter;
    }

    public String getTag(){
        return tag;
    }


    public int getBackgroundColor(){
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor){
        this.backgroundColor = backgroundColor;
    }

    public String getSubTitle(){
        return savedFilesAdapter.getItemCount() + " saved files";
    }

    public void setSubTitle(String subTitle){
        this.subTitle = subTitle;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }
}
