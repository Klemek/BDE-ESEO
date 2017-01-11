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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Created by François L. on 18/01/2016.
 * Activité qui montre les fonctionnalités de l'app au 1er lancement, puis redirection vers MainActivity
 */
public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Utils.getPreferredTheme(getApplicationContext()));
        setContentView(R.layout.activity_tutorial);

        // Preferences
        SharedPreferences prefs_Read;
        SharedPreferences.Editor prefs_Write;

        // Initialize preference objects
        prefs_Read = getSharedPreferences(Constants.PREFS_APP_WELCOME, 0);
        prefs_Write = prefs_Read.edit();

        // We won't reopen this Tutorial ever
        prefs_Write.putString(Constants.PREFS_APP_LAST_VERSION, getString(R.string.app_version_name));
        prefs_Write.apply();
    }

    /**
     * On click menu action
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tuto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_read:
                Intent i = new Intent(TutorialActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
