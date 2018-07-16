package org.tfgdomain.ldapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
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
    private FloatingActionButton fab;
    private int filterPosition;

    //private LDAPConnection c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fab = (FloatingActionButton) findViewById(R.id.fab_admin);


        //Spinner
        Spinner filterSpinner = (Spinner)findViewById(R.id.filter_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String filter = parent.getItemAtPosition(position).toString();
                Log.d("filter: ", String.valueOf(position));
                filterPosition = position;
                //fab.setVisibility(View.INVISIBLE);
                switch (position) {
                    case 0:
                        Log.d("filter0: ", "cuentas bloqueadas");

                        try {
                            fAdmin = Filter.create("(&(objectCategory=person)(objectClass=user)(lockoutTime>=1))");

                        } catch (LDAPException e) {

                        }
                        break;
                    case 1:
                        Log.d("filter: ", String.valueOf(position));
                            //fAdmin = Filter.createEqualityFilter("userAccountControl", "514");
                            try {
                                fAdmin = Filter.create("(&(objectCategory=person)(objectClass=user)(userAccountControl:1.2.840.113556.1.4.803:=2))");
                            } catch (LDAPException e) {

                            }
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


        //Button

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!arrayListElements.isEmpty()) {
                    LDAPResult ldapResult = null;

                    switch (filterPosition) {
                        case 0:
                            Log.d("filter: ", "bloqueada");
                            if(MyLdap.c.isConnected()) {
                                MyLdap myLdap = new MyLdap(AdminActivity.this, sourceId);
                                try {
                                    for (int i=0;i<arrayListElements.size();i++) {
                                        if (arrayListElements.get(i).getChecked()) {
                                            Log.i("AdminActivity",arrayListElements.get(i).getTextPrincipal());
                                            ldapResult = myLdap.new ModUser().execute(arrayListElements.get(i).getUserDN(),String.valueOf(filterPosition)).get();
                                        }
                                    }

                                    if (ldapResult.getResultCode().equals(ResultCode.SUCCESS)) {
                                        Log.i("AdminActivity","Usuarios desbloqueados");
                                        Toast.makeText(AdminActivity.this, R.string.unlocked_ok,Toast.LENGTH_LONG).show();
                                        runFilter(fAdmin);
                                    } else {
                                        Toast.makeText(AdminActivity.this, R.string.unlocked_nook,Toast.LENGTH_LONG).show();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                            }
                            break;
                        case 1:
                            Log.d("filter: ", "deshabilitada");
                            if(MyLdap.c.isConnected()) {
                                MyLdap myLdap = new MyLdap(AdminActivity.this, sourceId);

                                try {
                                    for (int i=0;i<arrayListElements.size();i++) {
                                        if (arrayListElements.get(i).getChecked()) {
                                            Log.i("AdminActivity",arrayListElements.get(i).getTextPrincipal());
                                            ldapResult = myLdap.new ModUser().execute(arrayListElements.get(i).getUserDN(),String.valueOf(filterPosition),String.valueOf(arrayListElements.get(i).getAccountControl())).get();
                                        }
                                    }

                                    if (ldapResult.getResultCode().equals(ResultCode.SUCCESS)) {
                                        Log.i("AdminActivity","Usuarios deshabilitados");
                                        Toast.makeText(AdminActivity.this, R.string.enabled_ok,Toast.LENGTH_LONG).show();
                                        runFilter(fAdmin);
                                    } else {
                                        Toast.makeText(AdminActivity.this, R.string.enabled_nook,Toast.LENGTH_LONG).show();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        });



    }
    private void runFilter(Filter filter) {
        fab.setVisibility(View.INVISIBLE);
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
                    Log.i("AdminActivity","datasetchanged");
                    int i=0;
                    boolean elementChecked = false;
                    while ((i<getCount())&& !(elementChecked)){
                        if(getListElement(i).getChecked()){
                            elementChecked = true;
                        }
                        i++;
                    }
                    if(elementChecked){
                        Log.i("AdminActivity","checked");
                        fab.setVisibility(View.VISIBLE);


                    } else {
                        Log.i("AdminActivity","not checked");
                        fab.setVisibility(View.INVISIBLE);
                    }

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
