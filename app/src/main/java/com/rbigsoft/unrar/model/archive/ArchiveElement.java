package com.rbigsoft.unrar.model.archive;

public class ArchiveElement {
    protected String chemin = "";
    private boolean crypt;
    private String nom;
    private ArchiveDossier parent;

    public ArchiveElement(String str) {
        this.nom = str;
    }

    public boolean isCrypt() {
        return this.crypt;
    }

    public void setCrypt(boolean z) {
        this.crypt = z;
    }

    public String getNom() {
        return this.nom;
    }

    public ArchiveDossier getParent() {
        return this.parent;
    }

    public void setParent(ArchiveDossier archiveDossier) {
        this.parent = archiveDossier;
    }

    public String getChemin() {
        return this.chemin;
    }
}
