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

package fr.bde_eseo.eseomega.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Patterns;

import java.util.regex.Pattern;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.slidingmenu.NavDrawerItem;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.StringUtils;

/**
 * Created by François on 14/04/2015.
 */
public class UserProfile {

    public static final String RESEAU_ESEO_FR = "@reseau.eseo.fr";
    public static final Pattern PHONE_PATTERN = Pattern.compile("/^((\\+|00)33\\s?|0)[679](\\s?\\d{2}){4}$/");
    private final static int MAX_TEXT_LENGTH = 39;
    private String name;
    private String id;
    private String email;
    private String encodedPassword;
    private boolean isCreated;
    private String picturePath;
    private String pushToken;
    private String phoneNumber; // Lydia oblige

    public UserProfile () {
        this.isCreated = false;
    }

    public UserProfile (Context ctx, String name, String id, String encodedPassword) {
        this.name = name;
        this.id = id;
        this.encodedPassword = EncryptUtils.sha256(ctx.getResources().getString(R.string.MESSAGE_PASS_USER) + encodedPassword);
        this.isCreated = true;
        this.picturePath = "";
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getName () {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId () {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword () {
        return this.encodedPassword;
    }

    public String getEmail () {
        return this.email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setCreated () {
        this.isCreated = true;
    }

    public void setUnCreated () {
        this.isCreated = false;
    }

    public String getPicturePath() {

        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    /**
     * Vérifie le numéro de téléphone du client
     * @param phoneNumber le numéro de téléphone
     * @return false si mauvais, true si correct
     */
    public boolean verifyPhoneNumber (String phoneNumber) {

        // 0600223344 -> ok (10)
        // +33600223344 -> ok aussi (12)
        // Balek du reste car le serveur check tout + le serveur de Lydia aussi

        return Patterns.PHONE.matcher(phoneNumber).matches() && phoneNumber.length() == 10 || phoneNumber.length() == 12;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isCreated () {
        return this.isCreated;
    }

    public String getFirstName () {
        int sp = getLastSpaceInString(this.name);
        if (sp != -1 && this.name.length() >= sp+2) {
            return this.name.substring(0, sp);
        } else {
            return this.name;
        }
    }

    public String getFamilyName () {
        int sp = getLastSpaceInString(this.name);
        if (sp != -1 && this.name.length() >= sp+2) {
            return this.name.substring(sp+1);
        } else {
            return this.name;
        }
    }

    public void guessEmailAddress () {
        this.email = StringUtils.simpleString(getFirstName()) + "." + StringUtils.simpleString(getFamilyName()) + RESEAU_ESEO_FR;
    }

    public void reverseName () {
        int sp = getLastSpaceInString(this.name);
        if (sp != -1 && this.name.length() >= sp+2) {
            String firstName = this.name.substring(sp+1);
            String famName = this.name.substring(0, sp);
            this.name = firstName + " " + famName;
        }
    }

    // Returns the exact position of the last space in a String, -1 else
    // V2.0 -> ok
    private int getLastSpaceInString (String s) {
        int a = 0, b = -1;

        do {
            a = s.indexOf(" ", a);
            if (a != -1) {
                b = a;
                ++a;
            }
        } while (a != -1);

        return b;
    }

    // Use getShortNameV2 instead
    public String getShortName () {
        if (this.name.length() > MAX_TEXT_LENGTH)
            return this.name.substring(0, MAX_TEXT_LENGTH) + "...";
        else
            return this.name;
    }

    public String getShortNameV2 () {

        // search for space
        String s = getShortName();
        int sp = getLastSpaceInString(s);
        if (sp != -1) {
            // Get the family name first letter
            if (s.length() >= sp + 2)
                return s.substring(0, sp+2);
            else
                return s.substring(0, sp);
        } else
            return s;
    }

    public String getShortEmail () {
        if (this.email.length() > MAX_TEXT_LENGTH)
            return this.email.substring(0, MAX_TEXT_LENGTH) + "...";
        else
            return this.email;
    }

    public NavDrawerItem getDrawerProfile () {
        return new NavDrawerItem(this.name, getShortEmail());
    }

    public void registerProfileInPrefs (Context context) {

        SharedPreferences prefs_Read;
        SharedPreferences prefs_Single;
        SharedPreferences.Editor prefs_Write;
        SharedPreferences.Editor prefs_WriteSingle;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_USER_PROFILE_KEY, 0);
        prefs_Write = prefs_Read.edit();
        prefs_Single = PreferenceManager.getDefaultSharedPreferences(context);
        prefs_WriteSingle = prefs_Single.edit();

        prefs_Write.putString(Constants.PREFS_USER_PROFILE_NAME, this.name);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_MAIL, this.email);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_ID, this.id);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_PASSWORD, this.encodedPassword);
        prefs_Write.putBoolean(Constants.PREFS_USER_PROFILE_EXISTS, this.isCreated);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_PICTURE, this.picturePath);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_PUSH_TOKEN, this.pushToken);
        prefs_WriteSingle.putString(Constants.PREFS_LYDIA_PHONE, this.phoneNumber);
        prefs_Write.apply();
        prefs_WriteSingle.apply();
    }

    public void readProfilePromPrefs (Context context) {

        SharedPreferences prefs_Read, prefs_Single;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_USER_PROFILE_KEY, 0);
        prefs_Single = PreferenceManager.getDefaultSharedPreferences(context);

        this.name = prefs_Read.getString(Constants.PREFS_USER_PROFILE_NAME, "");
        this.email = prefs_Read.getString(Constants.PREFS_USER_PROFILE_MAIL, "");
        this.encodedPassword = prefs_Read.getString(Constants.PREFS_USER_PROFILE_PASSWORD, "");
        this.id = prefs_Read.getString(Constants.PREFS_USER_PROFILE_ID, "");
        this.isCreated = prefs_Read.getBoolean(Constants.PREFS_USER_PROFILE_EXISTS, false);
        this.picturePath = prefs_Read.getString(Constants.PREFS_USER_PROFILE_PICTURE, "");
        this.pushToken = prefs_Read.getString(Constants.PREFS_USER_PROFILE_PUSH_TOKEN, "");
        this.phoneNumber = prefs_Single.getString(Constants.PREFS_LYDIA_PHONE, "");
    }

    public void removeProfileFromPrefs (Context context) {
        // remove user name, id, mail and reset Profile_exists
        this.isCreated = false;
        this.name = "";
        this.id = "";
        this.email = "";
        this.encodedPassword = "";
        this.picturePath = "";
        this.phoneNumber = "";
        registerProfileInPrefs(context);
    }

    public boolean isPushRegistered() {
        return pushToken != null && pushToken.length() > 0;
    }
}
