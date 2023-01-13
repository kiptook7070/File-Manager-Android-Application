package com.rbigsoft.unrar.nativeinterface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.rbigsoft.unrar.dialog.ArchiveDiag;
import com.rbigsoft.unrar.model.ArchiveFileString;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.utilitaire.ArchiveStrToArchiveDossier;
import java.util.ArrayList;
import java.util.List;

public class ListRarAsync extends AsyncTask<String, ProgressDialog, Integer> {
    protected FileSystemElement archive;
    protected Context context;
    protected ArchiveFileString currentArchiveFile;
    protected List<ArchiveFileString> listArchiveFile = new ArrayList();
    protected ProgressDialog progressDiag;

    private native void listRar(String str);

    static {
        System.loadLibrary("unrar");
    }

    public ListRarAsync(Context context2, FileSystemElement fileSystemElement) {
        this.archive = fileSystemElement;
        this.context = context2;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        this.progressDiag = new ProgressDialog(this.context);
        this.progressDiag.setMessage("Please wait...");
        this.progressDiag.show();
        super.onPreExecute();
    }

    /* access modifiers changed from: protected */
    public Integer doInBackground(String... strArr) {
        if (strArr.length == 1) {
            String str = strArr[0];
            String name = ExtractRarAsync.class.getName();
            Log.i(name, "List : " + str);
            listRar(str);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Integer num) {
        ArchiveDiag archiveDiag = new ArchiveDiag(this.context, new ArchiveStrToArchiveDossier().toArchiveDossier(this.listArchiveFile), this.archive);
        this.progressDiag.dismiss();
        archiveDiag.show();
    }

    /* access modifiers changed from: protected */
    public void debut() {
        this.currentArchiveFile = new ArchiveFileString();
    }

    /* access modifiers changed from: protected */
    public void isCrypt() {
        this.currentArchiveFile.setCrypt(true);
    }

    /* access modifiers changed from: protected */
    public void isDir() {
        this.currentArchiveFile.setDir(true);
    }

    /* access modifiers changed from: protected */
    public void addNom(String str) {
        this.currentArchiveFile.setNom(str);
    }

    /* access modifiers changed from: protected */
    public void addTaille(String str) {
        this.currentArchiveFile.setTaille(str);
    }

    /* access modifiers changed from: protected */
    public void fin() {
        this.listArchiveFile.add(this.currentArchiveFile);
    }
}
