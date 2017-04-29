package vocabletrainer.heinecke.aron.vocabletrainer.lib;

/**
 * Created by aron on 07.04.17.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.Entry;

/**
 * Trainer class
 */
public class Trainer {
    //TODO: Android Logger
    public enum TEST_MODE {A, B,RANDOM};
    private Random rng;
    private enum AB_MODE {A,B};
    private AB_MODE order;
    private List<Entry> vocables;
    private List<Entry> solved;
    private Entry cVocable;
    private int tips;
    private int failed;
    private int timesToSolve;

    /**
     * Creates a new Trainer
     * @param vocables Vocables to use
     * @param settings Trainer settings storage
     */
    public Trainer(final List<Entry> vocables, final TrainerSettings settings){
        if(vocables == null || vocables.size() == 0)
            throw new IllegalArgumentException("Invalid vocable list");
        this.tips = settings.tipsGiven;
        this.failed = settings.timesFailed;
        this.vocables = vocables;
        this.timesToSolve = settings.timesToSolve;
        sortVocable();
        rng = new Random();
    }

    /**
     * Initial sorting of completed / missing vocables
     */
    private void sortVocable(){
//          java 8
//        this.solved = vocables.stream().filter(f -> f.getPoints() >= timesToSolve).collect(Collectors.toList());
//        vocables.removeAll(solved);
        solved = new ArrayList<>();
        for(Iterator<Entry> iterator = vocables.iterator(); iterator.hasNext();){
            Entry elem = iterator.next();
            if(elem.getPoints() >= timesToSolve){
                iterator.remove();
                solved.add(elem);
            }
        }
    }

    /**
     * Returns the solution of the current vocable
     * @return
     */
    public String getSolution(){
        if(this.cVocable == null)
            //TODO: logger
            return "";
        this.failed++;
        return getSolutionUnchecked();
    }

    /**
     * Returns the solution<br>
     *     No null checks are done or failed counter changes are made
     * @return Solution
     */
    private String getSolutionUnchecked(){
        if(order == AB_MODE.A)
            return cVocable.getAWord();
        else
            return cVocable.getBWord();
    }

    /**
     * Checks for correct solution
     * @param tSolution
     * @return true on success
     */
    public boolean checkSolution(String tSolution){
        if (this.cVocable == null)
            //TODO: log it
            return false;
        boolean bSolved;
        if(bSolved = getSolutionUnchecked().equals(tSolution)){
            this.cVocable.setPoints(this.cVocable.getPoints() +1 );
            if(cVocable.getPoints() >= timesToSolve) {
                vocables.remove(cVocable);
                solved.add(cVocable);
            }
            getNext();
        }else{
            this.failed++;
        }
        return bSolved;
    }

    private void getNext(){

    }

    /**
     * Returns the tip, increasing the counter
     * @return
     */
    public String getTip(){
        if(this.cVocable == null)
            return "";

        this.tips++;
        return cVocable.getTip();
    }

    /**
     *
     * @return remaining vocables
     */
    public int remaining(){
        return vocables.size();
    }

    /**
     *
     * @return solved vocables
     */
    public int solved(){
        return solved.size();
    }
}
