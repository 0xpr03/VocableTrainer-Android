package vocabletrainer.heinecke.aron.vocabletrainer.lib;

/**
 * Simple pre API level 24 re-implementation of java.util.function.Function
 * @param <R> return type
 * @param <P> param type
 */
public interface Function<R,P> {
    /**
     * Function to pass
     * @param param Param of function
     * @return Return value
     */
    R function(P param);
}
