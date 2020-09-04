/home/simha/pdf_viewer/mupdf-android-viewer-mini/lib/src/main/java/com/artifex/mupdf/mini/PageView.java

Change maxscale to 5

FROM:	maxScale = 2;
TO:		maxScale = 5;


/home/simha/pdf_viewer/mupdf-android-viewer-mini/lib/src/main/res/layout/document_activity.xml

Change background to black

FROM:

	<com.artifex.mupdf.mini.PageView android:id="@+id/page_view"
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			android:background="#0F0F0F"
			android:keepScreenOn="true"
			/>

TO:

	<com.artifex.mupdf.mini.PageView android:id="@+id/page_view"
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			android:background="#000000"
			android:keepScreenOn="true"
			/>