package vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer;

import java.util.Collection;
import java.util.List;

/**
 * Input element for the trainer
 */
public interface TrainerInput {
    enum INPUT_STATE {
        VALID,INVALID,DUPLICATE
    }
    /**
     * Get data of input
     * @return Input data to verify<br>
     *     The order of elements is expected to correlate with the no for setInputState
     */
    List<String> getData();

    /**
     * Set state of input
     * @param input Input no
     * @param newState New state
     */
    void setInputState(int input, INPUT_STATE newState);

    /**
     * Set input to specified value
     * @param input Input no
     * @param newValue New Value
     */
    void setInputValue(int input, String newValue);

    /**
     * Set amount of input elements
     * @param newAmount New Amount
     */
    void setAmountInputs(int newAmount);
}
