package com.tycho.app.primenumberfinder.utils;

import android.content.res.Resources;

import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public enum FileType{
    UNKNOWN(-1, null),
    PRIMES(0, DisplayPrimesActivity.class),
    FACTORS(1, DisplayFactorsActivity.class),
    TREE(2, DisplayPrimeFactorizationActivity.class);

    private final int id;

    private final Class cls;

    FileType(final int id, final Class cls){
        this.id = id;
        this.cls = cls;
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

    public Class getOpeningClass() {
        return cls;
    }

    public int getId(){
        return id;
    }
}
