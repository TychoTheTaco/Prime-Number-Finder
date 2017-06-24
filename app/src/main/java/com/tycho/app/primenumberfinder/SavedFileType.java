package com.tycho.app.primenumberfinder;

import android.content.res.Resources;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public enum SavedFileType{
    PRIMES(0),
    FACTORS(1),
    FACTOR_TREE(2);

    private final int id;

    SavedFileType(final int id){
        this.id = id;
    }

    /**
     * Find a SavedFileType by its ID.
     * @param id The ID to search for
     * @return The corresponding SavedFileType
     */
    public static SavedFileType findById(final int id){

        //Search through each SavedFileType
        for (SavedFileType savedFileType : SavedFileType.values()){
            if (savedFileType.id == id){
                return savedFileType;
            }
        }

        //No match found
        throw new Resources.NotFoundException("No resource found that matches the specified ID");
    }

    public int getId(){
        return id;
    }
}
