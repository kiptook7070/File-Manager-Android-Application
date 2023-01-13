package com.rbigsoft.unrar.model.document;

import java.io.File;
import java.io.Serializable;

public class Fichier extends FileSystemElement implements Serializable {
    private static final long serialVersionUID = 7488787550828891481L;
    private long fileSize = new File(getChemin()).length();

    public Fichier(String str, FileSystemElement fileSystemElement) {
        super(str, fileSystemElement);
    }

    public long getFileSize() {
        return this.fileSize;
    }
}
