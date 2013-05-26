package net.anzix.osm.upload;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomAdapter<T> extends BaseAdapter {

    private List<T> entries;

    private LayoutInflater vi;

    protected ColorStateList defaultColor;

    protected CustomAdapter(Context context, List<T> entries) {
        super();
        this.entries = entries;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        entries.clear();
    }

    public void add(T item) {
        entries.add(item);
    }

    public boolean remove(T s) {
        return entries.remove(s);
    }

    public void remove(int idx) {
        entries.remove(idx);
    }

    public static class ViewHolder {
        public TextView item1;
        public TextView item2;
        public TextView item3;
    }


    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public T getItem(int i) {
        return entries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
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

