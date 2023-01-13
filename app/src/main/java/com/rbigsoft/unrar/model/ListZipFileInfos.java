package com.rbigsoft.unrar.model;

import java.io.File;
import java.util.ArrayList;
import net.lingala.zip4j.model.ZipParameters;

public class ListZipFileInfos {
    private String cheminDest;
    private boolean crypt = false;
    private ArrayList<File> listeFichier;
    private boolean multiPart = false;
    private String nomArchive;
    private ZipParameters parameters;
    private String pass;
    private long splitSize;

    public ArrayList<File> getListeFichier() {
        return this.listeFichier;
    }

    public void setListeFichier(ArrayList<File> arrayList) {
        this.listeFichier = arrayList;
    }

    public boolean isCrypt() {
        return this.crypt;
    }

    public void setCrypt(boolean z) {
        this.crypt = z;
    }

    public ZipParameters getParameters() {
        return this.parameters;
    }

    public void setParameters(ZipParameters zipParameters) {
        this.parameters = zipParameters;
    }

    public String getPass() {
        return this.pass;
    }

    public void setPass(String str) {
        this.pass = str;
    }

    public String getNomArchive() {
        return this.nomArchive;
    }

    public void setNomArchive(String str) {
        this.nomArchive = str;
    }

    public long getSplitSize() {
        return this.splitSize;
    }

    public void setSplitSize(long j) {
        this.splitSize = j;
    }

    public boolean isMultiPart() {
        return this.multiPart;
    }

    public void setMultiPart(boolean z) {
        this.multiPart = z;
    }

    public String getCheminDest() {
        return this.cheminDest;
    }

    public void setCheminDest(String str) {
        this.cheminDest = str;
    }
}
