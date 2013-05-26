package net.anzix.osm.upload;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import net.anzix.osm.upload.data.Gpx;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomAdapter<T> extends ArrayAdapter<T> {

    private List<T> entries;

    private Activity activity;

    ColorStateList defaultColor;

    public CustomAdapter(Activity a, int textViewResourceId, List<T> entries) {
        super(a, textViewResourceId, entries);
        this.entries = entries;
        this.activity = a;
    }

    public static class ViewHolder {
        public TextView item1;
        public TextView item2;
        public TextView item3;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(getItemLayout(), null);
            holder = new ViewHolder();
            cacheViews(holder, v);
            v.setTag(holder);
        } else
            holder = (ViewHolder) v.getTag();

        if (defaultColor == null) {
            defaultColor = holder.item1.getTextColors();
        }
        final T custom = entries.get(position);
        if (custom != null) {
            fillView(custom, holder);
        }
        return v;
    }

    protected abstract int getItemLayout();

    protected abstract void fillView(T custom, ViewHolder holder);

    protected abstract void cacheViews(ViewHolder holder, View v);

}

