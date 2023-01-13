package com.rbigsoft.unrar.nativeinterface;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

public class Extract7zipAsync extends ExtractRarAsync {
    private native void extract(String str, String str2);

    private String filePath = "";

    private native void extractFiles(String str, String str2, String str3);

    static {
        System.loadLibrary("zip");
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }
    public Extract7zipAsync(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public Integer doInBackground(String... strArr) {
        if (strArr.length == 2) {
            String str = strArr[0];
            this.archiveFileName = str;
            String str2 = "-o" + strArr[1];

            this.filePath = str2.replace("-o", "");
            Log.i(ExtractRarAsync.class.getName(), "Extract : " + str + " to " + str2);
            extract(str, str2);
        } else if (strArr.length > 2) {
            String str3 = strArr[0];
            this.archiveFileName = str3;
            String str4 = "-o" + strArr[1];

            this.filePath = str4.replace("-o", "");
            String[] strArr2 = new String[(strArr.length - 2)];
            Log.d("7zip extracts", str4);
            try {
                File createTmpFile = createTmpFile(strArr);
                Log.d("7zip extracts", createTmpFile.getAbsolutePath());
                extractFiles(str3, str4, "@" + createTmpFile.getAbsolutePath());
                createTmpFile.delete();
            } catch (IOException e) {
                Log.d("7zip extracts error", e.getMessage());
            }
        }
        return 0;
    }

    private File createTmpFile(String[] strArr) throws IOException {
        File createTempFile = File.createTempFile(UUID.randomUUID().toString(), "tmpfile", this.context.getCacheDir());
        FileWriter fileWriter = new FileWriter(createTempFile);
        for (int i = 2; i < strArr.length; i++) {
            Log.d("7zip extracts", strArr[i]);
            fileWriter.append(strArr[i] + IOUtils.LINE_SEPARATOR_UNIX);
        }
        fileWriter.close();
        return createTempFile;
    }

    public void setSuspended(boolean z) {
        this.suspended = true;
    }

    public boolean isSuspended() {
        return this.suspended;
    }

    /* access modifiers changed from: protected */
    public synchronized int requestFileExist(String str) {
        return super.requestFileExist(str);
    }

    /* access modifiers changed from: protected */
    public void callback(int i) {
        super.callback(i);
    }
}
