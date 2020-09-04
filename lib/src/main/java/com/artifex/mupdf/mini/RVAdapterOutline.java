package com.artifex.mupdf.mini;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artifex.mupdf.mini.Database.OutlineItem;

import java.util.List;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_FIRST_USER;

/**
 * Created by simha on 8/9/20.
 */

public class RVAdapterOutline extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    Activity activity;
    List<OutlineItem> outlineItemList;
    final int HEADING = 0;
    final int HEADINGFOUND = 1;
    final int NORMALFOUND = 2;
    Integer found;

    // This is a constructor
    public RVAdapterOutline(List<OutlineItem> outlineItemList, Activity activity, Integer found){
        this.activity = activity;
        this.outlineItemList = outlineItemList;
        this.found = found;
    }

    //CustomViewholder extends RecyclerView.ViewHolder
    public class CusVH0 extends RecyclerView.ViewHolder {

        CardView cv;
        TextView text;

        CusVH0(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            text = (TextView)itemView.findViewById(R.id.textoutline);
        }
    }


    //CustomViewholder extends RecyclerView.ViewHolder
    public class CusVH1 extends RecyclerView.ViewHolder {

        CardView cv;
        TextView text;

        CusVH1(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            text = (TextView)itemView.findViewById(R.id.textoutline);
        }
    }

    //CustomViewholder extends RecyclerView.ViewHolder
    public class CusVH2 extends RecyclerView.ViewHolder {

        CardView cv;
        TextView text;

        CusVH2(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            text = (TextView)itemView.findViewById(R.id.textoutline);
        }
    }

    //CustomViewholder extends RecyclerView.ViewHolder
    public class CusVH3 extends RecyclerView.ViewHolder {

        CardView cv;
        TextView text;

        CusVH3(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            text = (TextView)itemView.findViewById(R.id.textoutline);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HEADING:{
                View v = inflater.inflate(R.layout.outline0cv, parent, false);
                // an inner class inside a non static method can be instantiated like this, it is same as
                // RVAdap.CusVH viewHolder = this.new CusVH(v);
                viewHolder = new CusVH0(v);
                break;
            }
            case HEADINGFOUND:{
                View v = inflater.inflate(R.layout.outline1cv, parent, false);
                // an inner class inside a non static method can be instantiated like this, it is same as
                // RVAdap.CusVH viewHolder = this.new CusVH(v);
                viewHolder = new CusVH1(v);
                break;
            }
            case NORMALFOUND:{
                View v = inflater.inflate(R.layout.outline2cv, parent, false);
                // an inner class inside a non static method can be instantiated like this, it is same as
                // RVAdap.CusVH viewHolder = this.new CusVH(v);
                viewHolder = new CusVH2(v);
                break;
            }
            default:{
                View v = inflater.inflate(R.layout.outline3cv, parent, false);
                // an inner class inside a non static method can be instantiated like this, it is same as
                // RVAdap.CusVH viewHolder = this.new CusVH(v);
                viewHolder = new CusVH3(v);
                break;
            }
        }

        return viewHolder;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        // we can use viewHolder.getItemViewType() or getItemViewType(position)
        switch (viewHolder.getItemViewType()) {
            case HEADING:{
                //NOTE HERE viewHolder is superclass reference of subclass object of CusVH. To access all the methods and fields of the subclass then we have to type cast it by adding (CusVH) before
                CusVH0 cusvh0 = (CusVH0) viewHolder;
                cusvh0.text.setText(outlineItemList.get(position).title);
                cusvh0.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.setResult(RESULT_FIRST_USER + outlineItemList.get(position).page);
                        activity.finish();
                    }
                });
            }
            case HEADINGFOUND:{
                //NOTE HERE viewHolder is superclass reference of subclass object of CusVH. To access all the methods and fields of the subclass then we have to type cast it by adding (CusVH) before
                CusVH1 cusvh1 = (CusVH1) viewHolder;
                cusvh1.text.setText(outlineItemList.get(position).title);
                cusvh1.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.setResult(RESULT_FIRST_USER + outlineItemList.get(position).page);
                        activity.finish();
                    }
                });
                break;
            }
            case NORMALFOUND:{
                //NOTE HERE viewHolder is superclass reference of subclass object of CusVH. To access all the methods and fields of the subclass then we have to type cast it by adding (CusVH) before
                CusVH2 cusvh2 = (CusVH2) viewHolder;
                cusvh2.text.setText(outlineItemList.get(position).title);
                cusvh2.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.setResult(RESULT_FIRST_USER + outlineItemList.get(position).page);
                        activity.finish();
                    }
                });
                break;
            }
            default:{
                //NOTE HERE viewHolder is superclass reference of subclass object of CusVH. To access all the methods and fields of the subclass then we have to type cast it by adding (CusVH) before
                CusVH3 cusvh3 = (CusVH3) viewHolder;
                cusvh3.text.setText(outlineItemList.get(position).title);
                cusvh3.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.setResult(RESULT_FIRST_USER + outlineItemList.get(position).page);
                        activity.finish();
                    }
                });
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return outlineItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        final String input = outlineItemList.get(position).title ;
        final Pattern pattern = Pattern.compile("^(?! ).*");
        if (pattern.matcher(input).matches()) {
            if (found.equals(position)){
                return HEADINGFOUND;
            }
            return  HEADING;
        }
        if (found.equals(position)){
            return NORMALFOUND;
        }
        return 1000;
    }
}
