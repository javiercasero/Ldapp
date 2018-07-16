package org.tfgdomain.ldapp;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public abstract class AdminContent extends BaseAdapter{
    private ArrayList<?> elementsList;
    private int R_layout_IdView;
    private Context context;

    public AdminContent(Context context, int R_layout_IdView, ArrayList<?> elementsList) {
        super();
        this.context = context;
        this.elementsList = elementsList;
        this.R_layout_IdView = R_layout_IdView;

    }

    @Override
    public View getView(final int index, View view, final ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R_layout_IdView, null);
        }

        onElementsList (elementsList.get(index), view);
        /*
        ImageView img = (ImageView)view.findViewById(R.id.imageView_icon);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView)viewGroup).performItemClick(v, index, 0);
            }
        });*/
        ListElement listElement = getListElement(index);
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkbox_filter);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox.setTag(index);
        checkBox.setChecked(listElement.getChecked());

        return view;
    }

    ListElement getListElement(int index) {
        return (ListElement)getItem(index);
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            getListElement((Integer)buttonView.getTag()).setChecked(isChecked);
            Log.i("AdminContent", "checked");
            notifyDataSetChanged();
        }
    };

    @Override
    public int getCount(){
        if (elementsList!=null){
            return elementsList.size();
        } else {return 0;}

    }

    @Override
    public Object getItem(int index) {
        return elementsList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    public abstract void onElementsList (Object element, View view);
}
