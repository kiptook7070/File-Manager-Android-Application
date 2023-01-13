package com.rbigsoft.unrar.utilitaire;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class SdcardUtilitaire {
    public static File getSdcardPath() {
        List mountPointList = getMountPointList();
        if (mountPointList == null || mountPointList.size() <= 0) {
            return null;
        }
        return (File) mountPointList.get(0);
    }

    public static List getMountPointList() {
        ArrayList var0 = new ArrayList();
        String var1 = Environment.getExternalStorageState();

        try {
            if ("mounted".equals(var1)) {
                var0.add(Environment.getExternalStorageDirectory());
            }
        } catch (Exception var4) {
        }

        File var8;
        label64:
        {
            boolean var10001;
            String[] var2;
            try {
                var2 = getStorageDirectories();
            } catch (Exception var7) {
                var10001 = false;
                break label64;
            }

            if (var2 != null) {
                label67:
                {
                    try {
                        if (var2.length <= 0) {
                            break label67;
                        }
                    } catch (Exception var6) {
                        var10001 = false;
                        break label67;
                    }

                    int var3 = 0;

                    while (true) {
                        try {
                            if (var3 >= var2.length) {
                                break;
                            }

                            var8 = new File(var2[var3]);
                            if (var8.exists() && !pathExist(var8.getAbsolutePath(), var0)) {
                                var0.add(var8);
                            }
                        } catch (Exception var5) {
                            var10001 = false;
                            break;
                        }

                        ++var3;
                    }
                }
            }
        }

        Iterator var9 = listeAllaMntDir().iterator();

        while (var9.hasNext()) {
            var8 = (File) var9.next();
            if (!pathExist(var8.getAbsolutePath(), var0)) {
                var0.add(var8);
            }
        }

        return var0;
    }


    private static List<File> listeAllaMntDir() {
        ArrayList arrayList = new ArrayList();
        try {
            File file = new File("/mnt/");
            if (file.exists() && file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File file2 : listFiles) {
                        if (file2.isDirectory()) {
                            arrayList.add(file2);
                        }
                    }
                }
            }
        } catch (Exception unused) {
        }
        return arrayList;
    }

    private static boolean pathExist(String str, List<File> list) {
        if (str.equalsIgnoreCase("/mnt/secure") || str.equalsIgnoreCase("/mnt/asec") || str.equalsIgnoreCase("/mnt/obb") || str.equalsIgnoreCase("/mnt/.lfs")) {
            return true;
        }
        if (list != null) {
            for (File absolutePath : list) {
                if (absolutePath.getAbsolutePath().equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String[] getStorageDirectories() throws IOException {
        String[] arrstring;
        BufferedReader bufferedReader;
        block22:
        {
            String[] arrstring2;
            block21:
            {
                String[] arrstring3;
                block20:
                {
                    ArrayList<Object> arrayList;
                    String[] arrstring6;
                    String[] arrstring5;
                    String[] arrstring4;
                    block23:
                    {
                        arrstring6 = null;
                        arrstring5 = null;
                        arrstring3 = null;
                        arrstring2 = null;
                        arrstring = null;
                        arrstring4 = null;
                        arrayList = new ArrayList<Object>(Integer.parseInt("/proc/mounts"));
                        bufferedReader = new BufferedReader((Reader) ((Object) arrayList));
                        arrstring3 = arrstring4;
                        arrstring2 = arrstring6;
                        arrstring = arrstring5;
                        arrstring3 = arrstring4;
                        arrstring2 = arrstring6;
                        arrstring = arrstring5;
                        arrayList = new ArrayList<Object>();
                        do {
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            String string = bufferedReader.readLine();
                            if (string == null) break;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (!string.contains("vfat")) {
                                arrstring3 = arrstring4;
                                arrstring2 = arrstring6;
                                arrstring = arrstring5;
                                if (!string.contains("/mnt")) continue;
                            }
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            Object object = new StringTokenizer(string, " ");
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            ((StringTokenizer) object).nextToken();
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            object = ((StringTokenizer) object).nextToken();
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (((String) object).equals(Environment.getExternalStorageDirectory().getPath())) {
                                arrstring3 = arrstring4;
                                arrstring2 = arrstring6;
                                arrstring = arrstring5;
                                arrayList.add(object);
                                continue;
                            }
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (!string.contains("/dev/block/vold")) continue;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (string.contains("/mnt/secure")) continue;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (string.contains("/mnt/asec")) continue;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (string.contains("/mnt/obb")) continue;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (string.contains("/dev/mapper")) continue;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            if (string.contains("tmpfs")) continue;
                            arrstring3 = arrstring4;
                            arrstring2 = arrstring6;
                            arrstring = arrstring5;
                            arrayList.add(object);
                        } while (true);
                        break block23;
                    }
                    arrstring3 = arrstring4;
                    arrstring2 = arrstring6;
                    arrstring = arrstring5;
                    arrstring4 = new String[arrayList.size()];
                    int n = 0;
                    do {
                        arrstring3 = arrstring4;
                        arrstring2 = arrstring4;
                        arrstring = arrstring4;
                        if (n >= arrayList.size()) break;
                        arrstring3 = arrstring4;
                        arrstring2 = arrstring4;
                        arrstring = arrstring4;
                        arrstring4[n] = (String) arrayList.get(n);
                        ++n;
                    } while (true);
                    try {
                        bufferedReader.close();
                        return arrstring4;
                    } catch (IOException iOException) {
                        return arrstring4;
                    }
                }
            }
        }
    }


}
