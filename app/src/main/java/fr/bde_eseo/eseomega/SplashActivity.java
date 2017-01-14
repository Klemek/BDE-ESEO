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

package fr.bde_eseo.eseomega;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ThemeUtils;

/**
 * Created by François on 20/04/2015.
 */
public class SplashActivity extends Activity {

    private final static int SPLASH_TIME_OUT = 1900;
    private final static int MIN_TRICK = 5;
    private int trick = 0;

    // Preferences
    private SharedPreferences prefs_Read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeUtils.preferredTheme(getApplicationContext()));
        setContentView(R.layout.activity_splash);
        ImageView vLogo = (ImageView) findViewById(R.id.imgLogo);
        int lcolor = ThemeUtils.updateLogo(vLogo);
        UserProfile profile = new UserProfile();
        profile.readProfilePromPrefs(this);

        final ProgressBar progressBar;

        if (ThemeUtils.circularLoad(getApplicationContext())) {
            progressBar = (ProgressBar) findViewById(R.id.idLoadc);
            findViewById(R.id.idLoad).setVisibility(View.GONE);
        } else {
            progressBar = (ProgressBar) findViewById(R.id.idLoad);
            findViewById(R.id.idLoadc).setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(lcolor), PorterDuff.Mode.MULTIPLY);
        }
        // Initialize preference objects
        prefs_Read = getSharedPreferences(Constants.PREFS_APP_WELCOME, 0);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 1, 500);
                animation.setDuration(1500); //in milliseconds
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                animation.start();
            }
        }, 400);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i;
                //Tutorial at first launch
                boolean tutorial = !prefs_Read.contains(Constants.PREFS_APP_LAST_VERSION);
                try{

                    if (getString(R.string.app_version_name).compareTo(prefs_Read.getString(Constants.PREFS_APP_LAST_VERSION, "")) > 0) {
                        //Do update things

                        String[] vero2 = prefs_Read.getString(Constants.PREFS_APP_LAST_VERSION, "").split("\\.");
                        String[] vern2 = getString(R.string.app_version_name).split("\\.");

                        tutorial = vero2.length == 0 || !(vero2[0].equals(vern2[0]) && vero2[1].equals(vern2[1]));//check first two numbers of version
                    }
                }catch(ClassCastException ignore){}
                if (tutorial) {
                    i = new Intent(SplashActivity.this, TutorialActivity.class);
                } else {
                    i = new Intent(SplashActivity.this, MainActivity.class);
                }

                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
