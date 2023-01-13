package com.rbigsoft.unrar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.rbigsoft.sn.unzip.R;


public class FileExistDialog extends Dialog implements OnClickListener, OnItemSelectedListener {
    private Context callContext;
    private String existingFile;
    private int result = 1;
    private Spinner spinner;

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public FileExistDialog(Context context, String str) {
        super(context, R.style.Dialog);
        this.existingFile = str;
        this.callContext = context;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(3);
        setContentView(R.layout.fileexist_diag);

        setTitle(getContext().getString(R.string.fichier_existe));
        this.spinner = (Spinner) findViewById(R.id.spinnerFileExistsType);
        ArrayAdapter createFromResource = ArrayAdapter.createFromResource(getContext(), R.array.fileExistOverriteType, 17367048);
        createFromResource.setDropDownViewResource(17367049);
        this.spinner.setAdapter(createFromResource);
        this.spinner.setOnItemSelectedListener(this);
        ((Button) findViewById(R.id.butOkFileExist)).setOnClickListener(this);
        ((Button) findViewById(R.id.butCancelFileExist)).setOnClickListener(this);
        TextView textView = (TextView) findViewById(R.id.textViewFilename);
        StringBuilder sb = new StringBuilder();
        sb.append(this.existingFile);
        sb.append(" ");
        sb.append(getContext().getString(R.string.file_exist));
        textView.setText(sb.toString());
    }

    public void onClick(View view) {
        if (view.getId() == R.id.butCancelFileExist) {
            this.result = 6;
        }
        dismiss();
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
        if (i == 0) {
            this.result = 1;
        } else if (i == 1) {
            this.result = 2;
        } else if (i == 2) {
            this.result = 3;
        } else if (i == 3) {
            this.result = 4;
        } else if (i == 4) {
            this.result = 5;
        }
    }

    public int getResult() {
        return this.result;
    }

    public String getExistingFile() {
        return this.existingFile;
    }

    public void setExistingFile(String str) {
        this.existingFile = str;
    }
}
