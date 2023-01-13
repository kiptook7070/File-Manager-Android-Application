package com.rbigsoft.unrar.nativeinterface;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;

public class ExtractZipAsync extends ExtractRarAsync {
    public ExtractZipAsync(Context context) {
        super(context);
    }


    public void setSuspended(boolean z) {
        this.suspended = true;
    }


    @Override
    public void onPostExecute(Integer num) {
        this.diag.finish(filePath);
        super.onPostExecute(num);
    }

    public Integer doInBackground(String... strArr) {
        if (strArr.length == 2) {
            String str = strArr[0];
            this.archiveFileName = str;
            String str2 = strArr[1];
            String name = ExtractRarAsync.class.getName();
            Log.i(name, "Extract : " + str + " to " + str2);
            this.filePath = str2;
            extract(str, str2);
        } else if (strArr.length > 2) {
            String str3 = strArr[0];
            this.archiveFileName = str3;
            String str4 = strArr[1];
            this.filePath = str4;
            String[] strArr2 = new String[(strArr.length - 2)];
            int i = 0;
            for (int i2 = 2; i2 < strArr.length; i2++) {
                strArr2[i] = strArr[i2];
                i++;
            }
            extractZipFiles(str3, str4, strArr2, strArr2.length);
        }
        return 0;
    }

    private void extract(String str, String str2) {
        try {
            ZipFile zipFile = new ZipFile(str);
            zipFile.setRunInThread(true);
            if (zipFile.isEncrypted()) {
                String requestPassword = requestPassword();
                if (!this.suspended) {
                    if (requestPassword == null || requestPassword.length() <= 0) {
                        erreurCrc();
                        return;
                    }
                    zipFile.setPassword(requestPassword);
                } else {
                    return;
                }
            }
            if (zipFile.isSplitArchive()) {
                isMultiPart();
                if (this.suspended) {
                    return;
                }
            }
            zipFile.extractAll(str2);
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            while (progressMonitor.getState() == 1) {
                callback(progressMonitor.getPercentDone());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (this.suspended) {
                    progressMonitor.cancelAllTasks();
                }
            }
            if (progressMonitor.getResult() == 2) {
                erreurCrc();
            }
        } catch (ZipException unused) {
            erreurCrc();
        }
    }

    private void extractZipFiles(String str, String str2, String[] strArr, int i) {
        try {
            ZipFile zipFile = new ZipFile(str);
            String[] buildFilesFromHeader = buildFilesFromHeader(strArr, zipFile.getFileHeaders());
            if (zipFile.isEncrypted()) {
                String requestPassword = requestPassword();
                if (!this.suspended) {
                    if (requestPassword == null || requestPassword.length() <= 0) {
                        erreurCrc();
                        return;
                    }
                    zipFile.setPassword(requestPassword);
                } else {
                    return;
                }
            }
            zipFile.setRunInThread(true);
            for (int i2 = 0; i2 < buildFilesFromHeader.length; i2++) {
                zipFile.extractFile(buildFilesFromHeader[i2].substring(1, buildFilesFromHeader[i2].length()), str2);
                ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
                while (progressMonitor.getState() == 1) {
                    callback(progressMonitor.getPercentDone());
                    if (this.suspended) {
                        progressMonitor.cancelAllTasks();
                    }
                }
                if (progressMonitor.getResult() == 2) {
                    erreurCrc();
                    return;
                }
            }
        } catch (ZipException unused) {
            erreurCrc();
        }
    }

    private String[] buildFilesFromHeader(String[] strArr, List list) {
        ArrayList arrayList = new ArrayList();
        for (String str : strArr) {
            String substring = str.substring(1, str.length());
            int i = 0;
            while (true) {
                if (i >= list.size()) {
                    break;
                } else if (((FileHeader) list.get(i)).getFileName().equals(substring)) {
                    arrayList.add(str);
                    break;
                } else {
                    i++;
                }
            }
        }
        return (String[]) arrayList.toArray(new String[0]);
    }
}
