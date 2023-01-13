package com.rbigsoft.unrar.nativeinterface;

import android.content.Context;
import android.util.Log;
import com.rbigsoft.unrar.model.document.FileSystemElement;

public class List7zipAsync extends ListRarAsync {
    private native void listeArchive(String str);

    public boolean isSuspended() {
        return false;
    }

    static {
        System.loadLibrary("zip");
    }

    public List7zipAsync(Context context, FileSystemElement fileSystemElement) {
        super(context, fileSystemElement);
    }

    /* access modifiers changed from: protected */
    public Integer doInBackground(String... strArr) {
        if (strArr.length == 1) {
            String str = strArr[0];
            String name = ExtractRarAsync.class.getName();
            Log.i(name, "List : " + str);
            listeArchive(str);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Integer num) {
        super.onPostExecute(num);
    }

    /* access modifiers changed from: protected */
    public void addNom(String str) {
        super.addNom(str);
    }

    /* access modifiers changed from: protected */
    public void addTaille(String str) {
        super.addTaille(str);
    }

    /* access modifiers changed from: protected */
    public void debut() {
        super.debut();
    }

    /* access modifiers changed from: protected */
    public void fin() {
        super.fin();
    }

    /* access modifiers changed from: protected */
    public void isCrypt() {
        super.isCrypt();
    }

    /* access modifiers changed from: protected */
    public void isDir() {
        super.isDir();
    }
}
