package fr.bde_eseo.eseomega.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.ImageView;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;

/**
 * Created by klemek on 12/01/17.
 */

public class ThemeUtils {

    public static int resolveColorFromTheme(Context ctx, int attr) {
       /* TypedValue typedValue = new TypedValue();

        TypedArray a = ctx.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();
        return color;*/
        TypedValue a = new TypedValue();
        ctx.getTheme().resolveAttribute(attr, a, true);
        return a.data;
    }

    public static int resolveColorFromTheme2(Context ctx, int attr, boolean resolve) {
       /* TypedValue typedValue = new TypedValue();

        TypedArray a = ctx.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();
        return color;*/
        TypedValue a = new TypedValue();
        ctx.getTheme().resolveAttribute(attr, a, resolve);
        return a.data;
    }

    public static int preferredTheme(Context ctx) {
        int id;
        switch (PreferenceManager.getDefaultSharedPreferences(ctx).getString(Constants.PREFS_GENERAL_THEME, "0")) {
            default:
                id = R.style.DefaultTheme;
                break;
            case "1":
                id = R.style.OasisTheme;
                break;
            case "2":
                id = R.style.MegaTheme;
                break;
            case "3":
                id = R.style.EldoradoTheme;
                break;
        }
        ctx.setTheme(id);
        return id;
    }

    public static int updateLogo(ImageView logo) {
        int cid, did;
        switch (PreferenceManager.getDefaultSharedPreferences(logo.getContext()).getString(Constants.PREFS_GENERAL_THEME, "0")) {
            default:
                cid = R.color.md_white_1000;
                did = R.drawable.logo_default_white;
                break;
            case "1":
                cid = R.color.oasis_bg;
                did = R.drawable.logo_oasis_white;
                break;
            case "2":
                cid = R.color.mega_bg;
                did = R.drawable.logo_mega_white;
                break;
            case "3":
                cid = R.color.eldo_bg;
                did = R.drawable.logo_eldorado_white;
                break;
        }

        logo.setImageDrawable(logo.getResources().getDrawable(did));
        logo.setColorFilter(logo.getResources().getColor(cid), PorterDuff.Mode.MULTIPLY);

        return cid;
    }

    public static boolean circularLoad(Context ctx) {
        switch (PreferenceManager.getDefaultSharedPreferences(ctx).getString(Constants.PREFS_GENERAL_THEME, "0")) {
            default:
                return false;
            case "1":
                return false;
            case "2":
                return true;
            case "3":
                return true;
        }
    }

}
