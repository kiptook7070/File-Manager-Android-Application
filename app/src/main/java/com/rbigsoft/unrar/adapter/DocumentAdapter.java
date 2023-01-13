package com.rbigsoft.unrar.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.rbigsoft.unrar.model.document.Dossier;

public class DocumentAdapter extends BaseAdapter {
    protected Context context;
    protected Dossier dossierCourant;
    public long getItemId(int i) {
        return (long) i;
    }
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    public DocumentAdapter(Dossier dossier, Context context2) {
        this.dossierCourant = dossier;
        this.context = context2;
    }
    public int getCount() {
        return this.dossierCourant.getContenu().size();
    }

    public Object getItem(int i) {
        return this.dossierCourant.getContenu().get(i);
    }

    public Dossier getDossierCourant() {
        return this.dossierCourant;
    }

    public void setDossierCourant(Dossier dossier) {
        this.dossierCourant = dossier;
    }
}
