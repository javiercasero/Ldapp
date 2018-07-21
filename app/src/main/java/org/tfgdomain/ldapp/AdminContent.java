package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase AdminContent.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

abstract class AdminContent extends BaseAdapter{
    private final ArrayList<?> elementsList;
    private final int R_layout_IdView;
    private final Context context;

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
            assert layoutInflater != null;
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
        CheckBox checkBox = view.findViewById(R.id.checkbox_filter);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox.setTag(index);
        checkBox.setChecked(listElement.getChecked());

        return view;
    }

    ListElement getListElement(int index) {
        return (ListElement)getItem(index);
    }

    private final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

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

    protected abstract void onElementsList(Object element, View view);
}
