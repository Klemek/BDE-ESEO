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

package fr.bde_eseo.eseoasis.news;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import fr.bde_eseo.eseoasis.Constants;
import fr.bde_eseo.eseoasis.utils.JSONUtils;

/**
 * Created by François L. on 25/08/2015.
 */
public class NewsItem {
    private String name, strDate, data, shData, frenchStr, headerImg;
    private boolean isFooter;
    private boolean isHeader;
    private boolean isLoading;
    private Date date;



    public boolean isFooter() {
        return isFooter;
    }

    private ArrayList<String> imgLinks;

    public NewsItem() {
        isFooter = true;
        isHeader = false;
        isLoading = false;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public NewsItem(String text) {
        isFooter = false;
        isHeader = true;
        name = text;
    }

    public NewsItem(JSONObject obj) throws JSONException {
        this.isFooter = false;
        this.isHeader = false;
        this.name = obj.getString(Constants.JSON_NEWS_TITLE);
        this.strDate = obj.getString(Constants.JSON_NEWS_FULLDATE);
        this.data = obj.getString(Constants.JSON_NEWS_CONTENT);
        this.shData = JSONUtils.getString(obj,Constants.JSON_NEWS_PREVIEW, "").trim();
        this.date = getParsedDate(strDate);
        this.frenchStr = getFrenchDate(strDate);
        this.headerImg = obj.getString(Constants.JSON_NEWS_IMG);
        // TODO parse data and fill imgLinks
        this.imgLinks = new ArrayList<>();
        imgLinks.add(this.headerImg);

        /*

        // All img are like <img ... >
        if (this.data != null) {
            int stPos = 0;
            int endPos = 0, ipos = 0;
            while (stPos != -1) {
                stPos = this.data.indexOf("<img ");
                if (stPos != -1) {
                    endPos = this.data.indexOf(">", stPos);
                    if (endPos != -1 && stPos < endPos) {

                        // We found an image link : remove st...end
                        int fst = data.indexOf("src=\"", stPos)+5;
                        String imgLink = data.substring(fst, data.indexOf("\"", fst+1));
                        imgLinks.add(imgLink);
                        ipos++;
                        this.data = this.data.substring(0, stPos) + this.data.substring(endPos+1);
                        stPos = 0; // restart from zero -> we remove string in length
                    }
                }
            }
        }*/
    }

    public NewsItem (String name, String html, String frenchStr, ArrayList<String> imgLinks) {
        this.name = name;
        this.data = html;
        this.frenchStr = frenchStr;
        this.imgLinks = imgLinks;
    }


    @Override
    public String toString() {
        return "NewsItem{" +
                "name='" + name + '\'' +
                ", strDate='" + strDate + '\'' +
                ", data='" + data + '\'' +
                ", shData='" + shData + '\'' +
                ", frenchStr='" + frenchStr + '\'' +
                ", headerImg='" + headerImg + '\'' +
                ", date=" + date +
                ", imgLinks=" + imgLinks +
                '}';
    }



    private Date getParsedDate (String d) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.FRANCE);
        Date date = null;
        try {
            date = format.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getName() {
        return name;
    }

    public String getStrDate() {
        return strDate;
    }

    public String getData() {
        return data;
    }

    public String getShData() {
        return shData;
    }

    public String getFrenchStr() {
        return frenchStr;
    }

    public String getHeaderImg() {
        return headerImg;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<String> getImgLinks() {
        return imgLinks;
    }

    private String getFrenchDate (String dt) {
        Date d = getParsedDate(dt);
        SimpleDateFormat sdf = new SimpleDateFormat("E dd MMMM yyyy, 'à' HH:mm", Locale.FRANCE);
        return sdf.format(d);
    }
}
