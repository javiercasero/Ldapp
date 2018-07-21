package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase MainContent.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

abstract class MainContent extends BaseAdapter{
    private final ArrayList<?> userDomainList;
    private final int R_layout_IdView;
    private final Context context;

    public MainContent(Context context, int R_layout_IdView, ArrayList<?> userDomainList) {
        super();
        this.context = context;
        this.userDomainList = userDomainList;
        this.R_layout_IdView = R_layout_IdView;
    }

    @Override
    public View getView(final int index, View view, final ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R_layout_IdView, null);
        }
        onUserDomainList (userDomainList.get(index), view);
        ImageView img = view.findViewById(R.id.imageView_icon);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView)viewGroup).performItemClick(v, index, index);
            }
        });

        return view;
    }

    @Override
    public int getCount(){
        return userDomainList.size();
    }

    @Override
    public Object getItem(int index) {
        return userDomainList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    protected abstract void onUserDomainList(Object userDomain, View view);

}
