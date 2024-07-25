package ch.osiv.document.convert;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import ch.osiv.document.dms.DmsDocument;
import ch.osiv.helper.FileHelper;

/**
 * CombinePdfFilesComperator class
 *
 * @author Arno van der Ende
 */
public class CombinePdfFilesComperator
    implements Comparator<Path> {

    private List<DmsDocument> documents;

    /**
     * Constructor
     *
     * @param documents The document list which is needed for the comparison
     */
    public CombinePdfFilesComperator(List<DmsDocument> documents) {
        this.documents = documents;
    }

    /**
     * The compare method - for sorting with this rule:
     * 1. Paths which are NOT present in the documents list comes first, sorted by creationDate
     * 2. For all other documents, use the order as they are in the documents list
     *
     * @param path1 First path
     * @param path2 Second path
     * @return 0 when equals, 1 when first param is greater then second param, -1 when first param
     *         is less then second param
     */
    @Override
    public int compare(Path path1,
                       Path path2) {

        String path1BaseName = FileHelper.getBaseName(path1);
        String path2BaseName = FileHelper.getBaseName(path2);

        int path1InListIndex = getDocumentIndex(path1BaseName);
        int path2InListIndex = getDocumentIndex(path2BaseName);

        // when both paths are in the Document list, order by the list order
        if (path1InListIndex > -1 && path2InListIndex > -1) {
            return Integer.compare(path1InListIndex, path2InListIndex);
        }

        // when both are not in the list, then order by creation timestamp
        if (path1InListIndex == -1 && path2InListIndex == -1) {
            long createDatePath1 = FileHelper.getFileCreationDate(path1);
            long createDatePath2 = FileHelper.getFileCreationDate(path2);
            return Long.valueOf(createDatePath1).compareTo(createDatePath2);
        }

        // otherwise the file which are not in the documents list preceed the other file
        return (path1InListIndex == -1) ? -1
                                        : (path2InListIndex == -1) ? 1
                                                                   : 0;
    }

    /**
     * Returns the index in the documents list which equals the given document name
     *
     * @param documents    List of documents
     * @param documentName Document name to search for
     * @return The index number
     */
    private int getDocumentIndex(String documentName) {

        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getDocumentName() != null &&
                documents.get(i).getDocumentName().equals(documentName)) {
                return i;
            }
        }
        return -1;

    }

}
