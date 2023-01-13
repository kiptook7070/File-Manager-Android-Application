package com.rbigsoft.unrar.model;


import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class Utilities_File_Worker {
    private static final String[] ARCHIVE_ARRAY = {"rar", "zip", "7z", "bz2", "bzip2", "tbz2", "tbz", "gz", "gzip", "tgz", "tar", "xz", "txz"};

    public static File_Related_Information getFileInfoFromPath(String str) {
        File_Related_Information file_Related_Information = new File_Related_Information();
        File file = new File(str);
        file_Related_Information.setFileName(file.getName());
        file_Related_Information.setFilePath(file.getAbsolutePath());
        file_Related_Information.setFileType(FileType.fileunknown);
        file_Related_Information.setFileSize(Integer.parseInt(String.valueOf(file.length() / 1024)));
        //file.lastModified();
        file_Related_Information.setFileDate(file.lastModified()+"");
        if (file.isDirectory()) {
            Log.e("isFileFolderConvert", file.getName()+"  sdf");
            file_Related_Information.setFolder(true);
            file_Related_Information.setFileType(FileType.folderEmpty);
            String[] list = file.list();
            if (list != null && list.length > 0) {
                file_Related_Information.setSubCount(list.length);
                file_Related_Information.setFileType(FileType.folderFull);
            }
        } else {
            file_Related_Information.setFileLength(file.length());
            if (isArchive(file)) {
                file_Related_Information.setFileType(FileType.filearchive);
            }
        }
        return file_Related_Information;
    }

    private static boolean isArchive(File file) {
        if (file != null) {
            String name = file.getName();
            if (name.isEmpty()) {
                String lowerCase = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
                for (String equals : ARCHIVE_ARRAY) {
                    if (lowerCase.equals(equals)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ArrayList<File_Related_Information> getInfoListFromPath(String str) {
        File[] listFiles;
        ArrayList<File_Related_Information> arrayList = new ArrayList<>();
        File file = new File(str);
        if (file.exists() && file.isDirectory() && file.canRead() && (listFiles = file.listFiles()) != null) {
            Arrays.sort(listFiles, new FileComparator());
            for (File path : listFiles) {
                arrayList.add(getFileInfoFromPath(path.getPath()));
            }
        }
        return arrayList;
    }

    private static class FileComparator implements Comparator<File> {
        private FileComparator() {
        }

        public int compare(File file, File file2) {
            int access$100 = Utilities_File_Worker.getFileScore(file2) - Utilities_File_Worker.getFileScore(file);
            return access$100 == 0 ? file.getName().compareToIgnoreCase(file2.getName()) : access$100;
        }
    }

    /* access modifiers changed from: private */
    public static int getFileScore(File file) {
        return (file.isHidden() ^ true) ? 1 : 0;
    }

    public static String getParentPath(String str) {
        return str.substring(0, str.lastIndexOf(File.separatorChar));
    }
}
