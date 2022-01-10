package com.kingsbyte.subrefill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NetworkAdapter extends BaseAdapter implements Filterable {
    Context context;
    List<Networks> list;
    private ArrayList<Networks> suggestions = new ArrayList<>();
    private Filter filter = new CustomFilter();
    public NetworkAdapter(@NonNull Context context,List<Networks> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i).getNetworkName();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dropdown_text_item, parent, false);
            holder = new ViewHolder();
            //holder.networkName = (TextView)convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.networkName.setText(list.get(position).getNetworkName());



        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }


    private static class ViewHolder {
        TextView networkName;
    }

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            suggestions.clear();

            if (list != null && constraint != null) { // Check if the Original List and Constraint aren't null.
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getNetworkName().toLowerCase().contains(constraint)) { // Compare item in original list if it contains constraints.
                        suggestions.add(list.get(i)); // If TRUE add item in Suggestions.
                    }
                }
            }
            FilterResults results = new FilterResults(); // Create new Filter Results and return this to publishResults;
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
