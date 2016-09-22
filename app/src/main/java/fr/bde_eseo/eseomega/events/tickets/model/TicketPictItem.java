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

package fr.bde_eseo.eseomega.events.tickets.model;

import java.text.DecimalFormat;

import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by François L. on 13/01/2016.
 */
public class TicketPictItem {

    private String imgUrl, title, description, lowPrice;
    private EventItem externalEventItem;
    private double bestPrice;

    public TicketPictItem(EventItem eventItem) {
        externalEventItem = eventItem;
        title = eventItem.getName();
        description = eventItem.getDetails();
        imgUrl = eventItem.getImgUrl();

        bestPrice = 0;
        for (int i=0;i<externalEventItem.getSubEventItems().size();i++) {
            double p = externalEventItem.getSubEventItems().get(i).getPrice();
            if (bestPrice == 0 || p < bestPrice)
                bestPrice = p;
        }

        lowPrice = new DecimalFormat("0.00").format(bestPrice) + "€";
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EventItem getExternalEventItem() {
        return externalEventItem;
    }

    public String getLowPrice() {
        return lowPrice;
    }
}
