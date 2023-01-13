package com.rbigsoft.unrar.model.document;

import java.io.Serializable;

import net.lingala.zip4j.util.InternalZipConstants;

public class FileSystemElement implements Serializable {
    private static final long serialVersionUID = 4280255846463688408L;
    private String chemin;
    private int id;
    private String nom;
    private String nomCrypte;
    private FileSystemElement parent;

    public FileSystemElement(String chemin, String nom) {
        this.chemin = chemin;
        this.nom = nom;
    }

    public FileSystemElement(String str, FileSystemElement fileSystemElement) {
        this.nom = str;
        if (fileSystemElement != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(fileSystemElement.getChemin());
            sb.append(InternalZipConstants.ZIP_FILE_SEPARATOR);
            sb.append(str);
            this.chemin = sb.toString();
            this.parent = fileSystemElement;
        }
    }

    public FileSystemElement getParent() {
        return this.parent;
    }

    public void setParent(FileSystemElement fileSystemElement) {
        this.parent = fileSystemElement;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String str) {
        this.nom = str;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getChemin() {
        return this.chemin;
    }

    public void setChemin(String str) {
        this.chemin = str;
    }

    public String getNomCrypte() {
        return this.nomCrypte;
    }

    public void setNomCrypte(String str) {
        this.nomCrypte = str;
    }
}
