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

package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utils;

/**
 * Sends the email adress to server
 */
class AsyncEventEmail extends AsyncTask<String, String, String> {

    private final Context context;
    private final String email;
    private final UserProfile userProfile;
    private final AppCompatActivity backActivity;
    private final int idcmd;
    private MaterialDialog md;
    private String b64email;

    public AsyncEventEmail(Context context, String email, AppCompatActivity backActivity, UserProfile userProfile, int idcmd) {
        this.context = context;
        this.email = email;
        this.backActivity = backActivity;
        this.userProfile = userProfile;
        this.idcmd = idcmd;
        this.b64email = ""; // pre init, prevents from null.getBytes
        try {
            this.b64email = Base64.encodeToString(email.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        md = new MaterialDialog.Builder(context)
                .title(R.string.mail_sending_title)
                .content(R.string.wait)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .show();
    }

    @Override
    protected void onPostExecute(String data) {

        md.hide();
        int err = 0;
        String errMsg = context.getResources().getString(R.string.error_network_short);

        if (Utils.isNetworkDataValid(data)) {
            try {
                JSONObject obj = new JSONObject(data);
                err = obj.getInt("status");
                errMsg = obj.getString("cause");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Email sent with success !
        if (err == 1) {
            Toast.makeText(context, context.getResources().getString(R.string.mail_send) +" " + email, Toast.LENGTH_SHORT).show();
            if (backActivity != null) backActivity.finish();
        } else {
            // Error, show message
            new MaterialDialog.Builder(context)
                    .title(R.string.error)
                    .content(errMsg + (err == 0 ? "" : " (code : " + err + ")"))
                    .negativeText(R.string.dialog_close)
                    .positiveText(R.string.dialog_retry)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            AsyncEventEmail asyncEmail = new AsyncEventEmail(context, email, backActivity, userProfile, idcmd);
                            asyncEmail.execute();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            if (backActivity != null) backActivity.finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected String doInBackground(String... sData) {
        HashMap<String, String> params = new HashMap<>();
        params.put(context.getResources().getString(R.string.client), userProfile.getId());
        params.put(context.getResources().getString(R.string.password), userProfile.getPassword());
        params.put(context.getResources().getString(R.string.idcmd), String.valueOf(idcmd));
        params.put(context.getResources().getString(R.string.email), b64email);
        params.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_SEND_EMAIL_EVENT) + userProfile.getId() + userProfile.getPassword() + String.valueOf(idcmd) + b64email));
        return ConnexionUtils.postServerData(Constants.URL_API_EVENT_MAIL, params, context);
    }
}