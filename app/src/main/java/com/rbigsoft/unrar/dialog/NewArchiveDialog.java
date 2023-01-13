package com.rbigsoft.unrar.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.sn.unzip.activity.FileActivity;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;
import com.rbigsoft.unrar.model.ListZipFileInfos;
import com.rbigsoft.unrar.model.document.Dossier;
import com.rbigsoft.unrar.model.document.Fichier;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.nativeinterface.CompressZipAsync;
import com.rbigsoft.unrar.utilitaire.AvailableSpaceHandler;
import com.rbigsoft.unrar.utilitaire.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import static android.content.Context.MODE_PRIVATE;

public class NewArchiveDialog extends Dialog implements OnClickListener, OnCheckedChangeListener {
    private BrowseSdcardActivity activity;
    private FileActivity fileActivity;
    private String cheminDest;
    private boolean compress = false;
    private boolean cryptEnable = false;
    private EditText editTextNbrFichier;
    private EditText editTextNom;
    private EditText editTextPassword;
    private EditText editTextTailleFichier;
    private List<FileSystemElement> elementsSelectionnes;
    private boolean multipartEnable = false;
    boolean nbrFichierChange = false;
    private boolean selectFile = false;
    private Spinner spinerRapiditeCompress;
    private Spinner spinerTypeCrypt;
    boolean tailleFichierChange = false;
    private ToggleButton toggleCrypt;
    private ToggleButton toggleMultipart;
    private long totaleSize;
    private String type = "zip";
    private int typButton = 1;
    private RadioGroup rgTypeCompress;
    private RadioButton rbtnZip, rbtn7z, rbtnTar;
    private CheckBox cbDeleteFile;
    private boolean checkDelete = false;
    private SharedPreferences shareDeleteFile;
    class NbrFichierTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable editable) {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        NbrFichierTextWatcher() {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (NewArchiveDialog.this.verifNbrFichier() && !NewArchiveDialog.this.tailleFichierChange) {
                NewArchiveDialog newArchiveDialog = NewArchiveDialog.this;
                newArchiveDialog.nbrFichierChange = true;
                newArchiveDialog.computeTaillePartie();
                NewArchiveDialog.this.nbrFichierChange = false;
            }
        }
    }

    class TailleFichierTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable editable) {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        TailleFichierTextWatcher() {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (NewArchiveDialog.this.verifTaillePartie() && !NewArchiveDialog.this.nbrFichierChange) {
                NewArchiveDialog newArchiveDialog = NewArchiveDialog.this;
                newArchiveDialog.tailleFichierChange = true;
                newArchiveDialog.computeNbrPartie();
                NewArchiveDialog.this.tailleFichierChange = false;
            }
        }
    }

    public NewArchiveDialog(FileActivity fileActivity) {
        super(fileActivity, R.style.Dialog);
        this.fileActivity = fileActivity;
    }

    public NewArchiveDialog(BrowseSdcardActivity browseSdcardActivity, FileActivity fileActivity) {
        super(browseSdcardActivity, R.style.Dialog);
        if (browseSdcardActivity != null) {
            this.activity = browseSdcardActivity;
        }
        if (fileActivity != null) {
            this.fileActivity = fileActivity;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(3);
        setContentView(R.layout.compress_zip);
        shareDeleteFile = getContext().getSharedPreferences("deletefile", MODE_PRIVATE);
        checkDelete = shareDeleteFile.getBoolean("deletefile", false);
        ((Button) findViewById(R.id.butCompress)).setOnClickListener(this);
        ((Button) findViewById(R.id.butAnnuleCompressDiag)).setOnClickListener(this);
        editTextNom = (EditText) findViewById(R.id.editTextArchiveNom);
        displayTotalSize();
        this.editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        this.editTextNbrFichier = (EditText) findViewById(R.id.editTextNbrFichier);
        this.editTextTailleFichier = (EditText) findViewById(R.id.editTextTailleFichier);
        textListener();
        defSpinerRapiditeCompress();
        defSpinerCrypt();
        defToggleButton();

        ArrayList<File> arrayList = getListFichier();
        rgTypeCompress = findViewById(R.id.rgTypeCompress);
        rbtnZip = findViewById(R.id.rbtn_zip);
        rbtn7z = findViewById(R.id.rbtn_7z);
        rbtnTar = findViewById(R.id.rbtn_tar);
        rbtnZip.setChecked(true);
        rgTypeCompress.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_zip:
                        type = "zip";
                        editTextNom.setText(arrayList.get(arrayList.size() - 1).getName() + "." + type);
                        editTextPassword.setEnabled(true);
                        break;
                    case R.id.rbtn_7z:
                        type = "7z";
                        editTextPassword.setEnabled(false);

                        editTextNom.setText(arrayList.get(arrayList.size() - 1).getName() + "." + type);
                        break;
                    case R.id.rbtn_tar:
                        type = "tar";
                        editTextPassword.setEnabled(false);
                        editTextNom.setText(arrayList.get(arrayList.size() - 1).getName() + "." + type);
                        break;
                }
            }
        });
        editTextNom.setText(arrayList.get(arrayList.size() - 1).getName() + "." + type);
        cbDeleteFile = findViewById(R.id.cb_delete_file);
        cbDeleteFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    checkDelete = true;
                    SharedPreferences.Editor editor = shareDeleteFile.edit();
                    editor.putBoolean("deletefile", checkDelete);
                    editor.commit();
                } else {
                    checkDelete = false;
                    SharedPreferences.Editor editor = shareDeleteFile.edit();
                    editor.putBoolean("deletefile", checkDelete);
                    editor.commit();
                }
            }
        });
        ArrayList<File> arrayList1 = getListFichier();
        for (int i = 0; i < arrayList1.size(); i++) {
            Log.e("asdfdfefefefefefef", arrayList1.get(i).getName() + "            " + arrayList1.get(i).getPath());
        }
    }


    private void enableMultipart(boolean z) {
        this.editTextNbrFichier.setEnabled(z);
        this.editTextTailleFichier.setEnabled(z);
        this.multipartEnable = z;
    }

    private void defToggleButton() {
        this.toggleCrypt = (ToggleButton) findViewById(R.id.toggleButtonEcrypt);
        this.toggleCrypt.setOnCheckedChangeListener(this);
        this.toggleMultipart = (ToggleButton) findViewById(R.id.toggleButtonMultipart);
        this.toggleMultipart.setOnCheckedChangeListener(this);
    }

    private void defSpinerCrypt() {
        this.spinerTypeCrypt = (Spinner) findViewById(R.id.spinnerTypeCrypt);
        ArrayAdapter createFromResource = ArrayAdapter.createFromResource(getContext(), R.array.ListEncyptionType, 17367048);
        createFromResource.setDropDownViewResource(17367049);
        this.spinerTypeCrypt.setAdapter(createFromResource);
    }

    private void defSpinerRapiditeCompress() {
        this.spinerRapiditeCompress = (Spinner) findViewById(R.id.spinnerRapiditeCompress);
        ArrayAdapter createFromResource = ArrayAdapter.createFromResource(getContext(), R.array.listeRaditeCompress, 17367048);

        createFromResource.setDropDownViewResource(17367049);
        this.spinerRapiditeCompress.setAdapter(createFromResource);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.butCompress) {
            compressFile();
        } else if (view.getId() == R.id.butAnnuleCompressDiag) {
            //elementsSelectionnes.clear();
            dismiss();
        }
    }

    private void textListener() {
        NbrFichierTextWatcher nbrFichierTextWatcher = new NbrFichierTextWatcher();
        TailleFichierTextWatcher tailleFichierTextWatcher = new TailleFichierTextWatcher();
        this.editTextNbrFichier.addTextChangedListener(nbrFichierTextWatcher);
        this.editTextTailleFichier.addTextChangedListener(tailleFichierTextWatcher);
    }

    /* access modifiers changed from: private */
    public void computeNbrPartie() {
        try {
            long intValue = (long) Integer.valueOf(this.editTextTailleFichier.getText().toString()).intValue();
            if (intValue < this.totaleSize && intValue > PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH) {
                double d = (double) this.totaleSize;
                double d2 = (double) intValue;
                Double.isNaN(d);
                Double.isNaN(d2);
                this.editTextNbrFichier.setText(String.valueOf((long) ((double) Math.round(d / d2))));
            }
        } catch (Exception unused) {
        }
    }

    /* access modifiers changed from: private */
    public void computeTaillePartie() {
        try {
            long intValue = (long) Integer.valueOf(this.editTextNbrFichier.getText().toString()).intValue();
            if (intValue < this.totaleSize) {
                this.editTextTailleFichier.setText(String.valueOf(this.totaleSize / intValue));
                verifTaillePartie();
            }
        } catch (Exception unused) {
        }
    }

    private void displayTotalSize() {
        ((TextView) findViewById(R.id.textViewTotalSize)).setText(AvailableSpaceHandler.buildTextFileSize(this.totaleSize, getContext()));
    }

    public void computeTotalSize() {
        for (FileSystemElement fileSystemElement : this.elementsSelectionnes) {
            if (fileSystemElement instanceof Fichier) {
                this.totaleSize += ((Fichier) fileSystemElement).getFileSize();
            } else if (fileSystemElement instanceof Dossier) {
                this.totaleSize += computeRepSize(new File(fileSystemElement.getChemin()));
            }
        }
    }

    private long computeRepSize(File file) {
        long j;
        long j2;
        int i = 0;
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                int length = listFiles.length;
                int i2 = 0;
                while (i < length) {
                    File file2 = listFiles[i];
                    if (file2.isDirectory()) {
                        j = (long) i2;
                        j2 = computeRepSize(file2);
                    } else {
                        j = (long) i2;
                        j2 = file2.length();
                    }
                    i2 = (int) (j + j2);
                    i++;
                }
                i = i2;
            }
        }
        return (long) i;
    }

    /* access modifiers changed from: private */
    public boolean verifNbrFichier() {
        try {
            if (Integer.valueOf(this.editTextNbrFichier.getText().toString()).intValue() > 0) {
                return true;
            }
            Toast.makeText(getContext(), getContext().getString(R.string.partie_inf_1), Toast.LENGTH_LONG).show();
            return false;
        } catch (Exception unused) {
            Toast.makeText(getContext(), getContext().getString(R.string.nbr_partie_invalide), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean verifTaillePartie() {
        try {
            long longValue = Long.valueOf(this.editTextTailleFichier.getText().toString()).longValue();
            if (longValue < PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH) {
                Toast.makeText(getContext(), getContext().getString(R.string.taille_part_trop_petite), 1).show();
                this.tailleFichierChange = true;
                this.editTextTailleFichier.setText("65536");
                this.tailleFichierChange = false;
                return false;
            } else if (longValue <= this.totaleSize) {
                return true;
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.taille_part_sup_fichier), 1).show();
                this.tailleFichierChange = true;
                this.editTextTailleFichier.setText(String.valueOf(this.totaleSize));
                this.tailleFichierChange = false;
                this.nbrFichierChange = true;
                this.editTextNbrFichier.setText(String.valueOf(1));
                this.nbrFichierChange = false;
                return false;
            }
        } catch (Exception unused) {
            Toast.makeText(getContext(), getContext().getString(R.string.taille_partie_invalide), 1).show();
            return false;
        }
    }

    private boolean verifInfos() {
        if (this.editTextNom.getText().toString().length() == 0) {
            Toast.makeText(getContext(), getContext().getString(R.string.nom_archive_vide), Toast.LENGTH_LONG).show();
            return false;
        } else if (!this.cryptEnable || this.editTextPassword.getText().toString().length() != 0) {
            return !this.multipartEnable || (verifNbrFichier() && verifTaillePartie());
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.password_vide), 1).show();
            return false;
        }
    }

    private void compressFile() {
        if (verifInfos()) {
            ListZipFileInfos listZipFileInfos = new ListZipFileInfos();
            ArrayList<File> listFichier = getListFichier();
            if (listFichier == null || listFichier.size() <= 0) {
                Toast.makeText(getContext(), getContext().getString(R.string.invalid_compress_liste_file), 1).show();
            } else {

                for (int i = 0; i < listFichier.size(); i++) {
                    Log.e("asdfasd", listFichier.get(i).getName() + "             " + listFichier.get(i).getPath());
                }
                // them file
                listZipFileInfos.setListeFichier(listFichier);

                // them kiểu nén thường hay nhanh
                listZipFileInfos.setNomArchive(corrigeNom());
                // thêm đường dẫn của file
                listZipFileInfos.setCheminDest(this.cheminDest);
                boolean z = this.multipartEnable;
                if (z) {
                    listZipFileInfos.setMultiPart(z);
                    listZipFileInfos.setSplitSize(Long.valueOf(this.editTextTailleFichier.getText().toString()).longValue());
                }
                ZipParameters zipParameter = setZipParameter();
                zipParameter.setDefaultFolderPath(getDefaultFolderPath(listFichier));
                zipParameter.setIncludeRootFolder(false);
                listZipFileInfos.setParameters(zipParameter);
                this.compress = true;
                dismiss();
                new CompressZipAsync(this.activity, fileActivity).execute(new ListZipFileInfos[]{listZipFileInfos});
            }
        }
    }

    private ZipParameters setZipParameter() {
        ZipParameters zipParameters = new ZipParameters();
        int compressionLevel = getCompressionLevel();
        if (compressionLevel != 0) {
            zipParameters.setCompressionMethod(8);
            zipParameters.setCompressionLevel(compressionLevel);
        } else {
            zipParameters.setCompressionMethod(0);
        }
        if (this.cryptEnable) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(getTypeCryptFromSpinPos());
            if (getTypeCryptFromSpinPos() == 99) {
                zipParameters.setAesKeyStrength(getAesLenght());
            }
            zipParameters.setPassword(this.editTextPassword.getText().toString());
        }
        return zipParameters;
    }

    private int getCompressionLevel() {
        int selectedItemPosition = this.spinerRapiditeCompress.getSelectedItemPosition();
        if (selectedItemPosition == 0) {
            return 5;
        }
        if (selectedItemPosition == 1) {
            return 1;
        }
        if (selectedItemPosition == 2) {
            return 3;
        }
        if (selectedItemPosition == 3) {
            return 7;
        }
        if (selectedItemPosition != 4) {
            return selectedItemPosition != 5 ? 5 : 0;
        }
        return 9;
    }

    private int getTypeCryptFromSpinPos() {
        return this.spinerTypeCrypt.getSelectedItemPosition() == 0 ? 0 : 99;
    }

    private int getAesLenght() {
        int selectedItemPosition = this.spinerTypeCrypt.getSelectedItemPosition();
        return (selectedItemPosition == 1 || selectedItemPosition != 2) ? 1 : 3;
    }

    private String getDefaultFolderPath(ArrayList<File> arrayList) {
        File file = (File) arrayList.get(0);
        int nbrSlash = nbrSlash(file.getAbsolutePath());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            File file2 = (File) it.next();
            int nbrSlash2 = nbrSlash(file2.getAbsolutePath());
            if (nbrSlash2 < nbrSlash) {
                file = file2;
                nbrSlash = nbrSlash2;
            }
        }
        return file.getParentFile().getAbsolutePath();
    }

    private int nbrSlash(String str) {
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (str.charAt(i2) == '/') {
                i++;
            }
        }
        return i;
    }

    private String corrigeNom() {
        String obj = this.editTextNom.getText().toString();
        String extension = FileUtil.getExtension(obj);
        if (extension.length() != 0 && extension.equalsIgnoreCase("zip")) {
            return obj;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(obj);
        sb.append(".zip");
        return sb.toString();
    }

    private ArrayList<File> getListFichier() {
        ArrayList<File> arrayList = new ArrayList<>();
        for (FileSystemElement chemin : this.elementsSelectionnes) {
            File file = new File(chemin.getChemin());
            if (file.isDirectory()) {
                arrayList.addAll(listFilesAndDirs(file, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
            } else {
                arrayList.add(file);
            }
        }
        return arrayList;
    }

    private static void innerListFiles(Collection<File> collection, File file, IOFileFilter iOFileFilter, boolean z) {
        File[] listFiles = file.listFiles((FileFilter) iOFileFilter);
        if (listFiles != null) {
            for (File file2 : listFiles) {
                if (file2.isDirectory()) {
                    if (z) {
                        collection.add(file2);
                    }
                    innerListFiles(collection, file2, iOFileFilter, z);
                } else {
                    collection.add(file2);
                }
            }
        }
    }


    private static void validateListFilesParameters(File file, IOFileFilter iOFileFilter) {
        if (!file.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Parameter 'directory' is not a directory: ");
            sb.append(file);
            throw new IllegalArgumentException(sb.toString());
        } else if (iOFileFilter == null) {
            throw new NullPointerException("Parameter 'fileFilter' is null");
        }
    }

    private static IOFileFilter setUpEffectiveFileFilter(IOFileFilter iOFileFilter) {
        return FileFilterUtils.and(iOFileFilter, FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
    }

    private static IOFileFilter setUpEffectiveDirFilter(IOFileFilter iOFileFilter) {
        if (iOFileFilter == null) {
            return FalseFileFilter.INSTANCE;
        }
        return FileFilterUtils.and(iOFileFilter, DirectoryFileFilter.INSTANCE);
    }

    public static Collection<File> listFilesAndDirs(File file, IOFileFilter iOFileFilter, IOFileFilter iOFileFilter2) {
        validateListFilesParameters(file, iOFileFilter);
        IOFileFilter upEffectiveFileFilter = setUpEffectiveFileFilter(iOFileFilter);
        IOFileFilter upEffectiveDirFilter = setUpEffectiveDirFilter(iOFileFilter2);
        LinkedList linkedList = new LinkedList();
        if (file.isDirectory()) {
            linkedList.add(file);
        }
        innerListFiles(linkedList, file, FileFilterUtils.or(upEffectiveFileFilter, upEffectiveDirFilter), true);
        return linkedList;
    }

    private ArrayList<File> getDirFile(File file) {
        File[] listFiles;
        ArrayList<File> arrayList = new ArrayList<>();
        arrayList.add(file);
        if (file.isDirectory()) {
            for (File file2 : file.listFiles()) {
                if (file2.isDirectory()) {
                    arrayList.addAll(getDirFile(file2));
                } else {
                    arrayList.add(file2);
                }
            }
        }
        return arrayList;
    }

    public boolean isSelectFile() {
        return this.selectFile;
    }

    public List<FileSystemElement> getElementsSelectionnes() {
        return this.elementsSelectionnes;
    }

    public void setElementsSelectionnes(List<FileSystemElement> list) {

        this.elementsSelectionnes = list;
    }

    public String getCheminDest() {
        return this.cheminDest;
    }

    public void setCheminDest(String str) {
        this.cheminDest = str;
    }

    public boolean isCompress() {
        return this.compress;
    }

    public void setCompress(boolean z) {
        this.compress = z;
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        if (compoundButton.getId() == R.id.toggleButtonEcrypt) {

        } else if (compoundButton.getId() != R.id.toggleButtonMultipart) {
        } else {
            if (this.totaleSize > PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH) {
                enableMultipart(z);
                this.nbrFichierChange = true;
                this.editTextNbrFichier.setText("1");
                this.nbrFichierChange = false;
                return;
            }
            Toast.makeText(getContext(), "The size of a multi-part zip cannot be less than 65536 Bytes (65 kB).", Toast.LENGTH_LONG).show();
        }
    }
}
