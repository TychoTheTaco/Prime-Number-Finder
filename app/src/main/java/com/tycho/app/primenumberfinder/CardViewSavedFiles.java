package com.tycho.app.primenumberfinder;

import android.content.Context;

import com.tycho.app.primenumberfinder.Adapters_old.AdapterSavedFiles;
import com.tycho.app.primenumberfinder.Adapters_old.TestAdapter;

/**
 * @author Tycho Bellers
 *         Date Created: 11/4/2016
 */
//I really hope this works
public class CardViewSavedFiles{

    private String title;
    private String subTitle;
    private int backgroundColor;

    private final String tag;

    private AdapterSavedFiles adapterSavedFiles;

    private TestAdapter testAdapter;

    public CardViewSavedFiles(final String tag){
        this.tag = tag;
    }

    public CardViewSavedFiles(final Context context, String tag, String title, String subTitle, int backgroundColor, final SavedFileType fileType){
        this.title = title;
        this.subTitle = subTitle;
        this.tag = tag;
        this.backgroundColor = backgroundColor;
        this.adapterSavedFiles = new AdapterSavedFiles(context, fileType);
        this.testAdapter = new TestAdapter(context, fileType);
    }

    public AdapterSavedFiles getAdapterSavedFiles(){
        return adapterSavedFiles;
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
        return adapterSavedFiles.getItemCount() + " saved files";
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

    public TestAdapter getAdapterSavedFilesTest(){
        return testAdapter;
    }
}
