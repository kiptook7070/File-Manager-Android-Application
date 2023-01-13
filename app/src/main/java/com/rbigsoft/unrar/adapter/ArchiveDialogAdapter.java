package com.rbigsoft.unrar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.model.archive.ArchiveDossier;
import com.rbigsoft.unrar.model.archive.ArchiveElement;
import com.rbigsoft.unrar.model.archive.ArchiveFichier;
import com.rbigsoft.unrar.utilitaire.AvailableSpaceHandler;

import java.util.List;

public class ArchiveDialogAdapter extends BaseAdapter {
    private OnCheckedChangeListener checkChange;
    private Context context;
    private ArchiveDossier dossier;
    private List<ArchiveElement> elementSelect;


    class ArchiveitemHolder {
        public CheckBox chkBox;
        public TextView txtViewNom;
        public TextView txtViewSize;
        private TextView tvAvatars;

        ArchiveitemHolder() {
        }
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public ArchiveDialogAdapter(Context context2, ArchiveDossier archiveDossier, OnCheckedChangeListener onCheckedChangeListener, List<ArchiveElement> list) {
        this.context = context2;
        this.dossier = archiveDossier;
        this.checkChange = onCheckedChangeListener;
        this.elementSelect = list;
    }

    public int getCount() {
        return this.dossier.getCount();
    }

    public ArchiveElement getItem(int i) {
        return this.dossier.getElement(i);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ArchiveitemHolder archiveitemHolder;
        ArchiveElement item = getItem(i);
        if (view == null || view.getTag() == null) {
            view = LayoutInflater.from(this.context).inflate(R.layout.archive_file, null);
            TextView tvAvatar = view.findViewById(R.id.tv_avatar);

            TextView textView = (TextView) view.findViewById(R.id.textViewFileArchiveName);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBoxArchiveFile);
            TextView textView2 = (TextView) view.findViewById(R.id.textViewFileArchiveSize);
            ArchiveitemHolder archiveitemHolder2 = new ArchiveitemHolder();
            archiveitemHolder2.txtViewNom = textView;
            archiveitemHolder2.tvAvatars = tvAvatar;
            archiveitemHolder2.txtViewSize = textView2;
            archiveitemHolder2.chkBox = checkBox;
            view.setTag(archiveitemHolder2);
            archiveitemHolder = archiveitemHolder2;
        } else {
            archiveitemHolder = (ArchiveitemHolder) view.getTag();
        }
        archiveitemHolder.txtViewNom.setText(item.getNom());
        if (item.getNom().contains(".")) {
            String tx = item.getNom();
            int index = item.getNom().lastIndexOf(".");
            String fileType = tx.substring(index + 1);
            archiveitemHolder.tvAvatars.setText(fileType);
        }
        archiveitemHolder.chkBox.setTag(Integer.valueOf(i));
        archiveitemHolder.chkBox.setOnCheckedChangeListener(this.checkChange);
        archiveitemHolder.chkBox.setChecked(elementIsCheck(item));
        if (item instanceof ArchiveFichier) {
            archiveitemHolder.txtViewSize.setText(AvailableSpaceHandler.buildTextFileSize(((ArchiveFichier) item).getTaille(), this.context));
            archiveitemHolder.txtViewSize.setVisibility(0);
        } else {
            archiveitemHolder.txtViewSize.setVisibility(4);
        }
        return view;
    }

    private boolean elementIsCheck(ArchiveElement archiveElement) {
        if (!(archiveElement == null || archiveElement.getChemin() == null)) {
            for (ArchiveElement archiveElement2 : this.elementSelect) {
                if (archiveElement2 != null && archiveElement2.getChemin() != null && archiveElement.getChemin().equals(archiveElement2.getChemin())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setDossier(ArchiveDossier archiveDossier) {
        this.dossier = archiveDossier;
    }

    public void checkAll(boolean z) {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            ((CheckBox) getView(i, null, null).findViewById(R.id.checkBoxArchiveFile)).setChecked(z);
        }
    }
}
