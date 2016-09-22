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

package fr.bde_eseo.eseomega.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Created by François L. on 06/07/2015.
 * V2.0 : uses SHA-256
 */
public class EncryptUtils {

    public static String sha256(String password)
    {
        String sha256 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha256 = byteToHex(crypt.digest());
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sha256;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String passBase64 (String password) {
        String passBis = "", result = "", copyPass = password;

        // Create the password + key
        for (int i=0;i<password.length();i++) {
            passBis += "" + (char)(password.charAt(i) + 1);
        }

        // Base64 for keyed
        try {
            passBis = Base64.encodeToString(passBis.getBytes("UTF-8"), Base64.NO_WRAP);
            copyPass = Base64.encodeToString(copyPass.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Intercal password with keyed
        for (int i=0;i<passBis.length();i++) {
            result += copyPass.charAt(i);
            result += passBis.charAt(i);
        }

        // Remove == doublon if present
        if (result.contains("===="))
            result = result.substring(0, result.length()-2);
        else // Add two "==" if not
            result += "==";

        return result;
    }
}
