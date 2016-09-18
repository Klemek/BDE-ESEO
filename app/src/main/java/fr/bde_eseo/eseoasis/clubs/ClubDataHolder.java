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

package fr.bde_eseo.eseoasis.clubs;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by François L. on 31/08/2015.
 */
public class ClubDataHolder {
    private static ClubDataHolder instance;

    private ClubDataHolder (){}

    public static ClubDataHolder getInstance() {
        if (instance == null)
            instance = new ClubDataHolder();
        return instance;
    }

    private ArrayList<ClubItem> clubs;

    public void reset() {
        if (clubs == null)
            clubs = new ArrayList<>();
        else
            clubs.clear();
    }

    public ArrayList<ClubItem> getClubs() {
        return clubs;
    }

    public void parseJSON (JSONArray clubsarray) {
        try {
            for (int i=0;i<clubsarray.length();i++)
                clubs.add(new ClubItem(clubsarray.getJSONObject(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
