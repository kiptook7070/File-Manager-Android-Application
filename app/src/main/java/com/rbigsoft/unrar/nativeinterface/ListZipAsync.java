package com.rbigsoft.unrar.nativeinterface;

import android.content.Context;
import android.util.Log;
import com.rbigsoft.unrar.dialog.ArchiveDiag;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.utilitaire.ArchiveStrToArchiveDossier;
import java.util.List;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class ListZipAsync extends ListRarAsync {
    public ListZipAsync(Context context, FileSystemElement fileSystemElement) {
        super(context, fileSystemElement);
    }

    /* access modifiers changed from: protected */
    public Integer doInBackground(String... strArr) {
        if (strArr.length == 1) {
            String str = strArr[0];
            String name = ExtractRarAsync.class.getName();
            Log.i(name, "List : " + str);
            listZip(str);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Integer num) {
        ArchiveDiag archiveDiag = new ArchiveDiag(this.context, new ArchiveStrToArchiveDossier().toArchiveDossierZip(this.listArchiveFile), this.archive);
        this.progressDiag.dismiss();
        archiveDiag.show();
    }

    private void listZip(String str) {
        try {
            List fileHeaders = new ZipFile(str).getFileHeaders();
            for (int i = 0; i < fileHeaders.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaders.get(i);
                debut();
                addNom(fileHeader.getFileName());
                addTaille(String.valueOf(fileHeader.getUncompressedSize()));
                if (fileHeader.getEncryptionMethod() != -1) {
                    isCrypt();
                }
                if (fileHeader.isDirectory()) {
                    isDir();
                }
                fin();
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
