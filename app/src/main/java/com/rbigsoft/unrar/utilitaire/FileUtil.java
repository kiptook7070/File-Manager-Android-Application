package com.rbigsoft.unrar.utilitaire;

import com.rbigsoft.unrar.model.FileMimeCategorie;
import java.io.File;
import net.lingala.zip4j.util.InternalZipConstants;

public class FileUtil {
    public static FileMimeCategorie getMimeCategorie(String str) {
        String extension = getExtension(str);
        if (isImage(extension)) {
            return FileMimeCategorie.image;
        }
        if (isVideo(extension)) {
            return FileMimeCategorie.video;
        }
        if (isArchive(extension)) {
            return FileMimeCategorie.zip;
        }
        if (isDoc(extension)) {
            return FileMimeCategorie.doc;
        }
        if (isExcel(extension)) {
            return FileMimeCategorie.excel;
        }
        if (isHtml(extension)) {
            return FileMimeCategorie.html;
        }
        if (isMusic(extension)) {
            return FileMimeCategorie.music;
        }
        if (isPdf(extension)) {
            return FileMimeCategorie.pdf;
        }
        if (isPpt(extension)) {
            return FileMimeCategorie.ppt;
        }
        if (isRar(extension)) {
            return FileMimeCategorie.rar;
        }
        if (isTxt(extension)) {
            return FileMimeCategorie.txt;
        }
        if (isApk(extension)) {
            return FileMimeCategorie.apk;
        }
        if (is7zip(extension)) {
            return FileMimeCategorie.sevenZip;
        }
        return FileMimeCategorie.misc;
    }

    public static String getExtension(String str) {
        int lastIndexOf = str.lastIndexOf(".");
        return lastIndexOf != -1 ? str.substring(lastIndexOf + 1, str.length()) : "";
    }

    public static boolean isApk(String str) {
        return str.equalsIgnoreCase("apk");
    }

    public static boolean isImage(String str) {
        return str.equalsIgnoreCase("jpg") || str.equalsIgnoreCase("jpeg") || str.equalsIgnoreCase("png") || str.equalsIgnoreCase("gif") || str.equalsIgnoreCase("bmp");
    }

    public static boolean   isVideo(String str) {
        return str.equalsIgnoreCase("avi") || str.equalsIgnoreCase("mov") || str.equalsIgnoreCase("mp4");
    }

    public static boolean isDoc(String str) {
        return str.equalsIgnoreCase("doc") || str.equalsIgnoreCase("docx");
    }

    public static boolean isPdf(String str) {
        return str.equalsIgnoreCase("pdf");
    }

    public static boolean isPpt(String str) {
        return str.equalsIgnoreCase("ppt") || str.equalsIgnoreCase("pptx");
    }

    public static boolean isExcel(String str) {
        return str.equalsIgnoreCase("xls") || str.equalsIgnoreCase("xlsx");
    }

    public static boolean isTxt(String str) {
        return str.equalsIgnoreCase("txt") || str.equalsIgnoreCase("rtf");
    }

    public static boolean isArchive(String str) {
        return str.equalsIgnoreCase("zip");
    }

    public static boolean isRar(String str) {
        return str.equalsIgnoreCase("rar");
    }

    public static boolean is7zip(String str) {
        return str.equalsIgnoreCase("7z") || str.equalsIgnoreCase("tar") || str.equalsIgnoreCase("gz") || str.equalsIgnoreCase("cab") || str.equalsIgnoreCase("msi") || str.equalsIgnoreCase("xz") || str.equalsIgnoreCase("rpm") || str.equalsIgnoreCase("zip");
    }

    public static boolean isHtml(String str) {
        return str.equalsIgnoreCase("html") || str.equalsIgnoreCase("htm") || str.equalsIgnoreCase("xml");
    }

    public static boolean isMusic(String str) {
        return str.equalsIgnoreCase("mp3") || str.equalsIgnoreCase("ogg") || str.equalsIgnoreCase("wav") || str.equalsIgnoreCase("m4a");
    }

    public static boolean deleteDirectory(File file) {
        if (file.exists()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i].isDirectory()) {
                        deleteDirectory(listFiles[i]);
                    } else {
                        listFiles[i].delete();
                    }
                }
            }
        }
        return file.delete();
    }

    public static String realFilePathToDisplayPath(String str, boolean z) {
        File file = new File(str);
        if (z) {
            str = file.getParentFile().getPath();
        }
        if (str == null) {
            return str;
        }
        String str2 = InternalZipConstants.ZIP_FILE_SEPARATOR;
        return str.replaceFirst(str2, "").replaceAll(str2, " > ");
    }

    public static String getFolderName(String str) {
        return new File(str).getName();
    }
}
