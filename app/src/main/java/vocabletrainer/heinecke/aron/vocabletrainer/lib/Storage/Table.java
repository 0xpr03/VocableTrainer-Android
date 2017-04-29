package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

/**
 * Created by aron on 14.04.17.
 */

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;

/**
 * Table entry
 */
public class Table {
    private String nameA;
    private String nameB;
    private String name;
    private int totalVocs;
    private int unfinishedVocs;
    private int id;

    /**
     * Creates a new Table data object
     * @param id ID
     * @param nameA Name for A Column
     * @param nameB Name for B Column
     */
    public Table(final int id, final String nameA, final String nameB, final String name){
        this.id = id;
        this.nameA = nameA;
        this.nameB = nameB;
        this.name = name;
        this.totalVocs = MIN_ID_TRESHOLD-1;
        this.unfinishedVocs = MIN_ID_TRESHOLD-1;
    }

    /**
     * Create a new Table with none-ID -1
     * @param nameA Name for A Column
     * @param nameB Name for B Column
     */
    public Table(final String nameA, final String nameB, final String name){
        this(MIN_ID_TRESHOLD-1,nameA, nameB, name);
    }

    /**
     * Creates a new table data object<br>
     *     All fields except ID will be empty thens
     * @param id
     */
    public Table(final int id){
        this(id,null,null,null);
    }

    @Override
    public String toString(){
        return getId()+" "+getName()+" "+getNameA()+" "+getNameB();
    }


    /**
     * Tests for equality based on table ID
     * @param table
     * @return true when table IDs are equal
     */
    public boolean equals(Table table){
        return this.id == table.getId();
    }

    public void setNameA(String nameA) {
        this.nameA = nameA;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
    }

    /**
     * Returns the amount of vocables this table has<br>
     *     The value can be -1 when not set!
     * @return
     */
    public int getTotalVocs() {
        return totalVocs;
    }

    public void setTotalVocs(int total) {
        this.totalVocs = total;
    }

    /**
     * Retrns the amount of unfinished vocables<br>
     *     The value can be -1 when not set!
     * @return
     */
    public int getUnfinishedVocs() {
        return unfinishedVocs;
    }

    public void setUnfinishedVocs(int unfinished) {
        this.unfinishedVocs = unfinished;
    }

    /**
     * Set a new ID
     * @param id new ID
     * @throws IllegalAccessException if a valid ID is already set
     */
    public void setId(int id) {
        if(this.id < MIN_ID_TRESHOLD)
            this.id = id;
        else
            throw new IllegalAccessError("Can't override existing Table ID");
    }

    public String getNameA() {

        return nameA;
    }

    public String getNameB() {
        return nameB;
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
