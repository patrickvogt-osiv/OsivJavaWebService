package ch.osiv.document;

import ch.osiv.helper.JsonSerializable;

/**
 * ObjectTypeInfo class
 *
 * @author Arno van der Ende
 */
public class ObjectTypeInfo
    extends JsonSerializable {

    private int    id;
    private String name;

    /**
     * Getter for id - Unique key
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for id
     *
     * @param id The id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for name - Object type name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
