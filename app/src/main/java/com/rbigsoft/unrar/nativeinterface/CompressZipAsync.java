package com.rbigsoft.unrar.nativeinterface;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.sn.unzip.activity.FileActivity;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;
import com.rbigsoft.unrar.dialog.UncompressDialog;
import com.rbigsoft.unrar.model.ListZipFileInfos;
import com.rbigsoft.unrar.utilitaire.DialogUtil;
import com.rbigsoft.unrar.utilitaire.NotificationUtil;

import java.io.File;
import java.util.ArrayList;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.InternalZipConstants;

public class CompressZipAsync extends AsyncTask<ListZipFileInfos, ProgressDialog, Integer> implements Suspendable {

    public UncompressDialog compressDiag;
    private BrowseSdcardActivity context;
    private FileActivity fileActivity;
    private boolean suspended;

    public CompressZipAsync(BrowseSdcardActivity browseSdcardActivity, FileActivity fileActivity) {
        if (browseSdcardActivity != null) {
            this.context = browseSdcardActivity;
            this.compressDiag = new UncompressDialog(browseSdcardActivity, this, browseSdcardActivity.getString(R.string.compress_encours));
        }
        if (fileActivity != null) {
            this.fileActivity = fileActivity;
            this.compressDiag = new UncompressDialog(fileActivity, this, fileActivity.getString(R.string.compress_encours));
        }
    }

    /* access modifiers changed
     from: protected */
    public void onPreExecute() {
        this.compressDiag.show();
    }

    private void pourcent(final int i) {
        if (context != null) {
            this.context.runOnUiThread(new Runnable() {
                public void run() {
                    CompressZipAsync.this.compressDiag.setProgressValue(i);
                }
            });
        }
        if (fileActivity != null) {
            this.fileActivity.runOnUiThread(new Runnable() {
                public void run() {
                    CompressZipAsync.this.compressDiag.setProgressValue(i);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Integer num) {
        this.compressDiag.finish("");
        if (this.context != null) {
            refreshBrowser();
        }
        //NotificationUtil.sendNotification(this.context, "tes file");
    }

    /* access modifiers changed from: protected */
    public Integer doInBackground(ListZipFileInfos... listZipFileInfosArr) {
        if (listZipFileInfosArr.length > 0) {
            compress(listZipFileInfosArr[0]);
        }
        return 1;
    }

    private void compress(ListZipFileInfos listZipFileInfos) {
        String str = listZipFileInfos.getCheminDest() + InternalZipConstants.ZIP_FILE_SEPARATOR + listZipFileInfos.getNomArchive();
        try {
            ZipFile zipFile = new ZipFile(str);
            zipFile.setRunInThread(true);
            if (!listZipFileInfos.isMultiPart()) {
                zipFile.createZipFile((ArrayList) listZipFileInfos.getListeFichier(), listZipFileInfos.getParameters());
            } else {
                zipFile.createZipFile((ArrayList) listZipFileInfos.getListeFichier(), listZipFileInfos.getParameters(), true, listZipFileInfos.getSplitSize());
            }
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            while (progressMonitor.getState() == 1) {
                pourcent(progressMonitor.getPercentDone());
                Thread.sleep(500);
                if (this.suspended) {
                    progressMonitor.cancelAllTasks();
                    throw new Exception("Cancel task");
                }
            }
            if (progressMonitor.getResult() == 2) {
                deleteIncorrectZip(str);
                erreurCompress(progressMonitor.getException().getMessage());
            }
        } catch (ZipException unused) {
            deleteIncorrectZip(str);
        } catch (Exception unused2) {
            deleteIncorrectZip(str);
        }
    }

    private void deleteIncorrectZip(String str) {
        File file = new File(str);
        if (file.exists()) {
            file.delete();
        }
    }

    public void setSuspended(boolean z) {
        this.suspended = true;
    }

    private void refreshBrowser() {
        BrowseSdcardActivity browseSdcardActivity = this.context;
        if (browseSdcardActivity != null) {
            browseSdcardActivity.refresh();
        }
    }

    private void erreurCompress(final String str) {
        if (context != null) {
            final BrowseSdcardActivity browseSdcardActivity = this.context;
            browseSdcardActivity.runOnUiThread(new Runnable() {
                public void run() {
                    DialogUtil.errorBuildZip(browseSdcardActivity, str).show();
                }
            });
        }
        if (fileActivity != null) {
            final FileActivity fileActivity = this.fileActivity;
            fileActivity.runOnUiThread(new Runnable() {
                public void run() {
                    DialogUtil.errorBuildZip(fileActivity, str).show();
                }
            });
        }
    }
}
