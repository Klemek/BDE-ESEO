package fr.bde_eseo.eseoasis.family;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseoasis.Constants;
import fr.bde_eseo.eseoasis.utils.JSONUtils;

public class StudentItem implements Cloneable{

    public final static float NULL = -18616843.6419819f; //random

    private String name, promo;
    private int id, rank;
    private ArrayList<Integer> children;
    private ArrayList<Integer> parents;
    private float p;
    private boolean marked;

    public StudentItem(int id, String name, String promo, ArrayList<Integer> parents, ArrayList<Integer> children,  int rank, float p){
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

    public StudentItem(int id, String name, String promo, ArrayList<Integer> parents, ArrayList<Integer> children,  int rank){
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

    public String getName() {
        return name;
    }

    public boolean isMarked(){return marked;}

    public void mark(){this.marked = true;}

    public StudentItem clone(){
        return new StudentItem(this.id, name, promo, cloneList(parents), cloneList(children), rank, p);
    }

    private static ArrayList<Integer> cloneList(ArrayList<Integer> list) {
        ArrayList<Integer> clone = new ArrayList<Integer>(list.size());
        for (Integer item : list) clone.add(item);
        return clone;
    }

    public int getRank() {
        return rank;
    }

    public String getPromo() {
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
        String rnkname;
        switch(rank){
            case 0:rnkname = "P1";break;
            case 1:rnkname = "P2";break;
            case 2:rnkname = "I1";break;
            case 3:rnkname = "I2";break;
            case 4:rnkname = "I3";break;
            default:rnkname = "Ancien";break;
        }
        return rnkname + " • Promotion " +promo;
    }
}
