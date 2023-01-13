package com.rbigsoft.unrar.utilitaire;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class StorageOptions {
    public static int count = 0;
    public static String[] labels;
    private static ArrayList<String> mMounts = new ArrayList<>();
    private static ArrayList<String> mVold = new ArrayList<>();
    public static String[] paths;

    public static void determineStorageOptions() {
        readMountsFile();
        readVoldFile();
        compareMountsWithVold();
        testAndCleanMountsList();
        setProperties();
    }

    private static void readMountsFile() {
        String str = "/mnt/sdcard";
        mMounts.add(str);
        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                if (nextLine.startsWith("/dev/block/vold/")) {
                    String str2 = nextLine.split(" ")[1];
                    if (!str2.equals(str)) {
                        mMounts.add(str2);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readVoldFile() {
        String str = ":";
        String str2 = "/mnt/sdcard";
        mVold.add(str2);
        try {
            Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
            while (scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                if (nextLine.startsWith("dev_mount")) {
                    String str3 = nextLine.split(" ")[2];
                    if (str3.contains(str)) {
                        str3 = str3.substring(0, str3.indexOf(str));
                    }
                    if (!str3.equals(str2)) {
                        mVold.add(str3);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void compareMountsWithVold() {
        int i = 0;
        while (i < mMounts.size()) {
            if (!mVold.contains((String) mMounts.get(i))) {
                int i2 = i - 1;
                mMounts.remove(i);
                i = i2;
            }
            i++;
        }
        mVold.clear();
    }

    private static void testAndCleanMountsList() {
        int i = 0;
        while (i < mMounts.size()) {
            File file = new File((String) mMounts.get(i));
            if (!file.exists() || !file.isDirectory() || !file.canWrite()) {
                int i2 = i - 1;
                mMounts.remove(i);
                i = i2;
            }
            i++;
        }
    }

    private static void setProperties() {
        paths = new String[mMounts.size()];
        mMounts.toArray(paths);
        mMounts.clear();
    }
}
