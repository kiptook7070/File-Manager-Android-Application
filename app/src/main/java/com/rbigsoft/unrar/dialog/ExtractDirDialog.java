package com.rbigsoft.unrar.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.rbigsoft.sn.unzip.R;

import com.rbigsoft.unrar.model.document.Dossier;

import java.io.File;

import net.lingala.zip4j.util.InternalZipConstants;

import static android.content.Context.MODE_PRIVATE;

public class ExtractDirDialog extends Dialog implements TextWatcher, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static String[] invalidChars = {InternalZipConstants.ZIP_FILE_SEPARATOR, "\\", ".."};
    private String baseName;
    private boolean cancel = false;
    private Button cancelButton;
    private CheckBox checkBoxExtract;
    private SharedPreferences shareDeleteFile;
    private TextView errorTextView;
    private Button extractButton;
    private EditText extractFolderEditText;
    private boolean extractToFolder = false;
    private String folderName;
    private String chimen = "";
    private boolean checkDelete = false;
    private CheckBox cbDelete;

    public void afterTextChanged(Editable editable) {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public ExtractDirDialog(Context context, String str, String pathFile) {
        super(context, R.style.Dialog);
        requestWindowFeature(3);
        setContentView(R.layout.extract_dir);

        this.chimen = pathFile;
        this.baseName = removeExtension(str);

        initWidget();
    }

    @SuppressLint("WrongConstant")
    private void initWidget() {
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        shareDeleteFile = getContext().getSharedPreferences("deletefile", MODE_PRIVATE);
        checkDelete = shareDeleteFile.getBoolean("deletefile", false);
        this.extractFolderEditText = (EditText) findViewById(R.id.editTextExtractFolder);
        this.cbDelete = findViewById(R.id.cb_delete_file_ex);
        cbDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        this.extractButton = (Button) findViewById(R.id.butExtractExtractDir);
        this.cancelButton = (Button) findViewById(R.id.butExtractDirCancel);
        this.checkBoxExtract = (CheckBox) findViewById(R.id.checkBoxExtractDir);
        this.checkBoxExtract.setChecked(false);
        this.errorTextView = (TextView) findViewById(R.id.textViewErrorFolderName);
        this.errorTextView.setVisibility(4);
        this.extractButton.setOnClickListener(this);
        this.cancelButton.setOnClickListener(this);
        this.checkBoxExtract.setOnCheckedChangeListener(this);
        this.extractFolderEditText.setText(this.baseName);
        this.extractFolderEditText.addTextChangedListener(this);
        this.extractFolderEditText.setEnabled(false);
        validText();
    }

    private String removeExtension(String str) {
        int lastIndexOf = str.lastIndexOf(".");
        return (lastIndexOf == -1 || str.length() <= lastIndexOf) ? str : str.substring(0, lastIndexOf);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.butExtractDirCancel) {
            this.cancel = true;
        }
        dismiss();
    }

    @SuppressLint("WrongConstant")
    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.extractFolderEditText.setEnabled(z);
        this.extractToFolder = z;
        if (z) {
            validText();
            return;
        }
        if (this.extractFolderEditText.getText().toString().isEmpty()) {
            this.extractFolderEditText.setText(this.baseName);
        }
        enableExtract(true);
        this.errorTextView.setText("");
        this.errorTextView.setVisibility(4);
    }

    public boolean isExtractToFolder() {
        return this.extractToFolder;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        validText();
    }

    @SuppressLint("WrongConstant")
    private void validText() {
        String obj = this.extractFolderEditText.getText().toString();
        if (!obj.isEmpty()) {
            String str = null;
            String[] strArr = invalidChars;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String str2 = strArr[i];
                if (obj.contains(str2)) {
                    str = str2;
                    break;
                }
                i++;
            }
            if (obj.startsWith(".")) {
                str = ".";
            }
            if (str != null) {
                enableExtract(false);
                TextView textView = this.errorTextView;
                textView.setText(getContext().getString(R.string.extract_dir_diag_error_invalid_char) + str);
                this.errorTextView.setVisibility(0);
            } else if (obj.length() > 254) {
                enableExtract(false);
                this.errorTextView.setText(getContext().getString(R.string.extract_dir_diag_error_invalid_name));
                this.errorTextView.setVisibility(0);
            } else {

                File file = new File(chimen + InternalZipConstants.ZIP_FILE_SEPARATOR + obj);
                if (!file.isFile() || !file.exists()) {
                    enableExtract(true);
                    this.errorTextView.setText("");
                    this.errorTextView.setVisibility(4);
                    return;
                }
                enableExtract(false);
                this.errorTextView.setText(getContext().getString(R.string.extract_dir_diag_error_invalid_fileexist));
                this.errorTextView.setVisibility(0);
            }
        } else {
            enableExtract(false);
            this.errorTextView.setText(getContext().getString(R.string.extract_dir_diag_error_invalid_nameempty));
            this.errorTextView.setVisibility(0);
        }
    }

    private void enableExtract(boolean z) {
        this.extractButton.setEnabled(z);
        if (z) {
            this.folderName = this.extractFolderEditText.getText().toString();
        }
    }

    public boolean isCancel() {
        return this.cancel;
    }
}
