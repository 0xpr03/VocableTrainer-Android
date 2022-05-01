package vocabletrainer.heinecke.aron.vocabletrainer.eximport;

/**
 * Generic spinner entry
 *
 * @param <T> Object type for value of element
 */
public class GenericSpinnerEntry<T> {
    private T object;
    private String displayText;

    /**
     * Create a new GenericSpinnerEntry
     *
     * @param object      Object bound with this selection
     * @param displayText Text to display for this element
     */
    public GenericSpinnerEntry(T object, String displayText) {
        this.object = object;
        this.displayText = displayText;
    }

    /**
     * Update object value
     * @param newValue
     */
    public void updateObject(T newValue) {
        object = newValue;
    }

    /**
     * Returns text to display for element
     *
     * @return
     */
    private String getDisplayText() {
        return displayText;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }

    /**
     * Returns object bound by this element
     *
     * @return
     */
    public T getObject() {
        return object;
    }
}
