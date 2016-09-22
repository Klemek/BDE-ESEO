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

package fr.bde_eseo.eseomega.version;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fr.bde_eseo.eseomega.BuildConfig;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by François L. on 10/01/2016.
 * Permet de vérifier en tâche asynchrone si l'application dispose bien de la dernière version disponible.
 */
public class AsyncCheckVersion extends AsyncTask<String, String, String> {

    private Context context;

    public AsyncCheckVersion(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        HashMap<String,String> postParam = new HashMap<>();
        postParam.put(context.getResources().getString(R.string.os), Constants.APP_ID);
        return ConnexionUtils.postServerData(Constants.URL_API_INFO_VERSION, postParam, context);
    }

    @Override
    protected void onPostExecute(String data) {

        if (Utilities.isNetworkDataValid(data)) {
            try {
                JSONObject obj = new JSONObject(data);
                int status = obj.getInt("status");

                if (status == 1) {
                    JSONObject jsonData = obj.getJSONObject("data");
                    String servVersion = jsonData.getString("version");

                    if (!servVersion.equals(BuildConfig.VERSION_NAME)) {
                        new MaterialDialog.Builder(context)
                                .title(R.string.update_title)
                                .content(R.string.update_content)
                                .positiveText(R.string.dialog_yes)
                                .negativeText(R.string.dialog_no)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);

                                        try {
                                            Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
                                            context.startActivity(storeIntent);
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            Toast.makeText(context, R.string.toast_playstore, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
