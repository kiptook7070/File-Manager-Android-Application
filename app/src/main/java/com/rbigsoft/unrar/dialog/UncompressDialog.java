package com.rbigsoft.unrar.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.sn.unzip.activity.AppDatabaseSQL;
import com.rbigsoft.sn.unzip.activity.FileActivity;
import com.rbigsoft.unrar.nativeinterface.Suspendable;

import java.io.File;
import java.util.ArrayList;

public class UncompressDialog extends Dialog implements OnClickListener {
    private Button butStopOk;
    private String compressDialogTitle = "";
    /* access modifiers changed from: private */
    public Suspendable extractAsync;
    /* access modifiers changed from: private */
    public boolean finished = false;
    private ProgressBar progress;
    private TextView textViewPourcent;
    private AppDatabaseSQL appDatabaseSQL;

    public UncompressDialog(Context context, Suspendable suspendable, String str) {
        super(context, R.style.Dialog);
        this.extractAsync = suspendable;
        this.compressDialogTitle = str;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(3);
        appDatabaseSQL = new AppDatabaseSQL(getContext());
        setContentView(R.layout.uncompress_diag);
        this.progress = (ProgressBar) findViewById(R.id.progressBarUncompress);
        progress.setMax(100);
        setTitle(this.compressDialogTitle);
        this.textViewPourcent = (TextView) findViewById(R.id.textViewPourcent);
        this.butStopOk = (Button) findViewById(R.id.butStopOk);
        this.butStopOk.setOnClickListener(this);
        this.butStopOk.setText(getContext().getString(R.string.arrete));
        setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                if (!UncompressDialog.this.finished) {
                    UncompressDialog.this.extractAsync.setSuspended(true);
                }
            }
        });
    }

    public void setProgressValue(int i) {
        this.progress.setProgress(i);
        TextView textView = this.textViewPourcent;
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(i));
        progress.setProgress(i);
        sb.append(" %");
        textView.setText(sb.toString());
    }

    public void getAction(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        getContext().sendBroadcast(intent);
    }

    public void finish(String path) {
        setProgressValue(100);
        setTitle(getContext().getString(R.string.finish));
        this.butStopOk.setText(getContext().getString(R.string.retour));
        this.finished = true;


        if (compressDialogTitle.contains("Zip")) {
            getAction(FileActivity.ACTION_COMPRESS);
            Toast.makeText(getContext(), "" + getContext().getString(R.string.finish), Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            getAction(FileActivity.ACTION_EXTRA);
            Toast.makeText(getContext(), "" + getContext().getString(R.string.finish), Toast.LENGTH_SHORT).show();
            dismiss();
        }
        Log.e("filePathFinish", path + "");
        if (!path.equals("")) {
            boolean check = false;
            ArrayList<String> arrayList = appDatabaseSQL.getAllHistory();
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).equals(path)) {
                    check = true;
                }
            }
            if (check == false) {
                long x = appDatabaseSQL.insertSectionUser(path);
                if (x != -1) {
                    Log.e("checkInsert", "insert finish");
                } else {
                    Log.e("checkInsert", "insert false");
                }
            }
        }

    }

    public void onClick(View view) {
        if (view.getId() != R.id.butStopOk) {
            return;
        }
        if (!this.finished) {
            this.extractAsync.setSuspended(true);
            setTitle(getContext().getString(R.string.arret_en_cours));
        } else if (isShowing()) {
            dismiss();
        }
    }
}
