package com.tycho.app.primenumberfinder.utils;

import android.content.res.Resources;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public enum FileType{
    PRIMES(0),
    FACTORS(1),
    TREE(2);

    private final int id;

    FileType(final int id){
        this.id = id;
    }

    /**
     * Find a FileType by its ID.
     * @param id The ID to search for
     * @return The corresponding FileType
     */
    public static FileType findById(final int id){

        //Search through each FileType
        for (FileType fileType : FileType.values()){
            if (fileType.id == id){
                return fileType;
            }
        }

        //No match found
        throw new Resources.NotFoundException("No resource found that matches the specified ID");
    }

    public int getId(){
        return id;
    }
}
