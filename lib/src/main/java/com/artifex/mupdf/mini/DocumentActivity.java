package com.artifex.mupdf.mini;

import com.artifex.mupdf.fitz.*;
import com.artifex.mupdf.fitz.android.*;
import com.artifex.mupdf.mini.Database.KrishnaDatabaseAdapter;
import com.artifex.mupdf.mini.Database.OutlineItem;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentActivity extends Activity
{
	private final String APP = "MuPDF";

	public final int NAVIGATE_REQUEST = 1;
	protected final int PERMISSION_REQUEST = 42;

	protected Worker worker;
	protected SharedPreferences prefs;

	protected Document doc;

	protected String key;
	protected String path;
	protected String mimetype;
	protected byte[] buffer;

	protected boolean hasLoaded;
	protected boolean isReflowable;
	protected boolean fitPage;
	protected String title;
	protected ArrayList<OutlineActivity.Item> flatOutline;
	protected ArrayList<OutlineItem> outlineItem;
	protected float layoutW, layoutH, layoutEm;
	protected float displayDPI;
	protected int canvasW, canvasH;
	protected float pageZoom;

	protected View currentBar;
	protected PageView pageView;
	protected View actionBar;
	protected TextView titleLabel;
	protected View searchButton;
	protected View searchBar;
	protected EditText searchText;
	protected View searchCloseButton;
	protected View searchBackwardButton;
	protected View searchForwardButton;
	protected View zoomButton;
	protected View layoutButton;
	protected PopupMenu layoutPopupMenu;
	protected View outlineButton;
	protected View navigationBar;
	protected TextView pageLabel;
	protected SeekBar pageSeekbar;

	protected int pageCount;
	protected int currentPage;
	protected int searchHitPage;
	protected String searchNeedle;
	protected boolean stopSearch;
	protected Stack<Integer> history;
	protected boolean wentBack;
	public KrishnaDatabaseAdapter krishnaDatabaseAdapter;
	public JSONArray jsonArray = new JSONArray();
	public JSONArray jsonArray2 = new JSONArray();
	public JSONArray jsonArray3 = new JSONArray();
	View changeLayout;
	ProgressDialog pd;
	int offset=1;
	int currentrandomnumber = -1;
	View overlay;
	View overlay_button;
	int toggle_overlay = -1;
	TextInputLayout textview_overlay;
	View buttom_overlay;
	View offsettoggle;
	View offsettoggle2;
	int jsonarraytype=1;
	int bookmark_level=0;

	private String toHex(byte[] digest) {
		StringBuilder builder = new StringBuilder(2 * digest.length);
		for (byte b : digest)
			builder.append(String.format("%02x", b));
		return builder.toString();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		pd = new ProgressDialog(DocumentActivity.this);
		krishnaDatabaseAdapter = new KrishnaDatabaseAdapter(this);
		SQLiteDatabase sqLiteDatabase = krishnaDatabaseAdapter.helper.getWritableDatabase();

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		displayDPI = metrics.densityDpi;

		setContentView(R.layout.document_activity);
		actionBar = findViewById(R.id.action_bar);
		searchBar = findViewById(R.id.search_bar);
		navigationBar = findViewById(R.id.navigation_bar);

		currentBar = actionBar;



		Uri uri = getIntent().getData();
		mimetype = getIntent().getType();
		key = uri.toString();
		if (uri.getScheme().equals("file")) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
			title = uri.getLastPathSegment();
			path = uri.getPath();
		} else {
			title = uri.toString();
			try {
				InputStream stm = getContentResolver().openInputStream(uri);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[16384];
				int n;
				while ((n = stm.read(buf)) != -1)
					out.write(buf, 0, n);
				out.flush();
				buffer = out.toByteArray();
				key = toHex(MessageDigest.getInstance("MD5").digest(buffer));
			} catch (IOException | NoSuchAlgorithmException x) {
				Log.e(APP, x.toString());
				Toast.makeText(this, x.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}

		titleLabel = (TextView)findViewById(R.id.title_label);
		titleLabel.setText(title);

		history = new Stack<Integer>();

		worker = new Worker(this);
		worker.start();

		prefs = getPreferences(Context.MODE_PRIVATE);
		layoutEm = prefs.getFloat("layoutEm", 6);
		fitPage = prefs.getBoolean("fitPage", false);
		currentPage = prefs.getInt(key, 0);
		searchHitPage = -1;
		hasLoaded = false;

		pageView = (PageView)findViewById(R.id.page_view);
		pageView.setActionListener(this);

		overlay = findViewById(R.id.overlay);
		overlay_button = findViewById(R.id.overlay_show);
		overlay_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(toggle_overlay == -1){
					overlay.setVisibility(View.VISIBLE);
					toggle_overlay = 0;
				}else{
					overlay.setVisibility(View.GONE);
					toggle_overlay = -1;
				}

			}
		});

		offsettoggle = findViewById(R.id.offsettoggle);
		offsettoggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(offset == 0) {
					offset = 1;
					Toast.makeText(DocumentActivity.this,"SHOW RANDOM CONTEXT",Toast.LENGTH_LONG).show();
				}else{
					offset = 0;
					Toast.makeText(DocumentActivity.this,"SHOW RANDOM SLOKA",Toast.LENGTH_LONG).show();
				}

			}
		});

		offsettoggle2 = findViewById(R.id.offsettoggle2);
		offsettoggle2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(jsonarraytype == 0) {
					jsonarraytype = 1;
					Toast.makeText(DocumentActivity.this,"SLOKA TO SLOKA JUMPING",Toast.LENGTH_LONG).show();
				}else{
					jsonarraytype = 0;
					Toast.makeText(DocumentActivity.this,"BOOKMARK TO BOOKMARK JUMPING",Toast.LENGTH_LONG).show();
				}

			}
		});

		textview_overlay = findViewById(R.id.textInputLayout2);
		buttom_overlay = findViewById(R.id.SEL_FILE_overlay);
		buttom_overlay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try{
					offset = Integer.parseInt(textview_overlay.getEditText().getText().toString());
				}catch(Exception e){
				}
				textview_overlay.getEditText().getText().clear();
				overlay.setVisibility(View.GONE);

			}
		});

		changeLayout= findViewById(R.id.change_layout);
		changeLayout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(pageView.change_layout == 0){
					pageView.change_layout=1;
				}else{
					pageView.change_layout=0;
				}
			}
		});


		pageLabel = (TextView)findViewById(R.id.page_label);
		pageSeekbar = (SeekBar)findViewById(R.id.page_seekbar);
		pageSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public int newProgress = -1;
			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
				if (fromUser) {
					newProgress = progress;
					pageLabel.setText((progress+1) + " / " + pageCount);
				}
			}
			public void onStartTrackingTouch(SeekBar seekbar) {}
			public void onStopTrackingTouch(SeekBar seekbar) {
				gotoPage(newProgress);
			}
		});

		searchButton = findViewById(R.id.search_button);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showSearch();
			}
		});
		searchText = (EditText)findViewById(R.id.search_text);
		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
					search(1);
					return true;
				}
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					search(1);
					return true;
				}
				return false;
			}
		});
		searchText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				resetSearch();
			}
		});
		searchCloseButton = findViewById(R.id.search_close_button);
		searchCloseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideSearch();
			}
		});
		searchBackwardButton = findViewById(R.id.search_backward_button);
		searchBackwardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		searchForwardButton = findViewById(R.id.search_forward_button);
		searchForwardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

		outlineButton = findViewById(R.id.outline_button);
		outlineButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DocumentActivity.this, OutlineActivity.class);
				//Intent intent = new Intent(DocumentActivity.this, OutlineActivityRV.class);
				Bundle bundle = new Bundle();
				bundle.putInt("POSITION", currentPage);
				//bundle.putSerializable("OUTLINE", flatOutline);
				intent.putExtras(bundle);
				startActivityForResult(intent, NAVIGATE_REQUEST);
			}
		});

		zoomButton = findViewById(R.id.zoom_button);
		zoomButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fitPage = !fitPage;
				loadPage();
			}
		});



		layoutButton = findViewById(R.id.layout_button);
		layoutPopupMenu = new PopupMenu(this, layoutButton);
		layoutPopupMenu.getMenuInflater().inflate(R.menu.layout_menu, layoutPopupMenu.getMenu());
		layoutPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				float oldLayoutEm = layoutEm;
				int id = item.getItemId();
				if (id == R.id.action_layout_6pt) layoutEm = 6;
				else if (id == R.id.action_layout_7pt) layoutEm = 7;
				else if (id == R.id.action_layout_8pt) layoutEm = 8;
				else if (id == R.id.action_layout_9pt) layoutEm = 9;
				else if (id == R.id.action_layout_10pt) layoutEm = 10;
				else if (id == R.id.action_layout_11pt) layoutEm = 11;
				else if (id == R.id.action_layout_12pt) layoutEm = 12;
				else if (id == R.id.action_layout_13pt) layoutEm = 13;
				else if (id == R.id.action_layout_14pt) layoutEm = 14;
				else if (id == R.id.action_layout_15pt) layoutEm = 15;
				else if (id == R.id.action_layout_16pt) layoutEm = 16;
				if (oldLayoutEm != layoutEm)
					relayoutDocument();
				return true;
			}
		});
		layoutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				layoutPopupMenu.show();
			}
		});
	}

	public void onPageViewSizeChanged(int w, int h) {
		pageZoom = 1;
		canvasW = w;
		canvasH = h;
		layoutW = canvasW * 72 / displayDPI;
		layoutH = canvasH * 72 / displayDPI;
		if (!hasLoaded) {
			hasLoaded = true;
			openDocument();
		} else if (isReflowable) {
			relayoutDocument();
		} else {
			loadPage();
		}
	}

	public void onPageViewZoomChanged(float zoom) {
		if (zoom != pageZoom) {
			pageZoom = zoom;
			loadPage();
		}
	}

	protected void openDocument() {
		worker.add(new Worker.Task() {
			boolean needsPassword;
			public void work() {
				Log.i(APP, "open document");
				if (path != null)
					doc = Document.openDocument(path);
				else
					doc = Document.openDocument(buffer, mimetype);
				needsPassword = doc.needsPassword();
			}
			public void run() {
				if (needsPassword)
					askPassword(R.string.dlog_password_message);
				else
					loadDocument();
			}
		});
	}

	protected void askPassword(int message) {
		final EditText passwordView = new EditText(this);
		passwordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		passwordView.setTransformationMethod(PasswordTransformationMethod.getInstance());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dlog_password_title);
		builder.setMessage(message);
		builder.setView(passwordView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				checkPassword(passwordView.getText().toString());
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		builder.create().show();
	}

	protected void checkPassword(final String password) {
		worker.add(new Worker.Task() {
			boolean passwordOkay;
			public void work() {
				Log.i(APP, "check password");
				passwordOkay = doc.authenticatePassword(password);
			}
			public void run() {
				if (passwordOkay)
					loadDocument();
				else
					askPassword(R.string.dlog_password_retry);
			}
		});
	}

	public void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat("layoutEm", layoutEm);
		editor.putBoolean("fitPage", fitPage);
		editor.putInt(key, currentPage);
		editor.apply();
	}

	//ORIGINAL CODE
/*	public void onBackPressed() {
		if (history.empty()) {
			super.onBackPressed();
		} else {
			currentPage = history.pop();
			loadPage();
		}
	}*/

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	public void onActivityResult(int request, int result, Intent data) {
		if (request == NAVIGATE_REQUEST && result >= RESULT_FIRST_USER)
			gotoPage(result - RESULT_FIRST_USER);
	}

	protected void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(searchText, 0);
	}

	protected void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
	}

	protected void resetSearch() {
		stopSearch = true;
		searchHitPage = -1;
		searchNeedle = null;
		pageView.resetHits();
	}

	protected void runSearch(final int startPage, final int direction, final String needle) {
		stopSearch = false;
		worker.add(new Worker.Task() {
			int searchPage = startPage;
			public void work() {
				if (stopSearch || needle != searchNeedle)
					return;
				for (int i = 0; i < 9; ++i) {
					Log.i(APP, "search page " + searchPage);
					Page page = doc.loadPage(searchPage);
					Quad[] hits = page.search(searchNeedle);
					page.destroy();
					if (hits != null && hits.length > 0) {
						searchHitPage = searchPage;
						break;
					}
					searchPage += direction;
					if (searchPage < 0 || searchPage >= pageCount)
						break;
				}
			}
			public void run() {
				if (stopSearch || needle != searchNeedle) {
					pageLabel.setText((currentPage+1) + " / " + pageCount);
				} else if (searchHitPage == currentPage) {
					loadPage();
				} else if (searchHitPage >= 0) {
					history.push(currentPage);
					currentPage = searchHitPage;
					loadPage();
				} else {
					if (searchPage >= 0 && searchPage < pageCount) {
						pageLabel.setText((searchPage+1) + " / " + pageCount);
						worker.add(this);
					} else {
						pageLabel.setText((currentPage+1) + " / " + pageCount);
						Log.i(APP, "search not found");
						Toast.makeText(DocumentActivity.this, getString(R.string.toast_search_not_found), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	protected void search(int direction) {
		hideKeyboard();
		int startPage;
		if (searchHitPage == currentPage)
			startPage = currentPage + direction;
		else
			startPage = currentPage;
		searchHitPage = -1;
		searchNeedle = searchText.getText().toString();
		if (searchNeedle.length() == 0)
			searchNeedle = null;
		if (searchNeedle != null)
			if (startPage >= 0 && startPage < pageCount)
				runSearch(startPage, direction, searchNeedle);
	}

	protected void loadDocument() {
		worker.add(new Worker.Task() {
			public void work() {
				try {
					Log.i(APP, "load document");
					String metaTitle = doc.getMetaData(Document.META_INFO_TITLE);
					if (metaTitle != null)
						title = metaTitle;
					isReflowable = doc.isReflowable();
					if (isReflowable) {
						Log.i(APP, "layout document");
						doc.layout(layoutW, layoutH, layoutEm);
					}
					pageCount = doc.countPages();
				} catch (Throwable x) {
					doc = null;
					pageCount = 1;
					currentPage = 0;
					throw x;
				}
			}
			public void run() {
				if (currentPage < 0 || currentPage >= pageCount)
					currentPage = 0;
				titleLabel.setText(title);
				if (isReflowable)
					layoutButton.setVisibility(View.VISIBLE);
				else
					zoomButton.setVisibility(View.VISIBLE);
				loadPage();
				loadOutline();
			}
		});
	}

	protected void relayoutDocument() {
		worker.add(new Worker.Task() {
			public void work() {
				try {
					long mark = doc.makeBookmark(doc.locationFromPageNumber(currentPage));
					Log.i(APP, "relayout document");
					doc.layout(layoutW, layoutH, layoutEm);
					pageCount = doc.countPages();
					currentPage = doc.pageNumberFromLocation(doc.findBookmark(mark));
				} catch (Throwable x) {
					pageCount = 1;
					currentPage = 0;
					throw x;
				}
			}
			public void run() {
				loadPage();
				loadOutline();
			}
		});
	}

	private void loadOutline() {
		pd.setTitle("Loading outline...");
		pd.setMessage("Please wait.");
		pd.setCancelable(false);
		pd.show();
		worker.add(new Worker.Task() {
			private void flattenOutline(Outline[] outline, String indent, int level) {
				for (Outline node : outline) {
					if (node.title != null)
					{
						int outlinePage = doc.pageNumberFromLocation(doc.resolveLink(node));
						//flatOutline.add(new OutlineActivity.Item(indent + node.title, node.uri, outlinePage));
						//outlineItem.add(new OutlineItem(indent + node.title, node.uri, outlinePage));
						try{
							JSONObject myjsonobj = new JSONObject();
							myjsonobj.put("title", indent + node.title);
							myjsonobj.put("uri", node.uri);
							myjsonobj.put("page", outlinePage);
							myjsonobj.put("level", level+1);
							jsonArray.put(myjsonobj);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					if (node.down != null){
						flattenOutline(node.down, indent + "    ",level+1);
					}else{
						if(!(bookmark_level > level+1)){
							bookmark_level=level+1;
						}

						int outlinePage = doc.pageNumberFromLocation(doc.resolveLink(node));
						try{
							JSONObject myjsonobj = new JSONObject();
							myjsonobj.put("title", node.title);
							myjsonobj.put("uri", node.uri);
							myjsonobj.put("page", outlinePage);
							myjsonobj.put("level", level+1);
							jsonArray3.put(myjsonobj);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}
			}
			public void work() {
				Log.i(APP, "load outline1cv");
				Outline[] outline = doc.loadOutline();
				if (outline != null) {
					//flatOutline = new ArrayList<OutlineActivity.Item>();
					//outlineItem = new ArrayList<OutlineItem>();
					flattenOutline(outline, "",0);
				} else {
					//flatOutline = null;
					//outlineItem = null;
					jsonArray = null;
				}
				if(jsonArray3 != null){
					try{
						for (int i = 0; i < jsonArray3.length(); ++i) {
							if(jsonArray3.getJSONObject(i).getInt("level") == bookmark_level){
								JSONObject myjsonobj = new JSONObject();
								myjsonobj.put("title",jsonArray3.getJSONObject(i).getString("title") );
								myjsonobj.put("uri",jsonArray3.getJSONObject(i).getString("uri"));
								myjsonobj.put("page",jsonArray3.getJSONObject(i).getInt("page"));
								myjsonobj.put("level",jsonArray3.getJSONObject(i).getInt("level") );
								jsonArray2.put(myjsonobj);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					jsonArray2=null;
				}



			}
			public void run() {
				if (jsonArray != null){
					String fileName= "content";
					String textToWrite = jsonArray.toString();
					FileOutputStream fileOutputStream;
					try {
						fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
						fileOutputStream.write(textToWrite.getBytes());
						fileOutputStream.flush ( );
						fileOutputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

					//krishnaDatabaseAdapter.deleteandcreatetable();
					//krishnaDatabaseAdapter.insertOutline(jsonArray.toString());
					Log.d("Krishna",jsonArray.toString());
					Log.d("Krishna",Integer.toString( bookmark_level));
					/*long id = krishnaDatabaseAdapter.insertOutline(
							outlineItem.get(i).title,
							outlineItem.get(i).uri,
							outlineItem.get(i).page
					);
					Log.d("Gaura",Long.toString(id));*/
					outlineButton.setVisibility(View.VISIBLE);
				}
				pd.dismiss();
			}
		});
	}

	protected void loadPage() {
		final int pageNumber = currentPage;
		final float zoom = pageZoom;
		stopSearch = true;
		worker.add(new Worker.Task() {
			public Bitmap bitmap;
			public Link[] links;
			public Quad[] hits;
			public void work() {
				try {
					Log.i(APP, "load page " + pageNumber);
					Page page = doc.loadPage(pageNumber);
					Log.i(APP, "draw page " + pageNumber + " zoom=" + zoom);
					Matrix ctm;
					if (fitPage)
						ctm = AndroidDrawDevice.fitPage(page, canvasW, canvasH);
					else
						ctm = AndroidDrawDevice.fitPageWidth(page, canvasW);
					links = page.getLinks();
					if (links != null)
						for (Link link : links)
							link.bounds.transform(ctm);
					if (searchNeedle != null) {
						hits = page.search(searchNeedle);
						if (hits != null)
							for (Quad hit : hits)
								hit.transform(ctm);
					}
					if (zoom != 1)
						ctm.scale(zoom);
					bitmap = AndroidDrawDevice.drawPage(page, ctm);
				} catch (Throwable x) {
					Log.e(APP, x.getMessage());
				}
			}
			public void run() {
				if (bitmap != null)
					pageView.setBitmap(bitmap, zoom, wentBack, links, hits);
				else
					pageView.setError();
				pageLabel.setText((currentPage+1) + " / " + pageCount);
				pageSeekbar.setMax(pageCount - 1);
				pageSeekbar.setProgress(pageNumber);
				wentBack = false;
			}
		});
	}

	protected void showSearch() {
		currentBar = searchBar;
		actionBar.setVisibility(View.GONE);
		searchBar.setVisibility(View.VISIBLE);
		searchBar.requestFocus();
		showKeyboard();
	}

	protected void hideSearch() {
		currentBar = actionBar;
		actionBar.setVisibility(View.VISIBLE);
		searchBar.setVisibility(View.GONE);
		hideKeyboard();
		resetSearch();
	}

	public void toggleUI() {
		if (navigationBar.getVisibility() == View.VISIBLE) {
			currentBar.setVisibility(View.GONE);
			navigationBar.setVisibility(View.GONE);
			if (currentBar == searchBar)
				hideKeyboard();
		} else {
			currentBar.setVisibility(View.VISIBLE);
			navigationBar.setVisibility(View.VISIBLE);
			if (currentBar == searchBar) {
				searchBar.requestFocus();
				showKeyboard();
			}
		}
	}

	public void goBackward() {
		if (currentPage > 0) {
			wentBack = true;
			currentPage --;
			loadPage();
		}
	}

	public void goForward() {
		if (currentPage < pageCount - 1) {
			currentPage ++;
			loadPage();
		}
	}

	public void outline2(){
		Intent intent = new Intent(DocumentActivity.this, OutlineActivity.class);
		//Intent intent = new Intent(DocumentActivity.this, OutlineActivityRV.class);
		Bundle bundle = new Bundle();
		bundle.putInt("POSITION", currentPage);
		//bundle.putSerializable("OUTLINE", flatOutline);
		intent.putExtras(bundle);
		startActivityForResult(intent, NAVIGATE_REQUEST);
	}

	public void goBackward2() {
		int f1 = -1;
		int page = -1;
		if (currentPage > 0) {
			wentBack = true;
			JSONArray jsonArray4 = new JSONArray();
			if (jsonarraytype == 0){
				jsonArray4 = jsonArray;
			}else{
				jsonArray4 = jsonArray2;
			}
			if (jsonArray4 != null) {
				for (int i = 0; i < jsonArray4.length(); ++i) {
					try {
						if (jsonArray4.getJSONObject(i).getInt("page") < currentPage){
							if( i-1 < 0){
								page = jsonArray4.getJSONObject(0).getInt("page");
							}else{
								if(jsonArray4.getJSONObject(i).getInt("page") > page){
									page = jsonArray4.getJSONObject(i).getInt("page");
								} else {
									page = jsonArray4.getJSONObject(i-1).getInt("page");
								}
							}
						}

					} catch (JSONException e) {

					}
				}
			}
			if (page < 0){
				currentPage --;
			}else{
				currentPage = page;
			}
			loadPage();
		}
	}

	public void goForward2() {
		int page = -1;
		JSONArray jsonArray4 = new JSONArray();
		if (jsonarraytype == 0){
			jsonArray4 = jsonArray;
		}else{
			jsonArray4 = jsonArray2;
		}
		if (currentPage < pageCount - 1) {
			if (jsonArray4 != null) {
				for (int i = 0; i < jsonArray4.length(); ++i) {
					try {
						if (jsonArray4.getJSONObject(i).getInt("page") <= currentPage){
							page = jsonArray4.getJSONObject(i+1).getInt("page");
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			if (page < 0){
				currentPage++;
			}else{
				currentPage = page;
			}
			loadPage();
		}
	}

	public void setFitPage() {
		fitPage = !fitPage;
		loadPage();
	}

	public void gotoPage(int p) {
		if (p >= 0 && p < pageCount && p != currentPage) {
			history.push(currentPage);
			currentPage = p;
			loadPage();
		}
	}

	public void goToRandom(){
		if (jsonArray2 != null) {
			int random_pagenumber;
			int random_number;
			int length = jsonArray2.length();
			if(currentrandomnumber > 0){
				Random r = new Random();
				random_number = r.nextInt(jsonArray2.length());
				while(!(random_number > (currentrandomnumber + length/10) || random_number < (currentrandomnumber - length/10))){
					random_number = r.nextInt(jsonArray2.length());
				}
				random_pagenumber = -1;
			}else{
				Random r = new Random();
				random_number = r.nextInt(jsonArray2.length());
				random_pagenumber = -1;
			}
			try {
				random_pagenumber = jsonArray2.getJSONObject(random_number).getInt("page");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (random_pagenumber == -1 || random_pagenumber == 0) {
				gotoPage(0);
			} else {
				gotoPage(random_pagenumber-offset);
				currentrandomnumber = random_number;
			}
		} else {
			int pageCount = doc.countPages();
			Random r = new Random();
			int random_pagenumber = r.nextInt(pageCount);
			if (random_pagenumber == -1 || random_pagenumber == 0) {
				gotoPage(0);
			} else {
				gotoPage(random_pagenumber - offset);
			}
		}

	}

	public void gotoPage(String uri) {
		gotoPage(doc.pageNumberFromLocation(doc.resolveLink(uri)));
	}

	public void gotoURI(String uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); // FLAG_ACTIVITY_NEW_DOCUMENT in API>=21
		try {
			startActivity(intent);
		} catch (Throwable x) {
			Log.e(APP, x.getMessage());
			Toast.makeText(DocumentActivity.this, x.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
