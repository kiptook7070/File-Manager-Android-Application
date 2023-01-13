package com.rbigsoft.unrar.model;

public class ArchiveFileString {
    private boolean crypt = false;
    private boolean isDir = false;
    private String nom = "test";
    private long taille;

    public String getNom() {
        return this.nom;
    }

    public void setNom(String str) {
        this.nom = str;
    }

    public long getTaille() {
        return this.taille;
    }

    public void setTaille(String str) {
        this.taille = Long.valueOf(str).longValue();
    }

    public boolean isCrypt() {
        return this.crypt;
    }

    public void setCrypt(boolean z) {
        this.crypt = z;
    }

    public boolean isDir() {
        return this.isDir;
    }

    public void setDir(boolean z) {
        this.isDir = z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.nom);
        String str = " ";
        sb.append(str);
        sb.append(String.valueOf(this.taille));
        sb.append(str);
        sb.append(String.valueOf(this.crypt));
        sb.append(str);
        sb.append(String.valueOf(this.isDir));
        return sb.toString();
    }
}
