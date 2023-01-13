package com.rbigsoft.sn.unzip.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rbigsoft.sn.unzip.BuildConfig;
import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;
import com.rbigsoft.unrar.adapter.AdapterFileMain;
import com.rbigsoft.unrar.dialog.ExtractDirDialog;
import com.rbigsoft.unrar.dialog.NewArchiveDialog;
import com.rbigsoft.unrar.model.FileMimeCategorie;
import com.rbigsoft.unrar.model.File_Related_Information;
import com.rbigsoft.unrar.model.Utilities_File_Worker;
import com.rbigsoft.unrar.model.document.Dossier;
import com.rbigsoft.unrar.model.document.Fichier;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.nativeinterface.Extract7zipAsync;
import com.rbigsoft.unrar.nativeinterface.ExtractRarAsync;
import com.rbigsoft.unrar.nativeinterface.ExtractZipAsync;
import com.rbigsoft.unrar.nativeinterface.List7zipAsync;
import com.rbigsoft.unrar.nativeinterface.ListRarAsync;
import com.rbigsoft.unrar.nativeinterface.ListZipAsync;
import com.rbigsoft.unrar.utilitaire.FileUtil;

import com.rbigsoft.unrar.utilitaire.VersionUtilitaire;

import net.lingala.zip4j.util.InternalZipConstants;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.view.View.GONE;
import static com.rbigsoft.sn.unzip.activity.MainUiActivity.CATEGORY_FILE_MAIN;

public class FileActivity extends AppCompatActivity implements View.OnClickListener, AdapterFileMain.ClickItemCallBack, TextWatcher {
    public static final String ACTION_EXTRA = "action_extra";
    public static final String ACTION_COMPRESS = "action_compress";
    public static final String TAG_EXTRA = "tag_extra";
    private ArrayList<File_Related_Information> bufferlist = new ArrayList<>();
    private ArrayList<File_Related_Information> arrSearch = new ArrayList<>();
    private AdapterFileMain adapterFileMain;
    private ProgressBar loading;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider_paths";
    private ExtractRarAsync asyncExtract;
    private Extract7zipAsync asyncExtract7zip;
    private ExtractZipAsync asyncExtractZip;
    private ListRarAsync asyncList;
    private List7zipAsync asyncList7zip;
    private ListZipAsync asyncListZip;
    private static final String[] ARCHIVE_ARRAY_Alldoc = {"ppt", "pptx", "doc", "docx", "msg", "txt", "wpd", "xls", "xlsx", "odf", "odt", "rtf", "pdf"};
    private static final String[] ARCHIVE_ARRAY_Audio = {"mp3", "acc", "au", "mid", "ra", "snd", "wma", "wav"};
    private static final String[] ARCHIVE_ARRAY_Compress = {"rar", "zip", "7z", "bz2", "bzip2", "tbz2", "tbz", "gz", "gzip", "tgz", "tar", "xz", "txz"};
    private Comparator<File> fileSortComparator = LastModifiedFileComparator.LASTMODIFIED_REVERSE;
    private static final String[] ARCHIVE_ARRAY_Image = {"bmp", "eps", "jpg", "pict", "png", "psd", "gif"};
    private static final String[] ARCHIVE_ARRAY_APK = {"apk"};
    private static final String[] ARCHIVE_ARRAY_Video = {"mp4", "avi", "mpg", "mov", "wmv"};
    private ImageView imgBack;
    private RecyclerView lvFile;
    private FloatingActionButton flZzip, flUnZip;
    private TextView tvTag;
    private String type = "";
    File root;
    private AppDatabaseSQL appDatabaseSQL;
    public List<FileSystemElement> elementsSelectionnes = new ArrayList();
    private TextView tvEmptyFile;
    private ImageView imgCancelSearch, imgSearch, imgSearchKey;
    private EditText edtSearch;
    private RelativeLayout rlSearchView;
    private SharedPreferences shareDeleteFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        initViews();
    }

    private void initViews() {
        shareDeleteFile = getSharedPreferences("deletefile", MODE_PRIVATE);
        imgCancelSearch = findViewById(R.id.img_cancel_search);
        imgCancelSearch.setOnClickListener(this);
        imgSearch = findViewById(R.id.img_search_file);
        imgSearch.setOnClickListener(this);
        imgSearchKey = findViewById(R.id.img_search_key);
        imgSearchKey.setOnClickListener(this);
        edtSearch = findViewById(R.id.edt_search_file);
        edtSearch.addTextChangedListener(this);
        rlSearchView = findViewById(R.id.rl_search_file);

        tvEmptyFile = findViewById(R.id.tv_empty_file);
        appDatabaseSQL = new AppDatabaseSQL(this);
        loading = findViewById(R.id.loading);
        root = Environment.getExternalStorageDirectory();
        tvTag = findViewById(R.id.tv_tag);
        imgBack = findViewById(R.id.img_back_file);
        imgBack.setOnClickListener(this);
        lvFile = findViewById(R.id.lv_file);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapterFileMain = new AdapterFileMain(this, bufferlist);

        adapterFileMain.setCallBack(this);
        lvFile.setLayoutManager(manager);
        lvFile.setAdapter(adapterFileMain);
        flUnZip = findViewById(R.id.flt_un_zip);
        flUnZip.setOnClickListener(this);
        flZzip = findViewById(R.id.flt_zip);
        flZzip.setOnClickListener(this);
        if (getIntent().getStringExtra(CATEGORY_FILE_MAIN) != null) {
            type = getIntent().getStringExtra(CATEGORY_FILE_MAIN);
            tvTag.setText(type);
        }
        if (!type.equals("")) {

            switch (type) {
                case "image":

                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
                    break;
                case "video":

                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
                    break;
                case "document":


                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
                    break;
                case "music":

                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
                    break;
                case "apk":
                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
                    break;
                case "extracted":
                    flUnZip.setVisibility(View.VISIBLE);

                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsyncSql().execute();
                    break;
                case "compress":
                    flUnZip.setVisibility(View.VISIBLE);
                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
                    break;

            }
        }
        adapterFileMain.notifyDataSetChanged();

        // init action listent finish
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EXTRA);
        filter.addAction(ACTION_COMPRESS);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void showDialogShowResult(int x) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_show_result);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView tvContent = dialog.findViewById(R.id.tv_content);
        if (x == 2) {
            tvContent.setText(getResources().getString(R.string.extract_show_resutl));
        }
        TextView tvOk = dialog.findViewById(R.id.tv_ok);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (x == 1) {
                    type = "compress";
                    flUnZip.setVisibility(View.VISIBLE);
                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsync().execute(new String[]{root.getAbsolutePath()});
                } else {
                    flUnZip.setVisibility(View.VISIBLE);
                    type = "extracted";
                    elementsSelectionnes.clear();
                    bufferlist.clear();
                    new FileAsyncSql().execute();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            switch (intent.getAction()) {
                case ACTION_COMPRESS:
                    showDialogShowResult(1);
                    flUnZip.setVisibility(View.VISIBLE);
                    break;
                case ACTION_EXTRA:

                    showDialogShowResult(2);
                    flUnZip.setVisibility(View.VISIBLE);
                    break;

            }
        }
    };

    public void updateLisview() {
        for (int i = 0; i < bufferlist.size(); i++) {
            bufferlist.get(i).setSelected(false);
        }
        adapterFileMain.notifyDataSetChanged();
    }

    public void executeFichier(File file) {
        if (file.exists()) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtil.getExtension(file.getName()).toLowerCase());
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uriForFile = FileProvider.getUriForFile(this, VersionUtilitaire.getProviderName(this), file);
                intent.addFlags(1);
                intent.setDataAndType(uriForFile, mimeTypeFromExtension);
                startActivity(intent);
            } catch (Exception unused) {
                Log.e("errorOPenFile", unused.getMessage());
                Toast.makeText(this, getText(R.string.aucun_app_pour_fichier), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "File don't exist on the filesystem, remove by another browser ?", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickOpenArchive() {
        if (this.elementsSelectionnes.size() <= 0) {
            Toast.makeText(this, getString(R.string.au_moins_un_fichier), Toast.LENGTH_LONG).show();
        } else if (this.elementsSelectionnes.size() == 1) {
            listerFichier();

        } else {
            Toast.makeText(this, getString(R.string.seulement_un_fichier), Toast.LENGTH_LONG).show();

        }
    }

    private void listerRar(FileSystemElement fileSystemElement) {
        ListRarAsync listRarAsync = this.asyncList;
        if (listRarAsync == null) {
            this.asyncList = new ListRarAsync(this, fileSystemElement);
            this.asyncList.execute(new String[]{fileSystemElement.getChemin()});
        } else if (listRarAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncList = new ListRarAsync(this, fileSystemElement);
            this.asyncList.execute(new String[]{fileSystemElement.getChemin()});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD LIST RUNNING");
        }
    }

    private void listerZip(FileSystemElement fileSystemElement) {
        ListZipAsync listZipAsync = this.asyncListZip;
        if (listZipAsync == null) {
            this.asyncListZip = new ListZipAsync(this, fileSystemElement);
            this.asyncListZip.execute(new String[]{fileSystemElement.getChemin()});
        } else if (listZipAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncListZip = new ListZipAsync(this, fileSystemElement);
            this.asyncListZip.execute(new String[]{fileSystemElement.getChemin()});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD LIST RUNNING");
        }
    }

    private void lister7Zip(FileSystemElement fileSystemElement) {
        List7zipAsync list7zipAsync = this.asyncList7zip;
        if (list7zipAsync == null) {
            this.asyncList7zip = new List7zipAsync(this, fileSystemElement);
            this.asyncList7zip.execute(new String[]{fileSystemElement.getChemin()});
        } else if (list7zipAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncList7zip = new List7zipAsync(this, fileSystemElement);
            this.asyncList7zip.execute(new String[]{fileSystemElement.getChemin()});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD LIST RUNNING");
        }
    }

    public void listerFichier() {
        FileSystemElement fileSystemElement = (FileSystemElement) this.elementsSelectionnes.get(0);
        FileMimeCategorie mimeCategorie = FileUtil.getMimeCategorie(fileSystemElement.getChemin());
        if (mimeCategorie == FileMimeCategorie.rar) {
            listerRar(fileSystemElement);
        } else if (mimeCategorie == FileMimeCategorie.zip) {
            listerZip(fileSystemElement);
        } else if (mimeCategorie == FileMimeCategorie.sevenZip) {
            lister7Zip(fileSystemElement);
        }

        elementsSelectionnes.remove(0);
    }

    public void onClickValidCompress() {
        if (this.elementsSelectionnes.size() > 0) {
            String[] strings = new String[elementsSelectionnes.size()];
            for (int i = 0; i < elementsSelectionnes.size(); i++) {
                strings[i] = String.valueOf(elementsSelectionnes.get(i).getChemin());
            }
            String xx = "/" + elementsSelectionnes.get(0).getNom();
            String pathSave = elementsSelectionnes.get(0).getChemin().replace(xx, "");
            Log.e("pathSave", pathSave + "     s");
            final NewArchiveDialog newArchiveDialog = new NewArchiveDialog(this);
            newArchiveDialog.setElementsSelectionnes(this.elementsSelectionnes);
            newArchiveDialog.setCheminDest(pathSave);
            newArchiveDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    if (newArchiveDialog.isCompress()) {
                        updateLisview();
                    }
                }
            });
            new ComputeSizeAsyncTaskStart(this, newArchiveDialog).execute(new String[0]);

            return;
        }
        Toast.makeText(this, getString(R.string.au_moins_un_fichier), Toast.LENGTH_LONG).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }


    @Override
    public void afterTextChanged(Editable s) {
        if (!edtSearch.getText().toString().isEmpty()) {
            filterFileSearch(edtSearch.getText().toString().trim());
        } else {
            bufferlist.clear();
            bufferlist.addAll(arrSearch);
            adapterFileMain.notifyDataSetChanged();
        }
    }

    private void filterFileSearch(String keySearch) {
        bufferlist.clear();
        bufferlist.addAll(arrSearch);
        ArrayList<File_Related_Information> arrFilter = new ArrayList<>();
        arrFilter.clear();
        for (int i = 0; i < bufferlist.size(); i++) {
            if (bufferlist.get(i).getFileName().contains(keySearch) || bufferlist.get(i).getFilePath().contains(keySearch)) {
                arrFilter.add(bufferlist.get(i));
            }
        }
        bufferlist.clear();
        bufferlist.addAll(arrFilter);
        adapterFileMain.notifyDataSetChanged();
    }

    class ComputeSizeAsyncTaskStart extends AsyncTask<String, ProgressDialog, Boolean> {
        private NewArchiveDialog archiveDiag;
        private Context context;
        private ProgressDialog progressDiag;


        public ComputeSizeAsyncTaskStart(Context context2, NewArchiveDialog newArchiveDialog) {
            this.context = context2;
            this.archiveDiag = newArchiveDialog;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            this.progressDiag = new ProgressDialog(this.context);
            this.progressDiag.setProgressStyle(0);
            this.progressDiag.setMessage("Please wait...");
            this.progressDiag.show();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean bool) {
            this.progressDiag.dismiss();
            this.archiveDiag.show();
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(String... strArr) {
            this.archiveDiag.computeTotalSize();
            return Boolean.valueOf(true);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel_search:
                hideKeyboard(FileActivity.this);
                edtSearch.setText("");
                rlSearchView.setVisibility(GONE);
                break;
            case R.id.img_search_file:
                arrSearch.clear();
                arrSearch.addAll(bufferlist);
                rlSearchView.setVisibility(View.VISIBLE);
                edtSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
                break;
            case R.id.img_search_key:
                if (!edtSearch.getText().toString().isEmpty()) {
                    filterFileSearch(edtSearch.getText().toString().trim());
                } else {
                    Toast.makeText(this, "" + getResources().getString(R.string.empty_input), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.flt_un_zip:
                onClickExtraire();
                break;
            case R.id.flt_zip:
                onClickValidCompress();
                break;
            case R.id.img_back_file:
                onBackPressed();
                break;
        }
    }

    public void onClickExtraire() {
        if (this.elementsSelectionnes.size() == 1) {
            final FileSystemElement fileSystemElement = (FileSystemElement) this.elementsSelectionnes.get(0);
            String xx = "/" + fileSystemElement.getNom();
            String pathFile = fileSystemElement.getChemin().replace(xx, "");
            final ExtractDirDialog extractDirDialog = new ExtractDirDialog(this, fileSystemElement.getNom(), pathFile);
            extractDirDialog.show();
            extractDirDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    if (!extractDirDialog.isCancel()) {

                        extractArchive(fileSystemElement, extractDirDialog.isExtractToFolder(), extractDirDialog.getFolderName());
                    }
                }
            });
        } else if (this.elementsSelectionnes.size() == 0) {
            Toast.makeText(this, getString(R.string.au_moins_un_fichier), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.seulement_un_fichier), Toast.LENGTH_LONG).show();
        }
    }

    private String createTargetFolder(String str, boolean z, String str2) {
        if (!z) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(InternalZipConstants.ZIP_FILE_SEPARATOR);
        sb.append(str2);
        String sb2 = sb.toString();
        File file = new File(sb2);
        return (file.exists() || file.mkdir()) ? sb2 : str;
    }

    /* access modifiers changed from: private */
    public void extractArchive(FileSystemElement fileSystemElement, boolean z, String str) {
        FileMimeCategorie mimeCategorie = FileUtil.getMimeCategorie(fileSystemElement.getChemin());
        String xx = "/" + fileSystemElement.getNom();
        String pathFile = fileSystemElement.getChemin().replace(xx, "");
        String createTargetFolder = createTargetFolder(pathFile, z, str);

        if (mimeCategorie == FileMimeCategorie.rar) {
            extraireRar(fileSystemElement, createTargetFolder);
        } else if (mimeCategorie == FileMimeCategorie.zip) {
            extraireZip(fileSystemElement, createTargetFolder);
        } else if (mimeCategorie == FileMimeCategorie.sevenZip) {
            extraire7zip(fileSystemElement, createTargetFolder);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (bufferlist.size() != 0) {
            new FileAsync().execute(new String[]{this.root.getAbsolutePath()});
        }
    }


    public void extraireRar(FileSystemElement fileSystemElement, String str) {
        ExtractRarAsync extractRarAsync = this.asyncExtract;
        if (extractRarAsync == null) {
            this.asyncExtract = new ExtractRarAsync(this);
            this.asyncExtract.execute(new String[]{fileSystemElement.getChemin(), str});
        } else if (extractRarAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncExtract = new ExtractRarAsync(this);
            this.asyncExtract.execute(new String[]{fileSystemElement.getChemin(), str});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }

    public void extraire7zip(FileSystemElement fileSystemElement, String str) {
        Extract7zipAsync extract7zipAsync = this.asyncExtract7zip;
        if (extract7zipAsync == null) {
            this.asyncExtract7zip = new Extract7zipAsync(this);
            this.asyncExtract7zip.execute(new String[]{fileSystemElement.getChemin(), str});
        } else if (extract7zipAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncExtract7zip = new Extract7zipAsync(this);
            this.asyncExtract7zip.execute(new String[]{fileSystemElement.getChemin(), str});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }

    public void extraireZip(FileSystemElement fileSystemElement, String str) {
        ExtractZipAsync extractZipAsync = this.asyncExtractZip;
        if (extractZipAsync == null) {
            this.asyncExtractZip = new ExtractZipAsync(this);
            this.asyncExtractZip.execute(new String[]{fileSystemElement.getChemin(), str});
        } else if (extractZipAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncExtractZip = new ExtractZipAsync(this);
            this.asyncExtractZip.execute(new String[]{fileSystemElement.getChemin(), str});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }


    @Override
    public void clickItem(int position) {
        Log.e("sdfadfeef", position + "                 " + bufferlist.size());
        File file = new File(bufferlist.get(position).getFilePath());
        if (file.isDirectory()) {
        } else {
            if (type.equals("compress")) {
                if (elementsSelectionnes.size() == 0) {
                    FileSystemElement fileSystemElement = new FileSystemElement(bufferlist.get(position).getFilePath(), bufferlist.get(position).getFileName());
                    elementsSelectionnes.add(fileSystemElement);
                    onClickOpenArchive();
                } else {
                    Toast.makeText(this, getString(R.string.seulement_un_fichier), Toast.LENGTH_LONG).show();
                }
            } else {

                executeFichier(new File(bufferlist.get(position).getFilePath()));
            }
        }
    }

    @Override
    public void checkItem(int position, boolean check) {
        if (check == true) {
            FileSystemElement fileSystemElement = new FileSystemElement(bufferlist.get(position).getFilePath(), bufferlist.get(position).getFileName());
            elementsSelectionnes.add(fileSystemElement);
            bufferlist.get(position).setSelected(true);
        } else {
            for (int i = 0; i < elementsSelectionnes.size(); i++) {
                if (elementsSelectionnes.get(i).getChemin().equals(bufferlist.get(position).getFilePath())) {
                    elementsSelectionnes.remove(i);
                }
            }
            bufferlist.get(position).setSelected(false);

        }
        Log.e("sdfefdfsd", elementsSelectionnes.size() + "");
    }

    @Override
    public void logClickItem(int position, View view) {
        try {
            if (new File(bufferlist.get(position).getFilePath()).isDirectory() == false) {
                showPopupMenuOptionFile(view, position);
            }
        } catch (Exception e) {

        }
    }

    @SuppressLint({"StaticFieldLeak"})
    class FileAsyncSql extends AsyncTask<String, String, String> {
        FileAsyncSql() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public String doInBackground(String... strArr) {
            bufferlist.clear();
            ArrayList<String> arrayList = appDatabaseSQL.getAllHistory();
            for (int i = 0; i < arrayList.size(); i++) {
                Log.e("asdffe", arrayList.get(i));
                if (!arrayList.get(i).equals("")) {
                    bufferlist.addAll(getListFilesSql(new File(arrayList.get(i))));
                }
            }
            return null;
        }
        public void onPostExecute(String str) {
            loading.setVisibility(GONE);
            if (bufferlist.size() == 0 && appDatabaseSQL.getAllHistory().size() != 0) {
                new FileAsyncSql().execute();
            }
            if (appDatabaseSQL.getAllHistory().size() == 0) {
                tvEmptyFile.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < bufferlist.size(); i++) {
                for (int j = i + 1; j < bufferlist.size(); j++) {
                    if (Long.parseLong(bufferlist.get(i).getFileDate()) < Long.parseLong(bufferlist.get(j).getFileDate())) {
                        File_Related_Information itemTwo;
                        itemTwo = bufferlist.get(i);
                        bufferlist.set(i, bufferlist.get(j));
                        bufferlist.set(j, itemTwo);
                    }
                }
            }

            adapterFileMain.notifyDataSetChanged();
        }
    }

    @SuppressLint({"StaticFieldLeak"})
    class FileAsync extends AsyncTask<String, String, String> {
        FileAsync() {
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            bufferlist.clear();
            adapterFileMain.notifyDataSetChanged();
            loading.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        /* access modifiers changed from: protected */
        public String doInBackground(String... strArr) {
            bufferlist.addAll(getListFiles(root));
            return null;
        }

        public void onPostExecute(String str) {
            loading.setVisibility(GONE);

            for (int i = 0; i < bufferlist.size(); i++) {
                for (int j = i + 1; j < bufferlist.size(); j++) {
                    if (Long.parseLong(bufferlist.get(i).getFileDate()) < Long.parseLong(bufferlist.get(j).getFileDate())) {
                        File_Related_Information itemTwo;
                        itemTwo = bufferlist.get(i);
                        bufferlist.set(i, bufferlist.get(j));
                        bufferlist.set(j, itemTwo);
                    }
                }
            }
            adapterFileMain.notifyDataSetChanged();
        }
    }


    public ArrayList<File_Related_Information> getListFilesSql(File file) {
        ArrayList<File_Related_Information> arrayList = new ArrayList<>();
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File file2 : listFiles) {
                if (file2 != null) {
                    if (file2.isDirectory()) {
                        arrayList.addAll(getListFilesSql(file2));
                    } else {
                        arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                    }
                }
            }
        }

        return arrayList;
    }

    public void showPopupMenuOptionFile(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_option_file_custom, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete_file:
                        try {
                            File file = new File(bufferlist.get(position).getFilePath());
                            file.delete();
                            bufferlist.remove(position);
                            adapterFileMain.notifyDataSetChanged();
                        } catch (Exception e) {

                        }
                        break;
                    case R.id.menu_share_file:
                        shareFileAction(position);
                        break;

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void shareFileAction(int position) {

        File file = new File(bufferlist.get(position).getFilePath());
        if (file.exists()) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtil.getExtension(file.getName()).toLowerCase());
            Log.e("misdfefs", mimeTypeFromExtension + "");
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                Uri photoURI = FileProvider.getUriForFile(this, AUTHORITY, file);
                intent.putExtra(Intent.EXTRA_STREAM, photoURI);
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            intent.setType(mimeTypeFromExtension);
            startActivity(Intent.createChooser(intent, "share"));
        }
    }

    public ArrayList<File_Related_Information> getListFiles(File file) {
        ArrayList<File_Related_Information> arrayList = new ArrayList<>();
        File[] listFiles = file.listFiles();
        if (listFiles != null) {

            for (File file2 : listFiles) {
                if (file2 != null) {
                    if (file2.isDirectory()) {
                        arrayList.addAll(getListFiles(file2));
                    } else {
                        if (type.equals("video")) {
                            if (file2.getName().endsWith(ARCHIVE_ARRAY_Video[0])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Video[1])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Video[2])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Video[3])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Video[4])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            }
                        } else if (type.equals("apk")) {
                            if (file2.getName().endsWith(ARCHIVE_ARRAY_APK[0])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            }
                        } else if (type.equals("document")) {
                            if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[0])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[1])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[2])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[3])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[4])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[5])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[6])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[7])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[8])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[9])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[10])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[11])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Alldoc[12])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            }
                        } else if (type.equals("image")) {
                            if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[0])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[1])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[2])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[3])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[4])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[5])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Image[6])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            }
                        } else if (type.equals("compress")) {
                            if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[0])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[1])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[2])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[3])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[4])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[5])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[6])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[7])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[8])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[9])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[10])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[11])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Compress[12])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            }
                        } else if (type.equals("music")) {
                            if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[0])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[1])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[2])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[3])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[4])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[5])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[6])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            } else if (file2.getName().endsWith(ARCHIVE_ARRAY_Audio[7])) {
                                arrayList.add(Utilities_File_Worker.getFileInfoFromPath(file2.getPath()));
                            }
                        }
                    }
                }
            }
        }

        return arrayList;
    }


}
