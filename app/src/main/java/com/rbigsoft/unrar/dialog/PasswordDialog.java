package com.rbigsoft.unrar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.rbigsoft.sn.unzip.R;

public class PasswordDialog extends Dialog implements OnClickListener {
    /* access modifiers changed from: private */
    public boolean cancel = false;
    private EditText editPass;
    private String password = null;

    public PasswordDialog(Context context) {
        super(context, R.style.Dialog);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(3);
        setContentView(R.layout.password_diag);
        ((Button) findViewById(R.id.butPasswordOk)).setOnClickListener(this);
        ((Button) findViewById(R.id.butAnnulePass)).setOnClickListener(this);
        this.editPass = (EditText) findViewById(R.id.editTextPassword);
        setTitle(getContext().getString(R.string.password));
        setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                PasswordDialog.this.cancel = true;
            }
        });
    }

    public void onClick(View view) {
        if (view.getId() == R.id.butPasswordOk) {
            this.password = this.editPass.getText().toString();
            dismiss();
        } else if (view.getId() == R.id.butAnnulePass) {
            this.password = " ";
            this.cancel = true;
            dismiss();
        }
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isCancel() {
        return this.cancel;
    }
}
