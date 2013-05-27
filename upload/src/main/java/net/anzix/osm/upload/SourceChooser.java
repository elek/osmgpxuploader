package net.anzix.osm.upload;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListActivity;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.GpxDao;
import net.anzix.osm.upload.data.Source;
import net.anzix.osm.upload.source.SourceHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Choose a source type during the source creation.
 */
public class SourceChooser extends SherlockListActivity {

    GpxUploadApplication app;
    CustomAdapter<SourceHandler> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GpxUploadApplication) getApplication();

        setContentView(R.layout.sourcechoose_list);

        fillData();

        registerForContextMenu(getListView());


    }

    private void fillData() {
        GpxUploadApplication app = (GpxUploadApplication) getApplication();
        List<SourceHandler> handlers = new ArrayList<SourceHandler>();
        handlers.addAll(app.getSourceHandlers());
        setListAdapter(adapter = new CustomAdapter<SourceHandler>(this, handlers) {
            protected int getItemLayout() {
                return R.layout.sourcechoose_item;
            }

            protected void fillView(SourceHandler custom, ViewHolder holder) {
                holder.item1.setText(custom.getName());
                holder.item2.setText(custom.getDescription());
            }

            protected void cacheViews(ViewHolder holder, View v) {
                holder.item1 = (TextView) v.findViewById(R.id.name);
                holder.item2 = (TextView) v.findViewById(R.id.description);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            File f = new File(filePath);
            if (!f.isDirectory()) {
                Toast.makeText(this, "It's a file and not a directory!", Toast.LENGTH_SHORT);
            }
            if (f.exists()) {
                Source s = new Source();
                s.setType("dir");
                String loc = f.getAbsolutePath();
                try {
                    loc = f.getCanonicalPath();
                } catch (IOException e) {
                    Log.e("OSM", "Can't get canonical path ", e);
                }
                s.setLocation(loc);
                app.getDaoSession().getSourceDao().insert(s);
                app.syncNeeded = true;
                finish();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        SourceHandler handler = adapter.getItem((int) id);
        if (handler.getKey().equals("dir")) {
            Intent intent = new Intent(getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, "/sdcard");
            intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
            intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
            startActivityForResult(intent, 1);
        } else if (handler.getKey().equals("sygic")) {
            Source s = new Source();
            s.setType("sygic");
            s.setLocation("Sygic travelbook");
            app.getDaoSession().getSourceDao().insert(s);
            app.syncNeeded = true;
            finish();
        }
    }


}
