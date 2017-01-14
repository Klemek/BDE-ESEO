package fr.bde_eseo.eseomega.family;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.utils.JSONUtils;

public class StudentItem implements Cloneable{

    public final static float NULL = -18616843.6419819f; //random

    private final String name;
    private final int id;
    private final int rank;
    private final ArrayList<Integer> children;
    private final ArrayList<Integer> parents;
    private String promo;
    private float p;
    private boolean marked;

    private StudentItem(int id, String name, String promo, ArrayList<Integer> parents, ArrayList<Integer> children, int rank, float p) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.promo = promo;
        this.marked = false;
        if(children == null){
            children = new ArrayList<>();
        }
        this.children = children;
        if(parents == null){
            parents = new ArrayList<>();
        }
        this.parents = parents;
        this.p = p;
        this.promo = promo;
    }

    private StudentItem(int id, String name, String promo, ArrayList<Integer> parents, ArrayList<Integer> children, int rank) {
        this(id, name, promo, parents, children, rank, NULL);
    }

    public StudentItem(int id, ArrayList<Integer> parents, ArrayList<Integer> children,  int rank){
        this(id, "", "", parents, children, rank);
    }

    public StudentItem(JSONObject obj) throws JSONException {
        this(obj.getInt(Constants.JSON_STUDENT_ID),
                obj.getString(Constants.JSON_STUDENT_NAME),
                obj.getString(Constants.JSON_STUDENT_PROMO),
                JSONUtils.getList(obj, Constants.JSON_STUDENT_PARENTS),
                JSONUtils.getList(obj, Constants.JSON_STUDENT_CHILDREN),
                JSONUtils.getInt(obj, Constants.JSON_STUDENT_RANK, 0));
    }

    private static ArrayList<Integer> cloneList(ArrayList<Integer> list) {
        ArrayList<Integer> clone = new ArrayList<>(list.size());
        for (Integer item : list) clone.add(item);
        return clone;
    }

    public static String getRnk(int rank) {
        switch (rank) {
            case 0:
                return "P1";
            case 1:
                return "P2";
            case 2:
                return "I1";
            case 3:
                return "I2";
            case 4:
                return "I3";
            default:
                return "Ancien";
        }
    }

    public String getName() {
        return name;
    }

    public boolean isMarked(){return marked;}

    public void mark(){this.marked = true;}

    public StudentItem clone(){
        return new StudentItem(this.id, name, promo, cloneList(parents), cloneList(children), rank, p);
    }

    public int getRank() {
        return rank;
    }

    public String getRnk() {
        return promo;
    }

    public int getId() {return id;}

    public ArrayList<Integer> getChildren() { return children;}

    public void addChild(Integer child){
        children.add(child);
    }

    public ArrayList<Integer> getParents() { return parents;}

    public void addParent(Integer par){
        parents.add(par);
    }

    public float getP(){ return p;}

    public void setP(float p){this.p = p;}

    public String getDetails() {
        return getRnk(rank) + " • Promotion " +promo;
    }
}
