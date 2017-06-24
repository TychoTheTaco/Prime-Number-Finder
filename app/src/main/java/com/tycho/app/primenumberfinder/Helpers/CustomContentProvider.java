package com.tycho.app.primenumberfinder.Helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

public class CustomContentProvider extends android.content.ContentProvider{
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException{
        //String path = ViewFileContentsAdapter.file.getPath();
        String path = "path";
        File privateFile = new File(path);

        Log.e("ContentProvider", "path: " + path + " fromFile: " + Uri.fromFile(privateFile));

        return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public boolean onCreate(){
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri){
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values){
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        return 0;
    }
}
