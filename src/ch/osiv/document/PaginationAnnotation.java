package ch.osiv.document;

import org.w3c.dom.NodeList;

/**
 * PaginationAnnotation class
 *
 * @author arnovanderende
 */
public class PaginationAnnotation
    extends RubberStampAnnotation {

    /**
     * Constructor
     *
     * @param annObjectChildList
     * @param scalingFactor
     */
    public PaginationAnnotation(NodeList annObjectChildList,
                                Double scalingFactor) {

        super(annObjectChildList, scalingFactor);

    }

}
