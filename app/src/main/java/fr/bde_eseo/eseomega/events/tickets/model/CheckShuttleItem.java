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

/**
 * Created by François L. on 13/01/2016.
 * Décris une navette
 */
public class CheckShuttleItem {

    private boolean isCheck, isHeader;
    private String name;
    private ShuttleItem shuttleItem;

    public CheckShuttleItem(ShuttleItem shuttleItem) {
        this.shuttleItem = shuttleItem;
        isHeader = false;
        isCheck = false;
    }

    public CheckShuttleItem(String name) {
        this.name = name;
        isHeader = true;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public ShuttleItem getShuttleItem() {
        return shuttleItem;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getName() {
        return name;
    }

    public void setShuttleItem(ShuttleItem shuttleItem) {
        this.shuttleItem = shuttleItem;
    }
}
