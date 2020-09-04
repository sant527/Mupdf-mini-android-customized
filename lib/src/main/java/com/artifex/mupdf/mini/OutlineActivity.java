package com.artifex.mupdf.mini;

import android.app.ListActivity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import com.artifex.mupdf.mini.Database.KrishnaDatabaseAdapter;
import com.artifex.mupdf.mini.Database.OutlineItem;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OutlineActivity extends ListActivity
{
	public static class Item implements Serializable {
		public String title;
		public String uri;
		public int page;
		public Item(String title, String uri, int page) {
			this.title = title;
			this.uri = uri;
			this.page = page;
		}
		public String toString() {
			return title;
		}
	}

	protected ArrayAdapter<OutlineItem> adapter;
	public ArrayList<OutlineItem> outlineItemList;
	public KrishnaDatabaseAdapter krishnaDatabaseAdapter;
	public Bundle bundle;
	public ListView list;
	int found = -1;
	public YourAdapter yourAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		krishnaDatabaseAdapter = new KrishnaDatabaseAdapter(this);
		SQLiteDatabase sqLiteDatabase = krishnaDatabaseAdapter.helper.getWritableDatabase();

		yourAdapter= new YourAdapter(this);
		setListAdapter(yourAdapter);

/*		adapter= new ArrayAdapter<OutlineItem>(this,android.R.layout.simple_list_item_1);
		setListAdapter(adapter);*/

		bundle = getIntent().getExtras();


		int currentPage = bundle.getInt("POSITION");
		//ArrayList<Item> outline1cv = (ArrayList<Item>)bundle.getSerializable("OUTLINE");

		//String jsonArrayString = krishnaDatabaseAdapter.getDetailsasarrayOutlinetable();
		// This has a limit of 1MB so use file

		String jsonArrayString = null;
		String fileName= "content";
		try{
			FileInputStream fis;
			fis = openFileInput(fileName);
			StringBuffer fileContent = new StringBuffer("");

			byte[] buffer = new byte[1024];
			int n;
			while ((n = fis.read(buffer)) != -1)
			{
				fileContent.append(new String(buffer, 0, n));
			}
			jsonArrayString = fileContent.toString();
		} catch (FileNotFoundException e) {
			// exception handling
		} catch (IOException e) {
			// exception handling
		}


		try {
			JSONArray jsonArray = new JSONArray(jsonArrayString);
/*			for (int i = 0; i < jsonArray.length(); ++i) {
				if (jsonArray.getJSONObject(i).getInt("page") <= currentPage)
					found = i;
			}*/
			for (int i = 0; i < jsonArray.length(); ++i) {
				if (found < 0 && jsonArray.getJSONObject(i).getInt("page") >= currentPage)
					found = i;
				OutlineItem outlineItem;
/*				outlineItem = new OutlineItem(
						jsonArray.getJSONObject(i).getString("title"),
						jsonArray.getJSONObject(i).getString("uri"),
						jsonArray.getJSONObject(i).getInt("page"),
						found
				);*/
				if (found > 0 && i == found ){
					outlineItem = new OutlineItem(
							jsonArray.getJSONObject(i).getString("title"),
							jsonArray.getJSONObject(i).getString("uri"),
							jsonArray.getJSONObject(i).getInt("page"),
							found
					);
				}
				else{
					outlineItem = new OutlineItem(
							jsonArray.getJSONObject(i).getString("title"),
							jsonArray.getJSONObject(i).getString("uri"),
							jsonArray.getJSONObject(i).getInt("page"),
							-1
					);
				}
				yourAdapter.add(outlineItem);
				//adapter.add(outlineItem);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		setSelection(found-8);
		list = (ListView)findViewById(android.R.id.list);

		list.setFastScrollAlwaysVisible(true);
		list.setScrollBarSize(200);

		setFastScrollThumbImage(list,getResources().getDrawable(R.drawable.fastscroll_thumb));


		//list.getChildAt(getSelectedItemPosition()).setBackgroundColor(Color.RED);
		//ListView list2 = (ListView)findViewById(android.R.id.list);
		//list2.getChildAt(found);

		//list.setSelector(R.drawable.listselector);
		//list.getChildAt(list.getSelectedItemPosition());

		//list.setFastScrollEnabled(true);


//		View v=getListView().getChildAt(getSelectedItemPosition());
//		((TextView) v.findViewById(R.id.)).setTextColor(Color.RED);
		//getListView().getChildAt(getSelectedItemPosition()).setBackgroundColor(Color.RED);


	}

	public static boolean setFastScrollThumbImage(AbsListView listView, Drawable thumb) {
		try {
			Field f;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				f = AbsListView.class.getDeclaredField("mFastScroll");
			} else {
				f = AbsListView.class.getDeclaredField("mFastScroller");
			}
			f.setAccessible(true);
			Object o = f.get(listView);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
				f = f.getType().getDeclaredField("mThumbImage");
				f.setAccessible(true);
				ImageView iv = (ImageView) f.get(o);
				iv.setImageDrawable(thumb);
			} else {
				f = f.getType().getDeclaredField("mThumbDrawable");
				f.setAccessible(true);
				Drawable drawable = (Drawable) f.get(o);
				drawable = thumb;
				f.set(o, drawable);
			}
			return true;
		} catch (Exception ignored) {
		}
		return false;
	}

	private class YourAdapter extends ArrayAdapter<OutlineItem> {

		public ViewHolder mViewHolder;
		private final LayoutInflater mLayoutInflater;

		YourAdapter(OutlineActivity outlineActivity) {
			super(OutlineActivity.this, 0);
			mLayoutInflater = LayoutInflater.from(outlineActivity);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				// Inflate your view
				convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
				mViewHolder = new ViewHolder();
				mViewHolder.title = (TextView) convertView.findViewById(android.R.id.text1);

				convertView.setTag(mViewHolder);
			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
				mViewHolder.title.setBackgroundColor(getResources().getColor(android.R.color.white));
				mViewHolder.title.setTypeface(null, Typeface.NORMAL);
				mViewHolder.title.setTextSize(17);
			}

			final OutlineItem outlineItem = getItem(position);

			mViewHolder.title.getTextSize();


			String input;
			Pattern pattern1;
			Pattern pattern2;
			Pattern pattern3;
			Pattern pattern4;
			Pattern pattern5;
			input = outlineItem.title ;

			pattern1 = Pattern.compile("^(?! ).*");
			pattern2 = Pattern.compile("^(?!     ).*");

			if (pattern1.matcher(input).matches()) {
				if (outlineItem.found > 0){
					mViewHolder.title.setBackgroundColor(Color.rgb(226, 100, 11));
				}
				mViewHolder.title.setTypeface(null, Typeface.BOLD);
				mViewHolder.title.setTextSize(20);
			} else if(pattern2.matcher(input).matches()){
				if (outlineItem.found > 0){
					mViewHolder.title.setBackgroundColor(Color.rgb(226, 100, 11));
				}
				mViewHolder.title.setTypeface(null, Typeface.BOLD_ITALIC);
				mViewHolder.title.setTextSize(18);
			} else{
				if (outlineItem.found > 0){
					mViewHolder.title.setBackgroundColor(Color.rgb(226, 100, 11));
				}
			}

			mViewHolder.title.setText(outlineItem.title);

			return convertView;
		}
	}


	private static class ViewHolder {
		TextView title;
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		OutlineItem outlineItem = yourAdapter.getItem(position);
		setResult(RESULT_FIRST_USER + outlineItem.page);
		finish();
	}
}
