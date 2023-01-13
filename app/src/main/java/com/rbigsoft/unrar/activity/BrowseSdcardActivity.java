package com.rbigsoft.unrar.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.sn.unzip.activity.FileActivity;
import com.rbigsoft.unrar.adapter.DocumentImportAdapter;
import com.rbigsoft.unrar.dialog.ExtractDirDialog;
import com.rbigsoft.unrar.dialog.NewArchiveDialog;
import com.rbigsoft.unrar.model.FileMimeCategorie;
import com.rbigsoft.unrar.model.document.Dossier;
import com.rbigsoft.unrar.model.document.Fichier;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.nativeinterface.Extract7zipAsync;
import com.rbigsoft.unrar.nativeinterface.ExtractRarAsync;
import com.rbigsoft.unrar.nativeinterface.ExtractZipAsync;
import com.rbigsoft.unrar.nativeinterface.List7zipAsync;
import com.rbigsoft.unrar.nativeinterface.ListRarAsync;
import com.rbigsoft.unrar.nativeinterface.ListZipAsync;
import com.rbigsoft.unrar.utilitaire.AvailableSpaceHandler;
import com.rbigsoft.unrar.utilitaire.DialogUtil;
import com.rbigsoft.unrar.utilitaire.FileUtil;
import com.rbigsoft.unrar.utilitaire.SdcardUtilitaire;
import com.rbigsoft.unrar.utilitaire.VersionUtilitaire;

import net.lingala.zip4j.util.InternalZipConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.comparator.ExtensionFileComparator;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

public class BrowseSdcardActivity extends AppCompatActivity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 10;
    private boolean askToChangeDirOnNextRefresh = false;
    private ExtractRarAsync asyncExtract;
    private Extract7zipAsync asyncExtract7zip;
    private ExtractZipAsync asyncExtractZip;
    private ListRarAsync asyncList;
    private List7zipAsync asyncList7zip;
    private ListZipAsync asyncListZip;
    private BottomNavigationView bottomNavigationView;
    private TextView cheminRepCourant;
    private String currentMountPoint;
    public Uri data;
    public File dataPath;
    public DocumentImportAdapter docAdapter;
    public Dossier dossierCourant;
    public List<FileSystemElement> elementsSelectionnes = new ArrayList();
    public boolean fabExpanded = false;
    private LinearLayout llExtract, llCompress;
    private Comparator<File> fileSortComparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
    private String goToNextRefreshfolderName = null;
    /* access modifiers changed from: private */
    public boolean selectFileToZipState = false;
    /* access modifiers changed from: private */
    public boolean threadStartFinish = false;

    private TextView tvFilePath;
    private ImageView imgBackFolder;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int i, long j) {

        int size = this.dossierCourant.getContenu().size();
        if (i == -1 || i >= size) {
            Toast.makeText(this, "Unable to select the element, try to go back to the parent folder and re-execute.", Toast.LENGTH_LONG).show();
            return;
        }
        FileSystemElement fileSystemElement = dossierCourant.getContenu().get(i);
        if (fileSystemElement instanceof Dossier) {
            new RefreshAsyncTask(this).execute(new Dossier[]{(Dossier) fileSystemElement});
            tvFilePath.setText("/" + fileSystemElement.getChemin());
        } else if (FileUtil.getMimeCategorie(fileSystemElement.getNom()) == FileMimeCategorie.rar || FileUtil.getMimeCategorie(fileSystemElement.getNom()) == FileMimeCategorie.zip || FileUtil.getMimeCategorie(fileSystemElement.getNom()) == FileMimeCategorie.sevenZip) {
            Log.e("deohieu", fileSystemElement.getParent().getChemin() + "");
            elementsSelectionnes.add(fileSystemElement);
            onClickOpenArchive(view);
        } else {
            Log.e("deohieu", fileSystemElement.getParent().getChemin() + "");
            executeFichier(new File(fileSystemElement.getChemin()));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_compress:
                onClickValidCompress();
                break;
            case R.id.ll_extract:
                onClickExtraire();
                break;
        }
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

    class DeleteAsyncTask extends AsyncTask<File, ProgressDialog, Boolean> {
        private Context context;
        private ProgressDialog progressDiag;

        public DeleteAsyncTask(Context context2) {
            this.context = context2;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            this.progressDiag = new ProgressDialog(this.context);
            this.progressDiag.setMessage("Please wait...");
            this.progressDiag.show();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean bool) {
            this.progressDiag.dismiss();
            BrowseSdcardActivity.this.refresh();
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(File... fileArr) {
            File file = fileArr[0];
            if (file.isDirectory()) {
                FileUtil.deleteDirectory(file);
            } else {
                file.delete();
            }
            return Boolean.valueOf(true);
        }
    }

    class RefreshAsyncTask extends AsyncTask<Dossier, ProgressDialog, Boolean> {
        private Activity context;
        private ProgressDialog progressDiag;

        public RefreshAsyncTask(Activity activity) {
            this.context = activity;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            this.progressDiag = new ProgressDialog(this.context);
            this.progressDiag.setMessage("Please wait...");
            if (!this.context.isFinishing()) {
                this.progressDiag.show();
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean bool) {
            if (this.progressDiag.isShowing()) {
                try {
                    this.progressDiag.dismiss();
                } catch (IllegalArgumentException unused) {
                }
            }
            docAdapter.setDossierCourant(BrowseSdcardActivity.this.dossierCourant);
            listeChange();
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(Dossier... dossierArr) {
            BrowseSdcardActivity.this.setDossierCourant(dossierArr[0].getChemin());
            Log.e("adfadfadf", dossierArr[0].getChemin());
            return Boolean.valueOf(true);
        }
    }

    class RefreshAsyncTaskStart extends AsyncTask<String, ProgressDialog, Boolean> {
        private Activity context;
        private ProgressDialog progressDiag;

        public RefreshAsyncTaskStart(Activity activity) {
            this.context = activity;
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            this.progressDiag = new ProgressDialog(this.context);
            this.progressDiag.setMessage("Please wait...");
            if (!this.context.isFinishing()) {
                this.progressDiag.show();
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean bool) {
            if (this.progressDiag.isShowing()) {
                try {
                    this.progressDiag.dismiss();
                } catch (IllegalArgumentException unused) {
                }
            }
            BrowseSdcardActivity.this.initialiseListView();
            if (BrowseSdcardActivity.this.data != null && BrowseSdcardActivity.this.dataPath != null && BrowseSdcardActivity.this.dataPath.exists() && BrowseSdcardActivity.this.dataPath.isFile()) {
                BrowseSdcardActivity.this.elementsSelectionnes.add(new Fichier(BrowseSdcardActivity.this.dataPath.getName(), BrowseSdcardActivity.this.dossierCourant));
                BrowseSdcardActivity.this.listerFichier();
                BrowseSdcardActivity.this.elementsSelectionnes.clear();
                for (int i = 0; i < elementsSelectionnes.size(); i++) {
                    Log.e("DetailArrGetFile", elementsSelectionnes.get(i).getChemin() + "     " + elementsSelectionnes.get(i).getNom() + "       " + elementsSelectionnes.get(i).getNomCrypte() + "            " + elementsSelectionnes.get(i).getId() + "          ");

                }
            } else {
                Log.e("DetailArrGetFile", "null");
            }
            Log.e("DetailArrGetFile", "finish");
            BrowseSdcardActivity.this.threadStartFinish = true;
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(String... strArr) {
            BrowseSdcardActivity.this.setDossierCourant(strArr[0]);
            return Boolean.valueOf(true);
        }
    }

    public void onAdsClick(View view) {
    }

    protected void onCreate(Bundle var1) {
        super.onCreate(var1);
        if (Build.VERSION.SDK_INT >= 23) {
            this.checkPermissions();
        }

        this.data = this.getIntent().getData();

        this.setContentView(R.layout.import_fichier);
        imgBackFolder = findViewById(R.id.img_back_folder);
        imgBackFolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvFilePath = findViewById(R.id.tv_path_file_main);
        llCompress = findViewById(R.id.ll_compress);
        llCompress.setOnClickListener(this);
        llExtract = findViewById(R.id.ll_extract);
        llExtract.setOnClickListener(this);
        try {
            ExtractRarAsync.browser = this;
        } catch (UnsatisfiedLinkError var4) {
            try {
                ExtractRarAsync.browser = this;
            } catch (UnsatisfiedLinkError var3) {
                ExtractRarAsync.browser = this;
            }
        }

        File var2 = SdcardUtilitaire.getSdcardPath();
        if (var2 != null) {
            Uri var5 = this.data;
            if (var5 != null && var5.getPath() != null) {
                this.dataPath = new File(this.data.getPath());
                if (this.dataPath.exists()) {
                    File var6 = this.dataPath.getParentFile();
                    if (this.dataPath.isDirectory()) {
                        var6 = this.dataPath;
                    }

                    if (var6.exists()) {
                        this.currentMountPoint = var6.getAbsolutePath();
                    }
                } else {
                    this.currentMountPoint = var2.getAbsolutePath();
                    StringBuilder var7 = new StringBuilder();
                    var7.append("Unable to locate the file:");
                    var7.append(this.dataPath.toString());
                    Toast.makeText(this, var7.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                this.currentMountPoint = var2.getAbsolutePath();
            }

            if (this.currentMountPoint == null) {
                this.currentMountPoint = var2.getAbsolutePath();
            }
            new RefreshAsyncTaskStart(this).execute(new String[]{this.currentMountPoint});

        } else {
            Toast.makeText(this, this.getString(R.string.aucune_sdcard), Toast.LENGTH_LONG).show();
            this.cheminRepCourant = (TextView) this.findViewById(R.id.textViewCheminRepCourant);
            this.cheminRepCourant.setText(this.getString(R.string.aucune_sdcard));

            ((TextView) this.findViewById(R.id.textViewFreeSpace)).setText("");
            this.initializeMenu();
        }

    }

    private void selectExtractFileBottomMenu() {
        BottomNavigationView bottomNavigationView2 = this.bottomNavigationView;
        if (bottomNavigationView2 != null) {
            bottomNavigationView2.setSelectedItemId(R.id.navExtractFiles);
        }
    }


    private void checkPermissions() {
        String str = "android.permission.READ_EXTERNAL_STORAGE";
        if (ContextCompat.checkSelfPermission(this, str) != 0 && !ActivityCompat.shouldShowRequestPermissionRationale(this, str)) {
            String str2 = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, str2)) {
                ActivityCompat.requestPermissions(this, new String[]{str, str2}, 10);
            }
        }
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 10 && iArr.length > 0) {
            int i2 = iArr[0];
        }
    }

    private void initializeMenu() {
        registerForContextMenu(this.cheminRepCourant);
        registerForContextMenu(findViewById(R.id.listViewImportFichier));
    }

    /* access modifiers changed from: private */
    public void initialiseListView() {
        ListView listView = findViewById(R.id.listViewImportFichier);
        this.docAdapter = new DocumentImportAdapter(this.dossierCourant, this, this.elementsSelectionnes);
        listView.setAdapter(this.docAdapter);


        this.cheminRepCourant = (TextView) findViewById(R.id.textViewCheminRepCourant);
        String chemin = this.dossierCourant.getChemin();
        this.cheminRepCourant.setText(FileUtil.realFilePathToDisplayPath(chemin, false));

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        initializeMenu();
        updateStorageSpace();
    }

    private void updateStorageSpace() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarSpace);
        TextView textView = (TextView) findViewById(R.id.textViewFreeSpace);
        long externalAvailableSpaceInBytes = AvailableSpaceHandler.getExternalAvailableSpaceInBytes(this.currentMountPoint);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(AvailableSpaceHandler.getPourcentUse(this.currentMountPoint));
        sb2.append("% ");
        sb.append(String.valueOf(sb2.toString()));
        sb.append(getString(R.string.utilise));
        sb.append(" (");
        sb.append(AvailableSpaceHandler.buildTextFileSize(externalAvailableSpaceInBytes, this));
        sb.append(" ");
        sb.append(getString(R.string.libre));
        sb.append(")");
        textView.setText(sb.toString());
        progressBar.setProgress((int) AvailableSpaceHandler.getPourcentUse(this.currentMountPoint));
    }

    /* access modifiers changed from: private */
    public void setDossierCourant(String str) {
        File file = new File(str);
        Dossier dossier = this.dossierCourant;
        if (dossier != null && dossier.getChemin().equals(str)) {
            this.dossierCourant = new Dossier(file.getName(), this.dossierCourant.getParent());
        } else if (str.equals(SdcardUtilitaire.getSdcardPath().getAbsolutePath())) {
            this.dossierCourant = new Dossier(file.getName(), this.dossierCourant);
        } else {
            try {
                this.dossierCourant = createDossierCourantFromChildFolder(file);
            } catch (Exception unused) {
                this.dossierCourant = new Dossier(file.getName(), this.dossierCourant);
            }
        }
        this.dossierCourant.setChemin(str);
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            Arrays.sort(listFiles, this.fileSortComparator);
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isDirectory()) {
                    this.dossierCourant.ajouterElement(new Dossier(listFiles[i].getName(), this.dossierCourant));
                } else {
                    this.dossierCourant.ajouterElement(new Fichier(listFiles[i].getName(), this.dossierCourant));
                }
            }
        }
        Log.e("detailUnNkow", dossierCourant.getChemin() + "     ");

    }

    private Dossier createDossierCourantFromChildFolder(File file) {
        String absolutePath = file.getAbsolutePath();
        File sdcardPath = SdcardUtilitaire.getSdcardPath();
        String absolutePath2 = sdcardPath.getAbsolutePath();
        String substring = absolutePath.substring(absolutePath2.length(), absolutePath.length());
        String substring2 = substring.substring(1, substring.length());
        String str = InternalZipConstants.ZIP_FILE_SEPARATOR;
        String[] split = substring2.split(str);
        Dossier dossier = new Dossier(sdcardPath.getName(), null);
        dossier.setChemin(absolutePath2);
        int length = split.length;
        int i = 0;
        while (i < length) {
            String str2 = split[i];
            Dossier dossier2 = new Dossier(str2, dossier);
            StringBuilder sb = new StringBuilder();
            sb.append(dossier.getChemin());
            sb.append(str);
            sb.append(str2);
            dossier2.setChemin(sb.toString());
            dossier.ajouterElement(dossier2);
            i++;
            dossier = dossier2;
        }
        return dossier;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (this.threadStartFinish) {
            refresh();
        }
    }

    /* access modifiers changed from: private */
    public void listeChange() {
        this.docAdapter.notifyDataSetChanged();
        boolean z = false;
        this.cheminRepCourant.setText(FileUtil.realFilePathToDisplayPath(this.dossierCourant.getChemin(), false));

        String chemin = this.dossierCourant.getChemin();
        if (this.dossierCourant.getParent() != null) {
            z = true;
        }
        if (this.dossierCourant.getParent() == null) {
          //  getSupportActionBar().setTitle((CharSequence) "SN Unrar");
        } else {
         //  getSupportActionBar().setTitle((CharSequence) FileUtil.getFolderName(this.dossierCourant.getChemin()));
            Log.e("asdfadfef", "" + FileUtil.getFolderName(this.dossierCourant.getChemin()));

        }
        if (!this.selectFileToZipState) {
            this.elementsSelectionnes.clear();
        }
        updateStorageSpace();
    }

    public void refresh() {
        if (this.dossierCourant != null) {
            updateDossierCourant();
            new RefreshAsyncTask(this).execute(new Dossier[]{this.dossierCourant});
        }
    }

    private void updateDossierCourant() {
        if (this.askToChangeDirOnNextRefresh) {
            Dossier dossier = new Dossier(this.goToNextRefreshfolderName, this.dossierCourant);
            File file = new File(dossier.getChemin());
            if (!file.exists()) {
                file.mkdir();
            }
            if (file.isDirectory()) {
                this.dossierCourant = dossier;
            }
            this.goToNextRefreshfolderName = null;
            this.askToChangeDirOnNextRefresh = false;
        }
    }

    public void onCheckedChanged(View view) {
        CheckBox checkBox = (CheckBox) view;
        Log.i(BrowseSdcardActivity.class.getName(), "onCheckedChanged");
        int intValue = ((Integer) checkBox.getTag()).intValue();
        int size = this.dossierCourant.getContenu().size();
        if (intValue == -1 || intValue >= size) {
            Toast.makeText(this, "Unable to select the element, try to go back to the parent folder and re-execute.", Toast.LENGTH_LONG).show();
        } else if (checkBox.isChecked()) {
            this.elementsSelectionnes.add(this.dossierCourant.getContenu().get(intValue));
            Log.i(BrowseSdcardActivity.class.getName(), "add element");
        } else {
            this.elementsSelectionnes.remove(this.dossierCourant.getContenu().get(intValue));
            Log.i(BrowseSdcardActivity.class.getName(), "remove element");
        }
    }

    public void onClickRetour(View view) {
        Dossier dossier = this.dossierCourant;
        if (dossier != null) {
            Dossier dossier2 = (Dossier) dossier.getParent();
            if (dossier2 != null) {
                this.dossierCourant = dossier2;
                tvFilePath.setText(dossierCourant.getChemin());
                new RefreshAsyncTask(this).execute(new Dossier[]{this.dossierCourant});
                return;
            }
            finish();
        }
    }

    private void showExitMessage() {
        VersionUtilitaire.existdialogMessage(this);
    }

    public void onClickSortFile(View view) {
        if (this.dossierCourant != null) {
            openContextMenu(findViewById(R.id.listViewImportFichier));
        }
    }

    public void onClickExtraire() {
        if (this.elementsSelectionnes.size() == 1) {
            final FileSystemElement fileSystemElement = (FileSystemElement) this.elementsSelectionnes.get(0);
            final ExtractDirDialog extractDirDialog = new ExtractDirDialog(this, fileSystemElement.getNom(), this.dossierCourant.getChemin());
            extractDirDialog.show();
            extractDirDialog.setOnDismissListener(new OnDismissListener() {
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

    /* access modifiers changed from: private */
    public void extractArchive(FileSystemElement fileSystemElement, boolean z, String str) {
        FileMimeCategorie mimeCategorie = FileUtil.getMimeCategorie(fileSystemElement.getChemin());

        String createTargetFolder = createTargetFolder(fileSystemElement.getParent().getChemin(), z, str);
        //   Log.e("sadfkadfjklhadlfj", fileSystemElement.getChemin() + "              " + createTargetFolder + "          " + checkDelete);
        if (mimeCategorie == FileMimeCategorie.rar) {
            extraireRar(fileSystemElement, createTargetFolder);
        } else if (mimeCategorie == FileMimeCategorie.zip) {
            extraireZip(fileSystemElement, createTargetFolder);
        } else if (mimeCategorie == FileMimeCategorie.sevenZip) {
            extraire7zip(fileSystemElement, createTargetFolder);
        }
        if (z) {
            this.askToChangeDirOnNextRefresh = true;
            this.goToNextRefreshfolderName = str;
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

    public void extraireRar(FileSystemElement fileSystemElement, String str) {
        ExtractRarAsync extractRarAsync = this.asyncExtract;
        if (extractRarAsync == null) {
            this.asyncExtract = new ExtractRarAsync(this);
            this.asyncExtract.execute(new String[]{fileSystemElement.getChemin(), str});
        } else if (extractRarAsync.getStatus() != Status.RUNNING) {
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
        } else if (extract7zipAsync.getStatus() != Status.RUNNING) {
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
        } else if (extractZipAsync.getStatus() != Status.RUNNING) {
            this.asyncExtractZip = new ExtractZipAsync(this);
            this.asyncExtractZip.execute(new String[]{fileSystemElement.getChemin(), str});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }

    public void onClickOpenArchive(View view) {
        if (this.elementsSelectionnes.size() <= 0) {
            Toast.makeText(this, getString(R.string.au_moins_un_fichier), Toast.LENGTH_LONG).show();
        } else if (this.elementsSelectionnes.size() == 1) {
            listerFichier();

        } else {
            Toast.makeText(this, getString(R.string.seulement_un_fichier), Toast.LENGTH_LONG).show();
        }
    }

    /* access modifiers changed from: private */
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

    private void listerRar(FileSystemElement fileSystemElement) {
        ListRarAsync listRarAsync = this.asyncList;
        if (listRarAsync == null) {
            this.asyncList = new ListRarAsync(this, fileSystemElement);
            this.asyncList.execute(new String[]{fileSystemElement.getChemin()});
        } else if (listRarAsync.getStatus() != Status.RUNNING) {
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
        } else if (listZipAsync.getStatus() != Status.RUNNING) {
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
        } else if (list7zipAsync.getStatus() != Status.RUNNING) {
            this.asyncList7zip = new List7zipAsync(this, fileSystemElement);
            this.asyncList7zip.execute(new String[]{fileSystemElement.getChemin()});
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD LIST RUNNING");
        }
    }

    public void executeFichier(File file) {
        if (this.selectFileToZipState) {
            return;
        }
        if (file.exists()) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtil.getExtension(file.getName()).toLowerCase());

            try {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri uriForFile = FileProvider.getUriForFile(this, VersionUtilitaire.getProviderName(this), file);
                intent.addFlags(1);
                intent.setDataAndType(uriForFile, mimeTypeFromExtension);
                startActivity(intent);
            } catch (Exception unused) {
                Log.e("errorOPenFile", unused.getMessage());
                Toast.makeText(this, getText(R.string.aucun_app_pour_fichier), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "File don't exist on the filesystem, remove by another browser ?", 1).show();
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long j) {
        if (this.dossierCourant.getContenu().size() > i && i >= 0) {
            final FileSystemElement fileSystemElement = (FileSystemElement) this.dossierCourant.getContenu().get(i);
            DialogUtil.deleteDiag(this, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    File file = new File(fileSystemElement.getChemin());
                    new DeleteAsyncTask(BrowseSdcardActivity.this).execute(new File[]{file});
                }
            }).show();
        }
        return true;
    }

    public void onClickValidCompress() {
        if (this.elementsSelectionnes.size() > 0) {
            String[] strings = new String[elementsSelectionnes.size()];
            for (int i = 0; i < elementsSelectionnes.size(); i++) {
                strings[i] = String.valueOf(elementsSelectionnes.get(i).getChemin());
            }


            final NewArchiveDialog newArchiveDialog = new NewArchiveDialog(this, null);
            newArchiveDialog.setElementsSelectionnes(this.elementsSelectionnes);
            newArchiveDialog.setCheminDest(this.dossierCourant.getChemin());
            newArchiveDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    if (newArchiveDialog.isCompress()) {
                        BrowseSdcardActivity.this.retablirSelectionZip();
                    }
                }
            });
            new ComputeSizeAsyncTaskStart(this, newArchiveDialog).execute(new String[0]);

            return;
        }
        Toast.makeText(this, getString(R.string.au_moins_un_fichier), Toast.LENGTH_LONG).show();
    }

    /* access modifiers changed from: private */
    public void enableBrowseModeForExtract() {
        this.elementsSelectionnes.clear();
        DocumentImportAdapter documentImportAdapter = this.docAdapter;
        if (documentImportAdapter != null) {
            this.docAdapter.notifyDataSetChanged();
        }
        this.selectFileToZipState = false;
    }

    /* access modifiers changed from: private */
    public void retablirSelectionZip() {
        enableBrowseModeForExtract();
        selectExtractFileBottomMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (VersionUtilitaire.isLite(this)) {
            menuInflater.inflate(R.menu.premium_menu, menu);
            return true;
        }
        menuInflater.inflate(R.menu.normal_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.itemMenuPremium) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW");
                if (VersionUtilitaire.isLite(this)) {
                    intent.setData(Uri.parse("market://details?id=com.sn.unziparchiver.lite"));
                } else {
                    intent.setData(Uri.parse("market://details?id=com.sn.unziparchiver.premium"));
                }
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(BrowseSdcardActivity.class.getName(), "Unable to start the market activity", e);
            }
            return true;
        } else if (menuItem.getItemId() == R.id.itemChangeSdcard) {
            TextView textView = this.cheminRepCourant;
            if (textView != null) {
                openContextMenu(textView);
            }
            return true;
        } else {
            if (menuItem.getItemId() == R.id.aboutApp) {

            } else if (menuItem.getItemId() == 16908332) {
                onClickRetour(null);
            } else if (menuItem.getItemId() == R.id.butSortFile) {
                onClickSortFile(null);
            }
            return false;
        }
    }

    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo) {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
        if (view.getId() == R.id.listViewImportFichier) {
            contextMenu.setHeaderIcon(R.drawable.ic_sort_black_48dp);
            contextMenu.setHeaderTitle(R.string.sort_menu_title);
            contextMenu.add(R.string.sort_by_name);
            contextMenu.add(R.string.sort_by_name_inv);
            contextMenu.add(R.string.sort_by_last_modified);
            contextMenu.add(R.string.sort_by_last_modified_inv);
            contextMenu.add(R.string.sort_by_extension);
            contextMenu.add(R.string.sort_by_extension_inv);
            contextMenu.add(R.string.sort_by_size);
            contextMenu.add(R.string.sort_by_size_inv);
            return;
        }
        contextMenu.setHeaderTitle(R.string.changeSdcard);
        contextMenu.setHeaderIcon(R.drawable.ic_sd_storage_black_48dp);
        List<File> mountPointList = SdcardUtilitaire.getMountPointList();
        if (mountPointList == null || mountPointList.size() <= 0) {
            Toast.makeText(this, getString(R.string.aucune_sdcard), Toast.LENGTH_LONG).show();
            return;
        }
        for (File absolutePath : mountPointList) {
            contextMenu.add(absolutePath.getAbsolutePath());
        }
    }

    public boolean onContextItemSelected(MenuItem menuItem) {
        String charSequence = menuItem.getTitle().toString();
        if (charSequence.equals(getString(R.string.sort_by_extension))) {
            this.fileSortComparator = ExtensionFileComparator.EXTENSION_INSENSITIVE_COMPARATOR;
        } else if (charSequence.equals(getString(R.string.sort_by_name))) {
            this.fileSortComparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
        } else if (charSequence.equals(getString(R.string.sort_by_last_modified))) {
            this.fileSortComparator = LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
        } else if (charSequence.equals(getString(R.string.sort_by_size))) {
            this.fileSortComparator = SizeFileComparator.SIZE_COMPARATOR;
        } else if (charSequence.equals(getString(R.string.sort_by_extension_inv))) {
            this.fileSortComparator = ExtensionFileComparator.EXTENSION_INSENSITIVE_REVERSE;
        } else if (charSequence.equals(getString(R.string.sort_by_name_inv))) {
            this.fileSortComparator = NameFileComparator.NAME_INSENSITIVE_REVERSE;
        } else if (charSequence.equals(getString(R.string.sort_by_last_modified_inv))) {
            this.fileSortComparator = LastModifiedFileComparator.LASTMODIFIED_REVERSE;
        } else if (charSequence.equals(getString(R.string.sort_by_size_inv))) {
            this.fileSortComparator = SizeFileComparator.SIZE_REVERSE;
        } else {
            String charSequence2 = menuItem.getTitle().toString();
            if (!charSequence2.equals(getString(R.string.aucune_sdcard))) {
                File file = new File(charSequence2);
                if (file.exists() && file.isDirectory()) {
                    this.dossierCourant = new Dossier("ROOT", null);
                    this.dossierCourant.setChemin(charSequence2);
                    this.currentMountPoint = charSequence2;
                }
            }
        }
        refresh();
        return true;
    }

    public void onBackPressed() {
        onClickRetour(null);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }
}
