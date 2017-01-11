/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseomega.news;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * Created by François L. on 13/12/2015.
 */
public class WebViewTitle extends WebView {

    private int titleHeight;
    private boolean touchInTitleBar;

    public WebViewTitle(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // determine height of title bar
        View title = getChildAt(0);
        titleHeight = title==null ? 0 : title.getMeasuredHeight();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        return true;   // don't pass our touch events to children (title bar), we send these in dispatchTouchEvent
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){

        boolean wasInTitle = false;
        switch(me.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                touchInTitleBar = (me.getY() <= visibleTitleHeight());
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                wasInTitle = touchInTitleBar;
                touchInTitleBar = false;
                break;
        }
        if(touchInTitleBar || wasInTitle) {
            View title = getChildAt(0);
            if(title!=null) {
                // this touch belongs to title bar, dispatch it here
                me.offsetLocation(0, getScrollY());
                return title.dispatchTouchEvent(me);
            }
        }
        // this is our touch, offset and process
        me.offsetLocation(0, -titleHeight);
        return super.dispatchTouchEvent(me);
    }

    /**
     * @return visible height of title (may return negative values)
     */
    private int visibleTitleHeight(){
        return titleHeight-getScrollY();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt){
        super.onScrollChanged(l, t, oldl, oldt);
        View title = getChildAt(0);
        if(title!=null)   // undo horizontal scroll, so that title scrolls only vertically
            title.offsetLeftAndRight(l - title.getLeft());
    }

    @Override
    protected void onDraw(Canvas c){

        c.save();
        int tH = visibleTitleHeight();
        if(tH>0) {
            // clip so that it doesn't clear background under title bar
            int sx = getScrollX(), sy = getScrollY();
            c.clipRect(sx, sy+tH, sx+getWidth(), sy+getHeight());
        }
        c.translate(0, titleHeight);
        super.onDraw(c);
        c.restore();
    }
}