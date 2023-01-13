package com.rbigsoft.unrar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;
import com.rbigsoft.unrar.adapter.ArchiveDialogAdapter;
import com.rbigsoft.unrar.model.FileMimeCategorie;
import com.rbigsoft.unrar.model.archive.ArchiveDossier;
import com.rbigsoft.unrar.model.archive.ArchiveElement;
import com.rbigsoft.unrar.model.document.Dossier;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.nativeinterface.Extract7zipAsync;
import com.rbigsoft.unrar.nativeinterface.ExtractRarAsync;
import com.rbigsoft.unrar.nativeinterface.ExtractZipAsync;
import com.rbigsoft.unrar.utilitaire.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.util.InternalZipConstants;

public class ArchiveDiag extends Dialog implements AdapterView.OnItemClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ArchiveDialogAdapter adapter;
    private FileSystemElement archive;
    private ExtractRarAsync asyncExtract;
    private Extract7zipAsync asyncExtract7Zip;
    private ExtractZipAsync asyncExtractZip;
    private Button butExtract;
    private Context callContext;
    private Button checkAllButton;
    private ArchiveDossier dossierCourant;
    private List<ArchiveElement> elementSelect = new ArrayList();
    private ListView listeView;
    int prevOrientation;
    private ArchiveDossier root;
    private TextView textViewPath;
    private Button unckeckAllButton;

    public ArchiveDiag(Context context, ArchiveDossier archiveDossier, FileSystemElement fileSystemElement) {
        super(context, R.style.Dialog);
        requestWindowFeature(3);
        this.callContext = context;
        setContentView(R.layout.archive_diag);

        this.archive = fileSystemElement;
        setTitle(fileSystemElement.getNom());
        this.root = archiveDossier;
        this.dossierCourant = archiveDossier;
        this.listeView = (ListView) findViewById(R.id.listViewArchive);
        this.adapter = new ArchiveDialogAdapter(getContext(), archiveDossier, this, this.elementSelect);
        this.listeView.setAdapter(this.adapter);
        this.listeView.setOnItemClickListener(this);
        this.textViewPath = (TextView) findViewById(R.id.textViewArchivePath);
        findViewById(R.id.butArchiveRetour).setOnClickListener(this);
        this.butExtract = (Button) findViewById(R.id.butExtractSelectFile);
        this.butExtract.setOnClickListener(this);
        ((Button) findViewById(R.id.butAnnuleExtractFile)).setOnClickListener(this);
        changePath();
        refreshNbrFileButton();
        this.checkAllButton = (Button) findViewById(R.id.butCheckAll);
        this.unckeckAllButton = (Button) findViewById(R.id.butUnckeckAll);
        this.checkAllButton.setOnClickListener(this);
        this.unckeckAllButton.setOnClickListener(this);
    }

    private void changePath() {
        if (this.dossierCourant.getChemin().length() > 0) {
            this.textViewPath.setText(FileUtil.realFilePathToDisplayPath(this.dossierCourant.getChemin(), false));
        } else {
            this.textViewPath.setText("");
        }
    }

    private void listeChange() {
        this.adapter.setDossier(this.dossierCourant);
        changePath();
        this.adapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        ArchiveElement element = this.dossierCourant.getElement(i);
        if (element instanceof ArchiveDossier) {
            this.dossierCourant = (ArchiveDossier) element;
            listeChange();
        }
    }

    /* access modifiers changed from: private */
    public void extractFiles(boolean z, String str) {
        if (this.elementSelect.size() >= 1) {

            String createTargetFolder = "";
            try {

                createTargetFolder = createTargetFolder(this.archive.getParent().getChemin(), z, str);
            } catch (Exception e) {
                String xx = "/" + this.archive.getNom();
                String pathSave = this.archive.getChemin().replace(xx, "");
                createTargetFolder = createTargetFolder(pathSave, z, str);
            }
            if (FileUtil.getMimeCategorie(this.archive.getChemin()) == FileMimeCategorie.rar) {
                extractRar(createTargetFolder);
            } else if (FileUtil.getMimeCategorie(this.archive.getChemin()) == FileMimeCategorie.zip) {
                extractZip(createTargetFolder);
            } else if (FileUtil.getMimeCategorie(this.archive.getChemin()) == FileMimeCategorie.sevenZip) {
                extract7zip(createTargetFolder);
            }
        } else if (this.elementSelect.size() == 0) {
            Log.e(BrowseSdcardActivity.class.getName(), "AUCUN FICHIER SELECTIONNE");
        }
    }

    private String createTargetFolder(String str, boolean z, String str2) {
        if (!z) {
            return str;
        }
        String str3 = str + InternalZipConstants.ZIP_FILE_SEPARATOR + str2;
        File file = new File(str3);
        return (file.exists() || file.mkdir()) ? str3 : str;
    }

    private void extractRar(String str) {
        ExtractRarAsync extractRarAsync = this.asyncExtract;
        if (extractRarAsync == null) {
            this.asyncExtract = new ExtractRarAsync(this.callContext);
            this.asyncExtract.execute(createListeFileArgRar(this.archive.getChemin(), str));
        } else if (extractRarAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncExtract = new ExtractRarAsync(this.callContext);
            this.asyncExtract.execute(createListeFileArgRar(this.archive.getChemin(), str));
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }

    private void extract7zip(String str) {
        Extract7zipAsync extract7zipAsync = this.asyncExtract7Zip;
        if (extract7zipAsync == null) {
            this.asyncExtract7Zip = new Extract7zipAsync(this.callContext);
            this.asyncExtract7Zip.execute(createListeFileArg7zip(this.archive.getChemin(), str));
        } else if (extract7zipAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncExtract7Zip = new Extract7zipAsync(this.callContext);
            this.asyncExtract7Zip.execute(createListeFileArg7zip(this.archive.getChemin(), str));
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }

    private void extractZip(String str) {
        ExtractZipAsync extractZipAsync = this.asyncExtractZip;
        if (extractZipAsync == null) {
            this.asyncExtractZip = new ExtractZipAsync(this.callContext);
            this.asyncExtractZip.execute(createListeFileArgZip(this.archive.getChemin(), str));
        } else if (extractZipAsync.getStatus() != AsyncTask.Status.RUNNING) {
            this.asyncExtractZip = new ExtractZipAsync(this.callContext);
            this.asyncExtractZip.execute(createListeFileArgZip(this.archive.getChemin(), str));
        } else {
            Log.e(BrowseSdcardActivity.class.getName(), "THREAD EXTRACT RUNNING");
        }
    }

    private String[] createListeFileArgZip(String str, String str2) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(str);
        arrayList.add(str2);
        for (ArchiveElement next : this.elementSelect) {
            if (next instanceof ArchiveDossier) {
                arrayList.add(next.getChemin() + InternalZipConstants.ZIP_FILE_SEPARATOR);
                for (ArchiveElement next2 : ((ArchiveDossier) next).getRecusiveAllFile()) {
                    if (next2 instanceof ArchiveDossier) {
                        arrayList.add(next2.getChemin() + InternalZipConstants.ZIP_FILE_SEPARATOR);
                    } else {
                        arrayList.add(next2.getChemin());
                    }
                }
            } else {
                arrayList.add(next.getChemin());
            }
        }
        String[] strArr = new String[arrayList.size()];
        int i = 0;
        for (String str3 : arrayList) {
            strArr[i] = str3;
            i++;
        }
        return strArr;
    }

    private String[] createListeFileArgRar(String str, String str2) {
        int size = this.elementSelect.size() + 2;
        String[] strArr = new String[size];
        int i = 0;
        strArr[0] = str;
        strArr[1] = str2;
        for (int i2 = 2; i2 < size; i2++) {
            strArr[i2] = "-n" + this.elementSelect.get(i).getChemin();
            i++;
        }
        return strArr;
    }

    private String[] createListeFileArg7zip(String str, String str2) {
        int size = this.elementSelect.size() + 2;
        String[] strArr = new String[size];
        int i = 0;
        strArr[0] = str;
        strArr[1] = str2;
        for (int i2 = 2; i2 < size; i2++) {
            strArr[i2] = this.elementSelect.get(i).getChemin().substring(1);
            i++;
        }
        return strArr;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.butArchiveRetour) {
//            if (this.dossierCourant.getParent() != null) {
//                this.dossierCourant = this.dossierCourant.getParent();
//                listeChange();
//            }
            dismiss();
        } else if (view.getId() == R.id.butAnnuleExtractFile) {
            dismiss();
        } else if (view.getId() == R.id.butExtractSelectFile) {
            if (this.elementSelect.size() == 0) {
                Toast.makeText(getContext(), getContext().getString(R.string.au_moins_un_fichier), 1).show();
            } else {
                extractSelection();
            }
        } else if (view.getId() == R.id.butCheckAll) {
            checkAll();
        } else if (view.getId() == R.id.butUnckeckAll) {
            unCheckAll();
        }
    }

    private void extractSelection() {

        try {
            if (this.archive.getParent() == null) {
                String xx = "/" + this.archive.getNom();
                String pathSave = this.archive.getChemin().replace(xx, "");
                final ExtractDirDialog extractDirDialog = new ExtractDirDialog(getContext(), this.archive.getNom(), pathSave);
                extractDirDialog.show();
                extractDirDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (!extractDirDialog.isCancel()) {
                            ArchiveDiag.this.extractFiles(extractDirDialog.isExtractToFolder(), extractDirDialog.getFolderName());
                            ArchiveDiag.this.dismiss();
                        }
                    }
                });
            } else {
                final ExtractDirDialog extractDirDialog = new ExtractDirDialog(getContext(), this.archive.getNom(), this.archive.getParent().getChemin());
                extractDirDialog.show();
                extractDirDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (!extractDirDialog.isCancel()) {
                            ArchiveDiag.this.extractFiles(extractDirDialog.isExtractToFolder(), extractDirDialog.getFolderName());
                            ArchiveDiag.this.dismiss();
                        }
                    }
                });
            }
        } catch (Exception e) {
            String xx = "/" + this.archive.getNom();
            String pathSave = this.archive.getChemin().replace(xx, "");
            final ExtractDirDialog extractDirDialog = new ExtractDirDialog(getContext(), this.archive.getNom(), pathSave);
            extractDirDialog.show();
            extractDirDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    if (!extractDirDialog.isCancel()) {
                        ArchiveDiag.this.extractFiles(extractDirDialog.isExtractToFolder(), extractDirDialog.getFolderName());
                        ArchiveDiag.this.dismiss();
                    }
                }
            });
        }


    }

    private void checkAll() {
        this.adapter.checkAll(true);
        listeChange();
    }

    private void unCheckAll() {
        this.adapter.checkAll(false);
        listeChange();
    }

    private void refreshNbrFileButton() {
        this.butExtract.setText(getContext().getString(R.string.extraire) + " " + this.elementSelect.size() + " " + getContext().getString(R.string.fichiers));
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        ArchiveElement element = this.dossierCourant.getElement(((Integer) compoundButton.getTag()).intValue());
        if (!z) {
            this.elementSelect.remove(element);
        } else if (!this.elementSelect.contains(element)) {
            this.elementSelect.add(element);
        }
        refreshNbrFileButton();
    }
}
