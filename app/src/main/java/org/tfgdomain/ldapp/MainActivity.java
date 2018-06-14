package org.tfgdomain.ldapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView user;
    private TextView domain;
    private Bundle user_domain;
    private static final int sourceId = 1;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        getSupportActionBar().setIcon(R.drawable.ic_domain_black_24px);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_person_add_black_24px);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewDomainActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, sourceId);
            }
        });



        //myDB.rDB();

        //Pruebo a cargar una lista para dar contenido al ListView
        //ArrayList<ListUserDomain> arrayList = new ArrayList<ListUserDomain>();
        //arrayList.add(new ListUserDomain(R.drawable.ic_supervisor_account_black_24px, "tfgdomain.org","jcasero"));
        //arrayList.add(new ListUserDomain(R.drawable.ic_person_black_24px, "tfgdomain.org", "usuario1"));

        //Crear base de datos
        LdappDB myDB = new LdappDB(this);

        ListView listView = (ListView)findViewById(R.id.listUserDomain);

        listView.setAdapter(new MainContent(this, R.layout.user_domain, myDB.rDB()) {
            @Override
            public void onUserDomainList(Object userDomain, View view) {
                ImageView icon = (ImageView)view.findViewById(R.id.imageView_icon);
                icon.setImageResource(((ListElement) userDomain).getIdIcon());

                TextView texto_dominio = (TextView)view.findViewById(R.id.textView_dominio);
                texto_dominio.setText(((ListElement) userDomain).getTextPrincipal());

                TextView texto_usuario = (TextView)view.findViewById(R.id.textView_usuario);
                texto_usuario.setText(((ListElement) userDomain).getTextSecondary());
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                long viewId = view.getId();
                if (viewId == R.id.imageView_icon) {
                    Toast.makeText(getBaseContext(), "Icono pulsado", Toast.LENGTH_LONG).show();
                    Log.i("MainActivity","Icono pulsado: "+id);
                } else {
                    user = (TextView)view.findViewById(R.id.textView_usuario);
                    domain = (TextView)view.findViewById(R.id.textView_dominio);
                    //ImageView imageView = (ImageView)view.findViewById(R.id.imageView_icon);
                    int typeOfUser = ((ListElement)parent.getItemAtPosition(position)).getTypeOfUser();


                    user_domain = new Bundle();
                    user_domain.putString("user", user.getText().toString());
                    user_domain.putString("domain", domain.getText().toString());
                    user_domain.putInt("typeofuser", typeOfUser);

                    //Toast.makeText(getBaseContext(), "Lista pulsada " +id+" "+user.getText(), Toast.LENGTH_SHORT).show();
                    Log.i("MainActivity","Lista pulsada: "+id+" "+user.getText().toString()+", "+domain.getText().toString()+", "+typeOfUser);
                    showPwdDialog();
                }




            }
        });

        //myDB.wDB("tfgdomain.org", "jcasero", 1);
        //myDB.wDB("tfgdomain.org", "usuario1", 0);
        //myDB.rDB();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode == sourceId) && (resultCode == RESULT_OK)){
            Log.i("Resultado: ", "OK");
            this.recreate();
        }
        if((requestCode == sourceId) && (resultCode == RESULT_CANCELED)){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showPwdDialog(){
        DialogFragment dialogFragment = new PwdDialog();
        dialogFragment.setArguments(user_domain);
        dialogFragment.show(getSupportFragmentManager(),"Password Dialog");
    }


}
