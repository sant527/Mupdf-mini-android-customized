package com.artifex.mupdf.mini;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;

import com.artifex.mupdf.mini.Database.KrishnaDatabaseAdapter;
import com.artifex.mupdf.mini.Database.OutlineItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by simha on 8/9/20.
 */

public class OutlineActivityRV extends Activity {

    public RVAdapterOutline rvAdapterOutline;
    public ArrayList<OutlineItem> outlineItemList;
    public KrishnaDatabaseAdapter krishnaDatabaseAdapter;
    public Bundle bundle;

    private RecyclerView rv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.outline_activity);

        krishnaDatabaseAdapter = new KrishnaDatabaseAdapter(this);
        SQLiteDatabase sqLiteDatabase = krishnaDatabaseAdapter.helper.getWritableDatabase();

        bundle = getIntent().getExtras();
        int currentPage = bundle.getInt("POSITION");
        String jsonArrayString = krishnaDatabaseAdapter.getDetailsasarrayOutlinetable();

        outlineItemList = new ArrayList<OutlineItem>();
        int found = -1;
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); ++i) {
                OutlineItem outlineItem = new OutlineItem(
                        jsonArray.getJSONObject(i).getString("title"),
                        jsonArray.getJSONObject(i).getString("uri"),
                        jsonArray.getJSONObject(i).getInt("page"),
                        -1
                );
                if (jsonArray.getJSONObject(i).getInt("page") <= currentPage)
                    found = i;
                outlineItemList.add(outlineItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (found >= 0){
            rvAdapterOutline = new RVAdapterOutline(outlineItemList,OutlineActivityRV.this,found);
            rv = (RecyclerView) findViewById(R.id.rview);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);
            rv.setAdapter(rvAdapterOutline);
            llm.scrollToPositionWithOffset(found, 200);
        }
        else {
            rvAdapterOutline = new RVAdapterOutline(outlineItemList,OutlineActivityRV.this,null);
            rv = (RecyclerView) findViewById(R.id.rview);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);
            rv.setAdapter(rvAdapterOutline);
        }

    }
}
