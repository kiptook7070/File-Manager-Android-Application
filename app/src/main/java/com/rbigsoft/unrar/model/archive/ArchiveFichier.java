package com.rbigsoft.unrar.model.archive;

public class ArchiveFichier extends ArchiveElement {
    public long taille;

    public ArchiveFichier(String str) {
        super(str);
    }

    public long getTaille() {
        return this.taille;
    }

    public void setTaille(long j) {
        this.taille = j;
    }
}
