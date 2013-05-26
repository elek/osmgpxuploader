package net.anzix.osm.upload;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @see  http://stackoverflow.com/questions/14737519/how-can-you-implement-multi-selection-and-contextual-actionmode-in-actionbarsher/14737520#14737520
 * @param <T>
 */
public abstract class CustomCheckableAdapter<T> extends CustomAdapter<T> {

    private Set<Integer> checkedItems = new HashSet<Integer>();

    protected CustomCheckableAdapter(Context context, List<T> entries) {
        super(context, entries);
    }

    public void toggleChecked(int pos) {
        final Integer v = Integer.valueOf(pos);
        //TODO modify it to multi selection
        if (this.checkedItems.contains(v)) {
            this.checkedItems.remove(v);
        } else {
            checkedItems.clear();
            this.checkedItems.add(v);
        }
        this.notifyDataSetChanged();
    }

    public Set<Integer> getCheckedItems() {
        return this.checkedItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View res = super.getView(position, convertView, parent);
        boolean multiMode = false;
        res.setBackgroundResource(multiMode ? R.drawable.selector_list_multimode : R.drawable.selector_list);
        if(checkedItems.contains(Integer.valueOf(position))) {
            // if this item is checked - set checked state
            res.getBackground().setState(new int[]{android.R.attr.state_checked});
        }else{
            // if this item is unchecked - set unchecked state (notice the minus)
            res.getBackground().setState(new int[]{-android.R.attr.state_checked});
        }
        return res;
    }
}

