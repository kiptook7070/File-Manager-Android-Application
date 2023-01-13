package com.rbigsoft.unrar.utilitaire;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.rbigsoft.sn.unzip.R;

import java.text.NumberFormat;

public class AvailableSpaceHandler {
    public static final long SIZE_GB = 1073741824;
    public static final long SIZE_KB = 1024;
    public static final long SIZE_MB = 1048576;

    public static long getExternalAvailableSpaceInBytes(String str) {
        try {
            StatFs statFs = new StatFs(str);
            return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getExternalAvailableSpaceInKB(String str) {
        return getExternalAvailableSpaceInBytes(str) / 1024;
    }

    public static long getExternalAvailableSpaceInMB(String str) {
        return getExternalAvailableSpaceInBytes(str) / 1048576;
    }

    public static long getExternalAvailableSpaceInGB(String str) {
        return getExternalAvailableSpaceInBytes(str) / 1073741824;
    }

    public static long getExternalStorageAvailableBlocks() {
        try {
            return (long) new StatFs(Environment.getExternalStorageDirectory().getPath()).getAvailableBlocks();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getPourcentUse(String str) {
        try {
            StatFs statFs = new StatFs(str);
            long availableBlocks = ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
            long blockCount = ((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize());
            if (blockCount > 0) {
                return 100 - ((availableBlocks * 100) / blockCount);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String buildTextFileSize(long j, Context context) {
        String str = " ";
        if (j > 1073741824) {
            double d = (double) j;
            Double.isNaN(d);
            double d2 = d / 1.073741824E9d;
            NumberFormat instance = NumberFormat.getInstance();
            instance.setMaximumFractionDigits(1);
            StringBuilder sb = new StringBuilder();
            sb.append(instance.format(d2));
            sb.append(str);
            sb.append(context.getString(R.string.gigabyteShort));
            return sb.toString();
        } else if (j > 1048576) {
            double d3 = (double) j;
            Double.isNaN(d3);
            double d4 = d3 / 1048576.0d;
            NumberFormat instance2 = NumberFormat.getInstance();
            instance2.setMaximumFractionDigits(1);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(instance2.format(d4));
            sb2.append(str);
            sb2.append(context.getString(R.string.megabyteShort));
            return sb2.toString();
        } else if (j > 1024) {
            long j2 = j / 1024;
            StringBuilder sb3 = new StringBuilder();
            sb3.append(String.valueOf(j2));
            sb3.append(str);
            sb3.append(context.getString(R.string.kilobyteShort));
            return sb3.toString();
        } else {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(String.valueOf(j));
            sb4.append(str);
            sb4.append(context.getString(R.string.byteShort));
            return sb4.toString();
        }
    }
}
