package vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Database.MIN_ID_TRESHOLD;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.readParcableBool;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.readParcableDate;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.writeParcableBool;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.ParcableTools.writeParcableDate;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList.isIDValid;

/**
 * DB Vocable Entry
 */
@SuppressWarnings("JavaDoc")
public class VEntry implements Serializable, Parcelable {
    private final VList list;
    private String tip;
    private String addition;
    private int id;
    private int points;
    private Date last_used;
    private Date created;
    private int correct;
    private int wrong;
    private List<String> meaningA;
    private List<String> meaningB;
    private boolean changed = false;
    private boolean delete = false;
    private static final String CONCAT = "/";


    /**
     * Creates a new VEntry with invalid ID & empty fields
     * @param list
     */
    public VEntry(@NonNull final VList list) {
        this(new LinkedList<>(),new LinkedList<>(),"","",list);
    }

    /**
     * Creates a new VEntry
     *
     * @param meaningA
     * @param meaningB
     * @param tip
     * @param addition
     * @param id
     * @param list
     * @param points
     * @param last_used
     * @param created
     * @param correct
     * @param wrong
     */
    public VEntry(@NonNull List<String> meaningA,@NonNull List<String> meaningB,@Nullable String tip,@Nullable String addition,
                  int id, @NonNull VList list, int points, @Nullable Date last_used,@NonNull Date created, int correct, int wrong) {
        this.meaningA = meaningA;
        this.meaningB = meaningB;
        this.tip = tip;
        this.list = list;
        this.points = points;
        this.created = created;
        this.last_used = last_used;
        this.correct = correct;
        this.wrong = wrong;
        this.addition = addition;
        this.id = id;
    }

    /**
     * Creates a new VEntry with 0 points
     *
     * @param meaningA
     * @param meaningB
     * @param tip
     * @param addition
     * @param id
     * @param list
     * @param last_used
     * @param created
     * @param correct
     * @param wrong
     */
    public VEntry(@NonNull List<String> meaningA,@NonNull List<String> meaningB,@Nullable String tip,@Nullable String addition,
            int id,@NonNull VList list,@Nullable Date last_used,@NonNull Date created, int correct, int wrong) {
        this(meaningA,meaningB,tip,addition,id,list,0,last_used,created,correct,wrong);
    }

    /**
     * Creates a 1:1 entry for spacers etc
     *  throw an IllegalArgumentException if ID should be valid
     * @param A meaning A entry
     * @param B meaning B entry
     * @param fID fake ID, has to be invalid
     * @param tip
     */
    public VEntry(String A, String B,String tip, int fID){
        this(new ArrayList<>(1),new ArrayList<>(1),tip,"",fID,null,0,null,new Date(0),0,0);
        if(isIDValid(fID)){
            throw new IllegalArgumentException("no valid ID allowed!");
        }
        meaningA.add(A);
        meaningB.add(B);
    }

    /**
     * Creates a 1:1 VEntry for Importing with an invalid ID
     *
     * @param A
     * @param B
     * @param tip
     * @param addition
     * @param list
     */
    public VEntry(String A, String B, String tip, String addition, VList list) {
        this(new ArrayList<>(1),new ArrayList<>(1),tip,addition,list);
        meaningA.add(A);
        meaningB.add(B);
    }

    /**
     * Creates a new VEntry with 0 points, ID < MIN_ID_TRESHOLD & current Date & 0 correct, wrong
     *
     * @param meaningA
     * @param meaningB
     * @param tip
     * @param addition
     * @param list
     */
    public VEntry(@NonNull List<String> meaningA,@NonNull List<String> meaningB,@Nullable String tip,@Nullable String addition,@NonNull VList list) {
        this(meaningA,meaningB,tip,addition,MIN_ID_TRESHOLD-1,list,0,null,new Date(System.currentTimeMillis()),0,0);
    }

    public static final Creator<VEntry> CREATOR = new Creator<VEntry>() {
        @Override
        public VEntry createFromParcel(Parcel in) {
            return new VEntry(in);
        }

        @Override
        public VEntry[] newArray(int size) {
            return new VEntry[size];
        }
    };

    /**
     * Returns meanings for A Column
     *
     * @return
     */
    public List<String> getAMeanings() {
        return meaningA;
    }

    /**
     * Set A-Meanings
     *
     * @param AMeanings
     */
    public void setAMeanings(List<String> AMeanings) {
        this.meaningA = AMeanings;
        this.changed = true;
    }

    /**
     * Returns meanings for B Column
     *
     * @return
     */
    public List<String> getBMeanings() {
        return meaningB;
    }

    /**
     * Set B-Meanings
     *
     * @param BMeanings
     */
    public void setBMeanings(List<String> BMeanings) {
        this.meaningB = BMeanings;
        this.changed = true;
    }

    /**
     * Get Tip
     *
     * @return
     */
    public @Nullable String getTip() {
        return tip;
    }

    /**
     * Get addition
     *
     * @return
     */
    public @Nullable String getAddition() {
        return addition;
    }

    /**
     * Set Tip
     *
     * @param tip
     */
    public void setTip(@Nullable String tip) {
        this.tip = tip;
        this.changed = true;
    }

    @Override
    public String toString() {
        if (this.getList() != null)
            return getAString() + " " + getBString() + " ID:" + id +" List:"+ getList().getId()+ " P:" + points;
        else
            return getAString() + " " + getBString() + " ID:" + id +" List:null P:" + points;
    }

    /**
     * Test for equality based on entry & list ID
     * If both have no List, it is ignored.
     * If one has no list, they are not seen as equal.
     * @param entry
     * @return
     */
    public boolean equals(VEntry entry) {
        if(this == entry)
            return true;
        if(this.getList() == null){
            if(entry.getList() == null)
                return this.getId() == entry.getId();
            else
                return false;
        }
        return this.getId() == entry.getId() && this.getList().equals(entry.getList());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VEntry)
            return equals((VEntry) obj);
        return super.equals(obj);
    }

    /**
     * Check whether this entry is existing, according to it's ID<br>
     *     <b>Note:</b> this is not a check whether this entity exists in the Database
     * @return true if the ID is valid
     */
    public boolean isExisting(){
        return isIDValid(id);
    }

    /**
     * Returns whether the Data of this VEntry was changed
     *
     * @return
     */
    public boolean isChanged() {
        return changed;
    }

    public VList getList() {
        return list;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

    public Date getLast_used() {
        return last_used;
    }

    public void setLast_used(Date last_used) {
        this.last_used = last_used;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public void incrCorrect() { this.correct++; }

    public int getWrong() {
        return wrong;
    }

    public void setWrong(int wrong) {
        this.wrong = wrong;
    }

    public void incrWrong() { this.wrong++; }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getAString() {
        return TextUtils.join(CONCAT,meaningA);
    }

    public String getBString() {
        return TextUtils.join(CONCAT,meaningB);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcel constructor
     * @param in
     */
    protected VEntry(Parcel in) {
        meaningA = in.createStringArrayList();
        meaningB = in.createStringArrayList();
        tip = in.readString();
        list = (VList) in.readValue(VList.class.getClassLoader());
        points = in.readInt();
        changed = readParcableBool(in);
        delete = readParcableBool(in);
        created = readParcableDate(in);
        last_used = readParcableDate(in);
        correct = in.readInt();
        wrong = in.readInt();
        addition = in.readString();
        id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(meaningA);
        parcel.writeStringList(meaningB);
        parcel.writeString(tip);
        parcel.writeValue(list);
        parcel.writeInt(points);
        writeParcableBool(parcel, changed);
        writeParcableBool(parcel, delete);
        writeParcableDate(parcel, created);
        writeParcableDate(parcel, last_used);
        parcel.writeInt(correct);
        parcel.writeInt(wrong);
        parcel.writeString(addition);
        parcel.writeInt(id);
    }
}
