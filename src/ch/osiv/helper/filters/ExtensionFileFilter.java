package ch.osiv.helper.filters;

import java.io.File;
import java.io.FilenameFilter;

public class ExtensionFileFilter
    implements FilenameFilter {

    String extension;

    public ExtensionFileFilter(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(File dir,
                          String name) {
        return name.endsWith(extension);
    }

}
