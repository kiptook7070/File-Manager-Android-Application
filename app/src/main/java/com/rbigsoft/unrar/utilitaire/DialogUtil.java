package com.rbigsoft.unrar.utilitaire;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.rbigsoft.sn.unzip.R;


public class DialogUtil {
    public static AlertDialog buildErrorCRCDiag(Activity activity) {
        return new Builder(activity).setTitle(activity.getString(R.string.erreur)).setPositiveButton(R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(activity.getString(R.string.crc_invalide)).create();
    }

    public static AlertDialog deleteDiag(Context context, OnClickListener onClickListener) {
        return new Builder(context).setTitle(context.getString(R.string.supprimer_fichier)).setPositiveButton(R.string.ok, onClickListener).setNegativeButton(R.string.annuler, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(context.getString(R.string.confirmation_suppr)).create();
    }

    public static AlertDialog annuleDiag(Context context, OnClickListener onClickListener) {
        return new Builder(context).setTitle(context.getString(R.string.annuler)).setPositiveButton(R.string.ok, onClickListener).setNegativeButton(R.string.annuler, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(context.getString(R.string.mess_annule_compress)).create();
    }

    public static AlertDialog errorBuildZip(Activity activity, String str) {
        return new Builder(activity).setTitle(activity.getString(R.string.erreur)).setPositiveButton(R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setMessage(str).create();
    }
}
