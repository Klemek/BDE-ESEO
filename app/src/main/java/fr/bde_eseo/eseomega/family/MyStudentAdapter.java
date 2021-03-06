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

package fr.bde_eseo.eseomega.family;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;

class MyStudentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<StudentItem> studentItems;

    public MyStudentAdapter(ArrayList<StudentItem> studentItems) {
        this.studentItems = studentItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StudentItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_simple, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        int TYPE_ITEM = 1;
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final StudentItem ri = studentItems.get(position);

        StudentItemViewHolder rivh = (StudentItemViewHolder) holder;
        rivh.name.setText(ri.getName());
        rivh.details.setText(ri.getDetails());
    }

    @Override
    public int getItemCount() {
        return studentItems.size();
    }

    // Classic View Holder for Room item
    public class StudentItemViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView details;

        public StudentItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvName);
            details = (TextView) v.findViewById(R.id.tvDesc);
        }
    }
}
