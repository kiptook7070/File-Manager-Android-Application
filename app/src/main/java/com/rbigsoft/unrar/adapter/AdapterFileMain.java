package com.rbigsoft.unrar.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.model.File_Related_Information;
import com.rbigsoft.unrar.utilitaire.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static android.view.View.GONE;

public class AdapterFileMain extends RecyclerView.Adapter<AdapterFileMain.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<File_Related_Information> arrayList;
    private ClickItemCallBack callBack;

    public void setCallBack(ClickItemCallBack callBack) {
        this.callBack = callBack;
    }

    public AdapterFileMain(Context context, ArrayList<File_Related_Information> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_file_main, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File_Related_Information file_related_information = arrayList.get(position);
        holder.binData(file_related_information);
        if (callBack != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callBack.logClickItem(position, holder.itemView);
                    return false;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.clickItem(position);
                }
            });

            holder.cbCheckFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    callBack.checkItem(position, isChecked);
                    if (isChecked == true) {
                        holder.cbCheckFile.setChecked(true);
                    } else {
                        holder.cbCheckFile.setChecked(false);
                    }
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView tvAvatar, tvName, tvDescription;
        private CheckBox cbCheckFile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvAvatar = itemView.findViewById(R.id.tv_name_file_avatar);
            tvName = itemView.findViewById(R.id.tv_name_file);
            tvDescription = itemView.findViewById(R.id.tv_description_file);
            cbCheckFile = itemView.findViewById(R.id.cb_check_file);
        }

        public void binData(File_Related_Information itemFile) {
            setMimeImageView(tvAvatar, imgAvatar, itemFile.getFilePath(), itemFile.getFileName());
            tvName.setText(itemFile.getFileName());
            try {
                String dateString = DateFormat.format("dd/MM/yyyy", new Date(Long.parseLong(itemFile.getFileDate()))).toString();
                tvDescription.setText(dateString);
            } catch (Exception e) {
                tvDescription.setText(itemFile.getFileDate());
            }
            if (itemFile.isSelected()) {
                cbCheckFile.setChecked(true);
            } else {
                cbCheckFile.setChecked(false);
            }
        }
    }


    private void setMimeImageView(TextView tvAvatar, ImageView imageView, String filePath, String str) {
        switch (FileUtil.getMimeCategorie(str)) {
            case doc:
                imageView.setVisibility(GONE);

                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("DOC");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case excel:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("EX");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case image:
                tvAvatar.setVisibility(GONE);
                Glide.with(context).load(filePath).into(imageView);
                break;
            case music:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("MP3");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_mp3_avatar));

                break;
            case pdf:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("PDF");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case ppt:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("PPT");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_ppt_avatar));

                break;
            case txt:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("TXT");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_doc_avatar));

                break;
            case video:

                tvAvatar.setVisibility(GONE);
                Glide.with(context).load(filePath).into(imageView);
                break;
            case sevenZip:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("7z");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_avatar_file));

                break;
            case zip:
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("ZIP");
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_avatar_file));

                break;
            case rar:
                tvAvatar.setBackground(context.getResources().getDrawable(R.drawable.custom_ll_avatar_file));
                imageView.setVisibility(GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText("RAR");

                break;
            case apk:
                imageView.setVisibility(View.VISIBLE);
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
                imageView.setVisibility(View.VISIBLE);
                tvAvatar.setVisibility(GONE);
                imageView.setImageResource(R.drawable.ic_file_default);

                break;
        }
    }

    public interface ClickItemCallBack {
        void clickItem(int position);

        void checkItem(int position, boolean check);

        void logClickItem(int position, View view);
    }
}
