package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

/**
 * Created by aron on 07.04.17.
 */

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;

/**
 * DB Entry
 */
public class Entry {
    private String AWord;
    private String BWord;
    private String tip;
    private final Table table;

    private int id;
    private int points;
    private long date;
    private boolean changed = false;
    private boolean delete = false;

    /**
     * Creates a new Entry
     *
     * @param AWord
     * @param BWord
     * @param tip
     * @param id     ID, list unique
     * @param table  List ID
     * @param points (training session break)
     * @param date   (last used)
     */
    public Entry(String AWord, String BWord, String tip, int id, Table table, int points, long date) {
        this.AWord = AWord;
        this.BWord = BWord;
        this.tip = tip;
        this.table = table;
        this.points = points;
        this.date = date;
        this.id = id;
    }

    /**
     * Creates a new Entry with 0 points
     *
     * @param AWord
     * @param BWord
     * @param tip
     * @param id
     * @param table
     */
    public Entry(String AWord, String BWord, String tip, int id, Table table, long date) {
        this(AWord, BWord, tip, id, table, 0, date);
    }

    /**
     * Creates a new Entry with 0 points, -1 ID
     *
     * @param AWord
     * @param BWord
     * @param tip
     * @param table
     */
    public Entry(String AWord, String BWord, String tip, Table table, long date) {
        this(AWord, BWord, tip, MIN_ID_TRESHOLD - 1, table, 0, date);
    }

    /**
     * Returns the A-Word
     *
     * @return
     */
    public String getAWord() {
        return AWord;
    }

    /**
     * Set A-Word
     *
     * @param AWord
     */
    public void setAWord(String AWord) {
        this.AWord = AWord;
        this.changed = true;
    }

    /**
     * Returns the B word
     *
     * @return
     */
    public String getBWord() {
        return BWord;
    }

    /**
     * Set B-Word
     *
     * @param BWord
     */
    public void setBWord(String BWord) {
        this.BWord = BWord;
        this.changed = true;
    }

    /**
     * Get Tip
     *
     * @return
     */
    public String getTip() {
        return tip;
    }

    /**
     * Set Tip
     *
     * @param tip
     */
    public void setTip(String tip) {
        this.tip = tip;
        this.changed = true;
    }

    @Override
    public String toString() {
        return AWord + " " + BWord+" "+points;
    }

    /**
     * Test for equality based on entry & table ID
     *
     * @param entry
     * @return
     */
    public boolean equals(Entry entry) {
        return this.getId() == entry.getId() && this.getTable() == entry.getTable();
    }

    /**
     * Returns whether the Data of this Entry was changed
     *
     * @return
     */
    public boolean isChanged() {
        return changed;
    }

    public Table getTable() {
        return table;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
