package com.rbigsoft.unrar.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.model.FileMimeCategorie;
import com.rbigsoft.unrar.model.document.Dossier;
import com.rbigsoft.unrar.model.document.Fichier;
import com.rbigsoft.unrar.model.document.FileSystemElement;
import com.rbigsoft.unrar.utilitaire.AvailableSpaceHandler;
import com.rbigsoft.unrar.utilitaire.FileUtil;

import java.io.File;
import java.util.List;

import static android.view.View.GONE;

public class DocumentImportAdapter extends DocumentAdapter {
    private View convertViewDossier = null;
    private View convertViewMime = null;
    private View convertViewRar = null;
    private List<FileSystemElement> elementsSelectionnes;
    private boolean selectFileCompress = true;
    private ClickCallBack callBack;

    public void setCallBack(ClickCallBack callBack) {
        this.callBack = callBack;
    }

    static class DossierHolder extends Holder {
        CheckBox chkBoxSelect;
        TextView nbrFichier;
        LinearLayout llClick;
        TextView nom;

        DossierHolder() {
        }
    }

    static class Holder {
        holderType type;

        Holder() {
        }
    }

    static class MimeHolder extends Holder {
        CheckBox chkBoxSelect;
        ImageView imgView;
        TextView nom;
        TextView tvFileNameAvatar;
        TextView txtFileSize;
        LinearLayout llClick;

        MimeHolder() {
        }
    }

    static class RarHolder extends Holder {
        CheckBox chkBox;
        TextView nom;
        TextView txtFileSize;

        RarHolder() {
        }
    }

    enum holderType {
        Rar,
        Mime,
        Dossier
    }

    public DocumentImportAdapter(Dossier dossier, Context context, List<FileSystemElement> list) {
        super(dossier, context);
        this.elementsSelectionnes = list;
    }

    public int getCount() {
        return super.getCount();
    }

    public Object getItem(int i) {
        return this.dossierCourant.getContenu().get(i);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        MimeHolder mimeHolder;
        RarHolder rarHolder;
        DossierHolder dossierHolder;
        FileSystemElement fileSystemElement = (FileSystemElement) this.dossierCourant.getContenu().get(i);
        if (fileSystemElement instanceof Dossier) {
            if (view == null || ((Holder) view.getTag()).type != holderType.Dossier) {
                view = LayoutInflater.from(this.context).inflate(R.layout.sous_dossier_item, null, false);
                dossierHolder = new DossierHolder();


                dossierHolder.nom = (TextView) view.findViewById(R.id.textViewNomSousDossier);
                dossierHolder.nbrFichier = (TextView) view.findViewById(R.id.textViewNbrFichierItem);
                dossierHolder.chkBoxSelect = (CheckBox) view.findViewById(R.id.checkBoxSousDossierItem);
                view.setTag(dossierHolder);
                dossierHolder.type = holderType.Dossier;
            } else {
                dossierHolder = (DossierHolder) view.getTag();
            }
            dossierHolder.nom.setText(fileSystemElement.getNom());
            dossierHolder.nbrFichier.setText(String.valueOf(((Dossier) fileSystemElement).getNbrFile()));
            dossierHolder.chkBoxSelect.setTag(Integer.valueOf(i));
            if (elementIsCheck((FileSystemElement) getItem(i))) {
                dossierHolder.chkBoxSelect.setChecked(true);
            } else {
                dossierHolder.chkBoxSelect.setChecked(false);
            }
            if (this.selectFileCompress) {
                dossierHolder.chkBoxSelect.setVisibility(0);
            } else {
                dossierHolder.chkBoxSelect.setVisibility(8);
            }
            return view;
        }
        if (this.selectFileCompress || !(FileUtil.getMimeCategorie(fileSystemElement.getNom()) == FileMimeCategorie.rar || FileUtil.getMimeCategorie(fileSystemElement.getNom()) == FileMimeCategorie.zip || FileUtil.getMimeCategorie(fileSystemElement.getNom()) == FileMimeCategorie.sevenZip)) {
            if (view == null || ((Holder) view.getTag()).type != holderType.Mime) {
                view = LayoutInflater.from(this.context).inflate(R.layout.fichier_import_item_mime, null);
                mimeHolder = new MimeHolder();
                mimeHolder.tvFileNameAvatar = view.findViewById(R.id.tv_name_file_avatar);
                mimeHolder.nom = (TextView) view.findViewById(R.id.textViewNomFichierImportMime);
                mimeHolder.txtFileSize = (TextView) view.findViewById(R.id.textViewFileSizeMime);
                mimeHolder.imgView = (ImageView) view.findViewById(R.id.imageViewMime);
                mimeHolder.chkBoxSelect = (CheckBox) view.findViewById(R.id.checkBoxMimeItem);
                view.setTag(mimeHolder);
                mimeHolder.type = holderType.Mime;
            } else {
                mimeHolder = (MimeHolder) view.getTag();
            }

            setMimeImageView(mimeHolder.tvFileNameAvatar, mimeHolder.imgView, fileSystemElement.getChemin(), fileSystemElement.getNom());
            mimeHolder.txtFileSize.setText(AvailableSpaceHandler.buildTextFileSize(((Fichier) fileSystemElement).getFileSize(), this.context));
            mimeHolder.nom.setText(fileSystemElement.getNom());
            mimeHolder.chkBoxSelect.setTag(Integer.valueOf(i));

            if (elementIsCheck((FileSystemElement) getItem(i))) {
                mimeHolder.chkBoxSelect.setChecked(true);
            } else {
                mimeHolder.chkBoxSelect.setChecked(false);
            }
            if (this.selectFileCompress) {
                mimeHolder.chkBoxSelect.setVisibility(0);
            } else {
                mimeHolder.chkBoxSelect.setVisibility(8);
            }
        }
        return view;
    }

    private boolean elementIsCheck(FileSystemElement fileSystemElement) {
        if (!(fileSystemElement == null || fileSystemElement.getChemin() == null)) {
            for (FileSystemElement fileSystemElement2 : this.elementsSelectionnes) {
                if (fileSystemElement2 != null && fileSystemElement2.getChemin() != null && fileSystemElement.getChemin().equals(fileSystemElement2.getChemin())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setMimeImageView(TextView tvAvatar, ImageView imageView, String filePath, String str) {
        Log.e("asdfafeef", str);
        switch (FileUtil.getMimeCategorie(str)) {
            case doc:
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("DOC");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case excel:
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("EX");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case image:
                tvAvatar.setVisibility(GONE);
                Glide.with(context).load(filePath).into(imageView);
                break;
            case music:
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("MP3");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_mp3_avatar));

                break;
            case pdf:

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("PDF");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case ppt:

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("PPT");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_ppt_avatar));

                break;
            case txt:

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("TXT");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case video:

                tvAvatar.setVisibility(GONE);
                Glide.with(context).load(filePath).into(imageView);
                break;
            case sevenZip:

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("7z");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_avatar_file));

                break;
            case zip:

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("ZIP");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_avatar_file));

                break;
            case rar:
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_avatar_file));

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("RAR");

                break;
            case apk:
                tvAvatar.setVisibility(GONE);
                try {
                    File file = new File(filePath);
                    String sourcePath = file.getPath();
                    PackageInfo packageInfo = context.getPackageManager()
                            .getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                    if (packageInfo != null) {
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        if (Build.VERSION.SDK_INT >= 8) {
                            appInfo.sourceDir = sourcePath;
                            appInfo.publicSourceDir = sourcePath;
                        }
                        Drawable icon = appInfo.loadIcon(context.getPackageManager());
                        Bitmap bmpIcon = ((BitmapDrawable) icon).getBitmap();
                        if (bmpIcon == null) {
                            Log.e("bitmapApk", "null");
                        }
                        imageView.setImageBitmap(bmpIcon);

                    }
                } catch (Exception e) {

                }
                break;
            default:

                tvAvatar.setVisibility(GONE);
                imageView.setImageResource(R.drawable.ic_file_default);

                break;
        }
    }

    public boolean isSelectFileCompress() {
        return this.selectFileCompress;
    }

    public void setSelectFileCompress(boolean z) {
        this.selectFileCompress = true;
    }

    private Bitmap generateImageFromPdf(Uri pdfUri) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);

            pdfiumCore.closeDocument(pdfDocument); // important!
            return bmp;
        } catch (Exception e) {
            //todo with exception
        }
        return null;
    }

    public interface ClickCallBack {
        void clickItemCustom(View view, int position);

        boolean longClickItemCustom(View view, int position);
    }
}
