package ch.osiv.helper.filters;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFileFilter
    implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }

}
