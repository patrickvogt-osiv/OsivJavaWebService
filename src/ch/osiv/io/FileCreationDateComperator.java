package ch.osiv.io;

import java.nio.file.Path;
import java.util.Comparator;

import ch.osiv.helper.FileHelper;

public class FileCreationDateComperator
    implements Comparator<Path> {

    /**
     * The compare method - for sorting by creation date
     *
     * @param path1 First path
     * @param path2 Second path
     * @return -1, 0 or 1
     */
    @Override
    public int compare(Path path1,
                       Path path2) {

        long createDatePath1 = FileHelper.getFileCreationDate(path1);
        long createDatePath2 = FileHelper.getFileCreationDate(path2);

        return Long.valueOf(createDatePath1).compareTo(createDatePath2);
    }
}
