package org.tfgdomain.ldapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AdminActivity extends AppCompatActivity{
    private Filter fAdmin;
    private String user, domain, password;
    //private BaseDN dominioDN;
    private ArrayList<ListElement> arrayListElements;
    private static final int sourceId = 4;
    //private LDAPConnection c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Spinner
        Spinner filterSpinner = (Spinner)findViewById(R.id.filter_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = parent.getItemAtPosition(position).toString();
                Log.d("filter: ", String.valueOf(position));
                switch (position) {
                    case 0:
                        Log.d("filter0: ", "cuentas bloqueadas");
                        try {
                            fAdmin = Filter.create("(&(&(&(objectCategory=person)(objectClass=user)(lockoutTime>=1))))");

                        } catch (LDAPException e) {

                        }
                        break;
                    case 1:
                        Log.d("filter: ", String.valueOf(position));
                            fAdmin = Filter.createEqualityFilter("userAccountControl", "514");

                        break;
                    default:
                        break;


                }
                runFilter(fAdmin);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /* Comento para probar funcion
        Intent intent = getIntent();
        user = intent.getStringExtra("user");
        domain = intent.getStringExtra("domain");
        password = intent.getStringExtra("password");
        String userDN = user+"@"+domain;
        dominioDN = new BaseDN();

        try {
            fAdmin = Filter.create("(&(&(&(objectCategory=person)(objectClass=user)(lockoutTime>=1))))");
        } catch (LDAPException e) {

        }
        if(MyLdap.c.isConnected()){
            Log.d("Ldapconnected", "YES");
            MyLdap myLdap = new MyLdap(AdminActivity.this, sourceId);
            try {
                arrayListElements = myLdap.new Search().execute(fAdmin).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //MyLdap.Bind ldapBind = myLdap.new Bind();
            //myLdap.new Bind().execute(user,domain,password);
        } */ //Comento cierro para probar funcion
        /*
        try {
            MyLdap.c.connect(domain, 636);

            BindRequest bindRequest = new SimpleBindRequest(userDN,password);
            BindResult bindResult = c.bind(bindRequest);

            fAdmin = Filter.create("(&(&(&(objectCategory=person)(objectClass=user)(lockoutTime>=1))))");
            SearchResult searchResult = c.search(dominioDN.getDN(domain), SearchScope.SUB, fAdmin,"sAMAccountName", "cn", "distinguishedName", "userPrincipalName");
            Log.d("Num. resultados: ", String.valueOf(searchResult.getEntryCount()));
        } catch (LDAPException e) {
            e.printStackTrace();
        }
        MyLdap myLdap = new MyLdap(builder.getContext());
        //MyLdap.Bind ldapBind = myLdap.new Bind();
        myLdap.new Bind().execute(user,domain,password);

        */
/*
        ListView listView = (ListView)findViewById(R.id.listAdminFilter);

        listView.setAdapter(new AdminContent(this, R.layout.admin_filter, arrayListElements) {
            @Override
            public void onElementsList(Object element, View view) {

                TextView text_name = (TextView)view.findViewById(R.id.textView_name);
                text_name.setText(((ListElement) element).getTextPrincipal());

                TextView text_email = (TextView)view.findViewById(R.id.textView_email);
                text_email.setText(((ListElement) element).getTextSecondary());
            }
        });*/


    }
    private void runFilter(Filter filter) {

        if(MyLdap.c.isConnected()){
            Log.d("Ldapconnected", "YES");
            MyLdap myLdap = new MyLdap(AdminActivity.this, sourceId);


            try {
                arrayListElements = myLdap.new Search().execute(filter).get();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }



            ListView listView = (ListView)findViewById(R.id.listAdminFilter);


            listView.setAdapter(new AdminContent(this, R.layout.admin_filter, arrayListElements) {
                @Override
                public void notifyDataSetChanged() {
                    super.notifyDataSetChanged();
                }

                @Override
                public void onElementsList(Object element, View view) {

                    TextView text_name = (TextView)view.findViewById(R.id.textView_name);
                    text_name.setText(((ListElement) element).getTextPrincipal());

                    TextView text_email = (TextView)view.findViewById(R.id.textView_email);
                    text_email.setText(((ListElement) element).getTextSecondary());
                }
            });
        } else {
            Log.d("ldap: ", "not connected");
        }
    }
}
