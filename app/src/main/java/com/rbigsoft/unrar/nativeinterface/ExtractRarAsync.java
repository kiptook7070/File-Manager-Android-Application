package com.rbigsoft.unrar.nativeinterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.sn.unzip.activity.AppDatabaseSQL;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;
import com.rbigsoft.unrar.dialog.FileExistDialog;
import com.rbigsoft.unrar.dialog.PasswordDialog;
import com.rbigsoft.unrar.dialog.UncompressDialog;
import com.rbigsoft.unrar.utilitaire.DialogUtil;
import com.rbigsoft.unrar.utilitaire.NotificationUtil;

import net.lingala.zip4j.util.InternalZipConstants;

public class ExtractRarAsync extends AsyncTask<String, ProgressDialog, Integer> implements Suspendable {
    public static BrowseSdcardActivity browser = null;
    protected String archiveFileName;
    protected Context context;

    public UncompressDialog diag;
    private boolean multipartDialogShown = false;

    public String password;

    public int resultFileExist;
    protected boolean suspended = false;
    private AppDatabaseSQL appDatabaseSQL;
    public String filePath = "";
    public String fileGet = "";
    public String[] fileGetList ;
    private SharedPreferences shareDeleteFile;

    private native void extractRar(String str, String str2);

    private native void extractRarFiles(String str, String str2, String[] strArr, int i);

    public void isMultiPart() {
    }

    static {
        System.loadLibrary("unrar");
    }

    public ExtractRarAsync(Context context2) {
        this.context = context2;
        shareDeleteFile = context2.getSharedPreferences("deletefile", Context.MODE_PRIVATE);
        appDatabaseSQL = new AppDatabaseSQL(context2);
        this.diag = new UncompressDialog(context2, this, context2.getString(R.string.decompress_encours));
    }

    public void callback(final int i) {
        ((Activity) this.context).runOnUiThread(new Runnable() {
            public void run() {
                ExtractRarAsync.this.diag.setProgressValue(i);
            }
        });
    }

    public void erreurCrc() {
        if (!this.suspended) {
            final Activity activity = (Activity) this.context;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    DialogUtil.buildErrorCRCDiag(activity).show();
                }
            });
        }
    }

    public synchronized int requestFileExist(final String str) {
        final Activity activity = (Activity) this.context;
        if (!this.suspended) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    final FileExistDialog fileExistDialog = new FileExistDialog(activity, str);
                    fileExistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialogInterface) {
                            synchronized (this) {
                                int unused = ExtractRarAsync.this.resultFileExist = fileExistDialog.getResult();
                                this.notify();
                            }
                        }
                    });
                    fileExistDialog.show();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            this.resultFileExist = 4;
        }
        if (this.resultFileExist == 6) {
            this.resultFileExist = 4;
            this.suspended = true;
        }
        return this.resultFileExist;
    }

    /* access modifiers changed from: protected */
    public synchronized String requestPassword() {
        final Activity activity = (Activity) this.context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final PasswordDialog passwordDialog = new PasswordDialog(activity);
                passwordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialogInterface) {
                        synchronized (this) {
                            String unused = ExtractRarAsync.this.password = passwordDialog.getPassword();
                            if (ExtractRarAsync.this.password == null) {
                                String unused2 = ExtractRarAsync.this.password = "";
                            }
                            if (passwordDialog.isCancel()) {
                                ExtractRarAsync.this.suspended = true;
                            }
                            this.notify();
                        }
                    }
                });
                passwordDialog.show();
            }
        });
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.password;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        this.diag.show();
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Integer num) {
        this.diag.finish(filePath);
        refreshBrowser();
    }


    public Integer doInBackground(String... strArr) {
        int i = 0;
        this.multipartDialogShown = false;
        if (strArr.length == 2) {
            String str = strArr[0];
            this.archiveFileName = str;
            String str2 = strArr[1] + InternalZipConstants.ZIP_FILE_SEPARATOR;
            filePath = str2;
            Log.i(ExtractRarAsync.class.getName(), "Extract : " + str + " to " + str2);
            fileGet = str;
            extractRar(str, str2);
        } else if (strArr.length > 2) {
            String str3 = strArr[0];
            this.archiveFileName = str3;
            String str4 = strArr[1];
            filePath = str4;
            String[] strArr2 = new String[(strArr.length - 2)];
            for (int i2 = 2; i2 < strArr.length; i2++) {
                strArr2[i] = strArr[i2];
                i++;
            }
            extractRarFiles(str3, str4, strArr2, strArr2.length);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void refreshBrowser() {
        BrowseSdcardActivity browseSdcardActivity = browser;
        if (browseSdcardActivity != null) {
            browseSdcardActivity.refresh();
        }
    }

    public boolean isSuspended() {
        return this.suspended;
    }

    public void setSuspended(boolean z) {
        this.suspended = z;
    }
}
