package fr.bde_eseo.eseomega.family;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import fr.bde_eseo.eseomega.R;


/**
 * Created by Kleme on 10/09/2016.
 */
public class TreeView extends View {

    private static final float FACT = 0.6f;
    private static final float MFACTH = 0.08f;
    private static final float RATIO = 3.5f;
    private static final float TMPS = 10000;

    private Paint pb, pb2, pb2g, pline, pbx, pn2, pn, ppr, ppr2;
    private HashMap<Integer,StudentItem> family;
    private int depth, size, rnkmx;
    private int sizes[];
    private int dx, bs, dt;
    private int height, width;
    private String[] prnames;

    //private GestureDetector gestureDetector;



    public TreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        pb = new Paint();
        pb.setColor(getResources().getColor(R.color.color_background));
        pb.setStyle(Paint.Style.FILL);

        pb2 = new Paint();
        pb2.setColor(getResources().getColor(R.color.color_background2));

        pb2g = new Paint();

        pbx = new Paint();
        pbx.setColor(getResources().getColor(R.color.md_grey_600));
        pbx.setStyle(Paint.Style.FILL);

        pline = new Paint();
        pline.setColor(getResources().getColor(R.color.md_grey_600));
        pline.setStyle(Paint.Style.STROKE);
        pline.setStrokeWidth(3);
        pline.setAntiAlias(true);

        pn2 = new Paint();
        pn2.setColor(getResources().getColor(R.color.color_text_primary_inverse));
        pn2.setTextAlign(Paint.Align.CENTER);
        pn2.setAntiAlias(true);

        pn = new Paint();
        pn.setColor(getResources().getColor(R.color.md_white_1000));
        pn.setAntiAlias(true);
        pn.setTextAlign(Paint.Align.CENTER);

        ppr = new Paint();
        ppr.setColor(getResources().getColor(R.color.md_grey_600));
        ppr.setAntiAlias(true);
        ppr.setTextAlign(Paint.Align.CENTER);
        ppr.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        ppr2 = new Paint();
        ppr2.setColor(getResources().getColor(R.color.md_grey_600));
        ppr2.setAntiAlias(true);
        ppr2.setTextAlign(Paint.Align.CENTER);
        ppr2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        //gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setFamily(HashMap<Integer,StudentItem> family){
        this.family = family;
        this.size = 0;
        int mid = family.size();
        rnkmx = 0;
        for(int k:family.keySet()){
            int rank = family.get(k).getRank();
            if(rank > rnkmx){
                rnkmx = rank;
            }else if(rank < mid){
                mid = rank;
            }
        }
        this.depth = rnkmx - mid + 1;
        prnames = new String[depth + 1];
        organize();
        updateWidth();
        invalidate();
    }

    private void organize(){
        HashMap<Integer,StudentItem> f2 = copyf(family);
        boolean cont = true;
        HashMap<Integer,StudentItem> f3;
        while(cont){
            cont = false;
            f3 = copyf(f2);
            for(int k: f2.keySet()){
                StudentItem si = f3.get(k);
                if(prnames[si.getRank()-rnkmx+depth] == null){
                    prnames[si.getRank()-rnkmx+depth]  = si.getRnk();
                }
                if(si.getRank() < rnkmx && si.getParents().isEmpty()){
                    int i = Collections.max(f3.keySet()) + 1;
                    f3.put(i, new StudentItem(i, null, newList(new int[]{i}), si.getRank()+1));
                    si.addParent(i);
                    cont=true;
                }
                if(si.getRank() > 0 && si.getChildren().isEmpty()){
                    int i = Collections.max(f3.keySet()) + 1;
                    f3.put(i, new StudentItem(i, newList(new int[]{i}), null, si.getRank()-1));
                    si.addChild(i);
                    cont=true;
                }
            }
            f2 = copyf(f3);
        }
        sizes= new int[rnkmx+1];
        for(int k: f2.keySet()){
            sizes[f2.get(k).getRank()]+=1;
        }
        size = 0;
        for(int s:sizes){
            if(s > size){
                size = s;
            }
        }
        float p = 0;
        for(int k: f2.keySet()){
            StudentItem si = f2.get(k);
            if(si.getRank()==rnkmx){
                si.setP(p);
                p+= TMPS /(sizes[rnkmx]+1);
            }
        }
        for(int k: f2.keySet()){
            if(f2.get(k).getRank()==rnkmx){
                suborg(f2, k);
                break;
            }
        }
        for(int k: f2.keySet()){
            if(family.containsKey(k)){
                family.get(k).setP(f2.get(k).getP());
            }
        }

    }

    private void suborg(HashMap<Integer,StudentItem> f2, int k){
        ArrayList<Integer> chi = f2.get(k).getChildren();
        if(chi.size() > 0 && f2.get(chi.get(0)).getP() == StudentItem.NULL){
            ArrayList<Integer> par = f2.get(chi.get(0)).getParents();
            for(Integer p:par){
                if(f2.get(p).getP() == StudentItem.NULL){
                    suborg(f2, f2.get(p).getParents().get(0));
                }
            }
            if(chi.size()>par.size()){
                float m = 0;
                for(Integer p:par){
                    m += f2.get(p).getP();
                }
                m /= par.size();
                float n = TMPS / sizes[f2.get(chi.get(0)).getRank()];
                float p = m - (chi.size() - 1)*n*0.5f;
                for(Integer c:chi){
                    f2.get(c).setP(p);
                    p+=n;
                }
            }else{
                float mi = TMPS;
                float ma = 0;
                for(Integer p: par){
                    float x = f2.get(p).getP();
                    if(x<mi) {mi = x;}
                    if(x>ma){ma = x;}
                }
                if(chi.size()==1){
                    f2.get(chi.get(0)).setP((mi+ma)/2);
                }else{
                    float p = mi;
                    float n = (ma - mi) / (chi.size() + 1);
                    for(Integer c:chi){
                        f2.get(c).setP(p);
                        p+=n;
                    }
                }
            }
            for(Integer c:chi){
                suborg(f2, c);
            }
        }
    }

    private ArrayList<Integer> newList(int[] l){
        Integer[] l2 = new Integer[l.length];
        for(int i = 0; i < l.length; i++){l2[i] = l[i];}
        return new ArrayList<Integer>(Arrays.asList(l2));
    }

    private HashMap<Integer,StudentItem> copyf(HashMap<Integer,StudentItem> f){
        HashMap<Integer,StudentItem> clone = new  HashMap<>();
        for(Integer k:f.keySet()){
            clone.put(k, f.get(k).clone());
        }
        return clone;
    }

    @Override
    public void onDraw (Canvas canvas){
        int he = canvas.getHeight();
        int wi = canvas.getWidth();
        canvas.drawRect(new Rect(0, 0, wi, he), pb);
        if(family != null && family.size() > 0){
            calculate(width, height);
            for(int k = 0; k  < depth; k++){
                if(k%2==1) {
                    canvas.drawRect(new Rect(k * dx * 3 / 2, 0, k * dx * 3 / 2 + dx / 2, he), pb2g);
                    canvas.drawRect(new Rect(k * dx * 3 / 2 + dx / 2, 0, (k + 1) * dx * 3 / 2, he), pb2);
                    if(k != depth-1)
                        canvas.drawRect(new Rect((k + 1) * dx * 3 / 2, 0, (k + 1) * dx * 3 / 2 + dx/2, he), pb2g);
                    else
                        canvas.drawRect(new Rect((k + 1) * dx * 3 / 2, 0, (k + 1) * dx * 3 / 2 + dx/2, he), pb2);
                }
                canvas.drawText(StudentItem.getRnk(rnkmx-k), k * dx * 3 / 2 + dx, bs, ppr2);
                canvas.drawText(prnames[depth-k], k * dx * 3 / 2 + dx, he-bs, ppr);

            }
            for(int k: family.keySet()){
                drawMember(canvas, k);
                for(Integer k2: family.get(k).getChildren()){
                    drawLink(canvas, k, k2);
                }
            }
        }
    }

    private void calculate(float wi, float he){
        int ms = 0;
        for(int s:sizes){
            if(s>ms){
                ms= s;
            }
        }
        bs = Math.min((int)(he*FACT/(2*ms+1)),(int)(he* MFACTH));
        dx = (int)(wi/((3f*depth+1f)/2f));
        pn.setTextSize(bs /3);
        pn2.setTextSize(bs /3);
        ppr.setTextSize(he * MFACTH / 2.5f);
        ppr2.setTextSize(he * MFACTH / 2f);
        dt = (int)((pn.descent() + pn.ascent())/2);

        float mi = TMPS; //first is 0 so...
        float ma = 0;
        for(int k: family.keySet()){
            float x = family.get(k).getP();
            if(x<mi) {
                mi = x;
            }
            if(x>ma){
                ma = x;
            }
        }
        if(mi == ma){
            for(int k: family.keySet()){
                family.get(k).setP(he/2 - bs/2);
            }
        }else {
            float fa = he * FACT / (ma - mi);
            float de = he * (1 - FACT) / 2;
            for (int k : family.keySet()) {
                family.get(k).setP(de + fa * (-mi + family.get(k).getP()));
            }
        }
        pb2g.setShader(new LinearGradient(0, 0,
                dx/2, 0,
                ContextCompat.getColor(getContext(), R.color.color_background2),
                ContextCompat.getColor(getContext(), R.color.color_background), Shader.TileMode.MIRROR));
    }

    private int[] getCoords(int k){
        return new int[]{arr(3f*dx*(rnkmx - family.get(k).getRank())/2f + dx), arr(family.get(k).getP()), };
    }

    private static int arr(float f){
        return (int)(f+0.5f);
    }

    private static String crop(String s, String end, int n){
        if(s.length()<=n)
            return s;
        return s.substring(0, n-end.length())+end;
    }

    private void drawMember(Canvas canvas, int k){
        int[] xy = getCoords(k);
        Paint p1 = pbx, p2 = pn;
        if(family.get(k).isMarked()){
            p2 = pn2;
        }
        canvas.drawRect(new Rect(xy[0] - dx /2, xy[1] - bs /2,
                                xy[0] + dx /2, xy[1] + bs /2), p1);
        String[] names = family.get(k).getName().split(" ");
        canvas.drawText(crop(names[0], "...", 16),xy[0], xy[1] - bs / 2 + bs /3 - dt, p2 );
        canvas.drawText(crop(names[1], "...", 16),xy[0], xy[1] - bs / 2 + 2 * bs /3 - dt, p2 );
    }

    private void drawLink(Canvas canvas, int k0, int k1){
        int[] xy0 = getCoords(k0);
        int[] xy1 = getCoords(k1);
        canvas.drawLine(xy0[0] + dx /2, xy0[1],
                (xy0[0] + xy1[0]) / 2, xy0[1], pline);
        canvas.drawLine((xy0[0] + xy1[0]) / 2, xy0[1],
                (xy0[0] + xy1[0]) / 2, xy1[1], pline);
        canvas.drawLine((xy0[0] + xy1[0]) / 2, xy1[1],
                xy1[0] - dx /2, xy1[1], pline);
    }

    @Override
    public void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
        height = MeasureSpec.getSize(heightMeasureSpec);
        updateWidth();
    }

    private void updateWidth(){
        int bs = Math.min((int)(height / (2f*size+1f)),(int)(height * MFACTH));
        int dx = (int)(RATIO*bs);
        width = (int)(((3f*depth+1f)/2f)*dx);
        this.setMeasuredDimension(width, height);
    }
    //No need to zoom
    /*
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            return true;
        }
    }
*/
}
