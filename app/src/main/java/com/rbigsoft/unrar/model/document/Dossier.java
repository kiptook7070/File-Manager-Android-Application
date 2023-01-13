package com.rbigsoft.unrar.model.document;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.util.InternalZipConstants;

public class Dossier extends FileSystemElement implements Serializable {
    private static final long serialVersionUID = -2443343887077933955L;
    private List<FileSystemElement> contenu = new ArrayList();
    private int nbrFile;

    public Dossier(String str, FileSystemElement fileSystemElement) {
        super(str, fileSystemElement);
        computeFileNumber();
    }

    public void ajouterElement(FileSystemElement fileSystemElement) {
        this.contenu.add(fileSystemElement);
        fileSystemElement.setParent(this);
    }

    public void supprElement(FileSystemElement fileSystemElement) {
        this.contenu.remove(fileSystemElement);
    }

    public List<FileSystemElement> getContenu() {
        return this.contenu;
    }

    public void setChemin(String str) {
        super.setChemin(str);
        for (FileSystemElement fileSystemElement : this.contenu) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(InternalZipConstants.ZIP_FILE_SEPARATOR);
            sb.append(fileSystemElement.getNom());
            fileSystemElement.setChemin(sb.toString());
        }
    }

    private void computeFileNumber() {
        if (getChemin() != null) {
            File[] listFiles = new File(getChemin()).listFiles();
            if (listFiles != null) {
                this.nbrFile = listFiles.length;
            }
        }
    }

    public int getNbrFile() {
        return this.nbrFile;
    }

    public FileSystemElement getElementByName(String str) {
        if (str != null) {
            for (FileSystemElement fileSystemElement : this.contenu) {
                if (fileSystemElement.getNom() != null && fileSystemElement.getNom().equals(str)) {
                    return fileSystemElement;
                }
            }
        }
        return null;
    }
}
