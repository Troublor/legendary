package model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectFile extends File {
    public ProjectFile(String pathname) {
        super(pathname);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public ProjectFile[] listFiles() {
        File[] former = super.listFiles();
        if (former == null) {
            return null;
        }
        List<ProjectFile> files = new ArrayList<>();
        for (File file :
                former) {
            files.add((ProjectFile) file);
        }
        return (ProjectFile[])files.toArray();
    }
}
