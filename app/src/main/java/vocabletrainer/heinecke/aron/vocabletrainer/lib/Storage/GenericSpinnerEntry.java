package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import android.media.CamcorderProfile;

/**
 * Generic spinner entry holder
 * @param <T> Object type for value of element
 */
public class GenericSpinnerEntry<T> {
    T object;
    String displayText;

    /**
     * Create a new GenericSpinnerENtry
     * @param object Object bound with this selection
     * @param displayText Text to display for this element
     */
    public GenericSpinnerEntry(T object, String displayText) {
        this.object = object;
        this.displayText = displayText;
    }

    /**
     * Returns text to display for element
     * @return
     */
    public String getDisplayText(){
        return displayText;
    }

    @Override
    public String toString(){
        return getDisplayText();
    }

    /**
     * Returns object bound by this element
     * @return
     */
    public T getObject(){
        return object;
    }
}
