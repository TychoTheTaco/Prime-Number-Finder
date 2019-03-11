package com.tycho.app.primenumberfinder.modules.savedfiles;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DataFile {

    protected final File file;

    //Header
    private final int version;
    protected final int headerLength;

    public DataFile(final File file, final boolean readHeader) throws IOException {
        this.file = file;

        //Read header
        if (readHeader){
            final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            version = dataInputStream.readUnsignedByte();
            headerLength = dataInputStream.readUnsignedByte();
            dataInputStream.close();
        }else{
            version = -1;
            headerLength = 0;
        }
    }

    public File getFile(){
        return file;
    }

    public String getTitle(){
        return file.getName();
    }
}
