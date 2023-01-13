package com.rbigsoft.unrar.model.archive;

import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.util.InternalZipConstants;

public class ArchiveDossier extends ArchiveElement {
    private List<ArchiveElement> listeElement = new ArrayList();

    public ArchiveDossier(String str) {
        super(str);
    }

    public ArchiveElement addElement(ArchiveElement archiveElement) {
        if (!exist(archiveElement)) {
            archiveElement.setParent(this);
            StringBuilder sb = new StringBuilder();
            sb.append(this.chemin);
            sb.append(InternalZipConstants.ZIP_FILE_SEPARATOR);
            sb.append(archiveElement.getNom());
            archiveElement.chemin = sb.toString();
            this.listeElement.add(archiveElement);
            return archiveElement;
        }
        for (ArchiveElement archiveElement2 : this.listeElement) {
            if (archiveElement2.getNom().equals(archiveElement.getNom())) {
                return archiveElement2;
            }
        }
        return null;
    }

    public List<ArchiveElement> getRecusiveAllFile() {
        ArrayList arrayList = new ArrayList();
        for (ArchiveElement archiveElement : this.listeElement) {
            if (archiveElement instanceof ArchiveFichier) {
                arrayList.add((ArchiveFichier) archiveElement);
            } else if (archiveElement instanceof ArchiveDossier) {
                arrayList.add(archiveElement);
                arrayList.addAll(((ArchiveDossier) archiveElement).getRecusiveAllFile());
            }
        }
        return arrayList;
    }

    private boolean exist(ArchiveElement archiveElement) {
        for (ArchiveElement nom : this.listeElement) {
            if (nom.getNom().equals(archiveElement.getNom())) {
                return true;
            }
        }
        return false;
    }

    public ArchiveDossier getEnfantFromNom(String str) {
        for (ArchiveElement archiveElement : this.listeElement) {
            if (archiveElement.getNom().equals(str) && (archiveElement instanceof ArchiveDossier)) {
                return (ArchiveDossier) archiveElement;
            }
        }
        return null;
    }

    public int getCount() {
        return this.listeElement.size();
    }

    public ArchiveElement getElement(int i) {
        return (ArchiveElement) this.listeElement.get(i);
    }
}
