package com.rbigsoft.unrar.utilitaire;

import android.util.Log;
import com.rbigsoft.unrar.model.ArchiveFileString;
import com.rbigsoft.unrar.model.archive.ArchiveDossier;
import com.rbigsoft.unrar.model.archive.ArchiveFichier;
import java.util.List;
import net.lingala.zip4j.util.InternalZipConstants;

public class ArchiveStrToArchiveDossier {
    private ArchiveDossier root;

    public ArchiveDossier toArchiveDossier(List<ArchiveFileString> list) {
        this.root = new ArchiveDossier("ROOT");
        if (list != null) {
            for (ArchiveFileString archiveFileString : list) {
                checkIsFolderForRar(archiveFileString, list);
                if (archiveFileString.isDir()) {
                    creerHierarchieDossier(archiveFileString);
                }
            }
            for (ArchiveFileString archiveFileString2 : list) {
                if (!archiveFileString2.isDir()) {
                    addFile(archiveFileString2);
                }
            }
        }
        return this.root;
    }

    public ArchiveDossier toArchiveDossierZip(List<ArchiveFileString> list) {
        this.root = new ArchiveDossier("ROOT");
        if (list != null) {
            for (ArchiveFileString archiveFileString : list) {
                if (!archiveFileString.isDir()) {
                    creerHierarchieDossierZip(archiveFileString);
                } else {
                    creerHierarchieDossier(archiveFileString);
                }
            }
            for (ArchiveFileString archiveFileString2 : list) {
                if (!archiveFileString2.isDir()) {
                    addFile(archiveFileString2);
                }
            }
        }
        return this.root;
    }

    private void checkIsFolderForRar(ArchiveFileString archiveFileString, List<ArchiveFileString> list) {
        if (archiveFileString.getTaille() == 0 && !archiveFileString.isDir()) {
            for (ArchiveFileString archiveFileString2 : list) {
                if (!(archiveFileString2 == archiveFileString || archiveFileString2.getNom() == null || archiveFileString.getNom() == null)) {
                    String nom = archiveFileString2.getNom();
                    StringBuilder sb = new StringBuilder();
                    sb.append(archiveFileString.getNom());
                    sb.append(InternalZipConstants.ZIP_FILE_SEPARATOR);
                    if (nom.contains(sb.toString())) {
                        archiveFileString.setDir(true);
                        return;
                    }
                }
            }
        }
    }

    private void addFile(ArchiveFileString archiveFileString) {
        String[] split = archiveFileString.getNom().split(InternalZipConstants.ZIP_FILE_SEPARATOR);
        ArchiveDossier archiveDossier = this.root;
        int length = split.length;
        int i = 0;
        if (length == 1) {
            ArchiveFichier archiveFichier = new ArchiveFichier(split[0]);
            archiveFichier.setTaille(archiveFileString.getTaille());
            this.root.addElement(archiveFichier);
            return;
        }
        while (i < length - 1) {
            archiveDossier = archiveDossier.getEnfantFromNom(split[i]);
            if (archiveDossier == null) {
                archiveDossier = this.root;
            }
            i++;
        }
        ArchiveFichier archiveFichier2 = new ArchiveFichier(split[i]);
        archiveFichier2.setTaille(archiveFileString.getTaille());
        if (archiveDossier != null) {
            archiveDossier.addElement(archiveFichier2);
        } else {
            Log.e(ArchiveStrToArchiveDossier.class.getName(), "Erreur parent null");
        }
    }

    private void creerHierarchieDossier(ArchiveFileString archiveFileString) {
        String[] split = archiveFileString.getNom().split(InternalZipConstants.ZIP_FILE_SEPARATOR);
        ArchiveDossier archiveDossier = this.root;
        for (String archiveDossier2 : split) {
            ArchiveDossier archiveDossier3 = new ArchiveDossier(archiveDossier2);
            if (archiveDossier != null) {
                archiveDossier = (ArchiveDossier) archiveDossier.addElement(archiveDossier3);
            }
        }
    }

    private void creerHierarchieDossierZip(ArchiveFileString archiveFileString) {
        String[] split = archiveFileString.getNom().split(InternalZipConstants.ZIP_FILE_SEPARATOR);
        if (split.length > 1) {
            ArchiveDossier archiveDossier = this.root;
            for (int i = 0; i < split.length - 1; i++) {
                ArchiveDossier archiveDossier2 = new ArchiveDossier(split[i]);
                if (archiveDossier != null) {
                    archiveDossier = (ArchiveDossier) archiveDossier.addElement(archiveDossier2);
                }
            }
        }
    }
}
