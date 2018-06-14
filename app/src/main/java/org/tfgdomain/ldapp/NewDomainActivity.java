package org.tfgdomain.ldapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.ResultCode;

import java.util.concurrent.ExecutionException;

public class NewDomainActivity extends AppCompatActivity{
    private Button bTest;
    private Button bSave;
    private EditText editTextDomain, editTextUser, editTextPassword;
    //private Boolean testOk = false;
    private String domain, user, password;
    private ResultCode resultCode;
    private MyLdap myLdap;
    private static final int sourceId = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newdomain);


        editTextDomain = (EditText)findViewById(R.id.text_newdomain);
        editTextUser = (EditText)findViewById(R.id.text_newuser);
        editTextPassword = (EditText)findViewById(R.id.text_newpassword);

        myLdap = new MyLdap(NewDomainActivity.this, sourceId);

        bTest =(Button) findViewById(R.id.test_button);
        bTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                domain = editTextDomain.getText().toString();
                user = editTextUser.getText().toString();
                password = editTextPassword.getText().toString();
                //MyLdap.Bind ldapBind = myLdap.new Bind();
                try {
                    resultCode = myLdap.new Bind().execute(user,domain,password).get();
                    if (resultCode.equals(ResultCode.SUCCESS)) {
                        Log.i("Test: ", "Se puede salvar");
                        Log.i("Tipo de usuario: ", String.valueOf(myLdap.getTypeOfUser()));

                        Toast.makeText(NewDomainActivity.this, "Conexion OK",Toast.LENGTH_SHORT).show();
                        //LdappDB myDB = new LdappDB(NewDomainActivity.this);
                        //myDB.wDB(domain, user, 0);
                    } else {
                        //Toast.makeText(NewDomainActivity.this, "Conexion Fallida",Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        bSave =(Button) findViewById(R.id.save_button);
        bSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                domain = editTextDomain.getText().toString();
                user = editTextUser.getText().toString();
                password = editTextPassword.getText().toString();
                //MyLdap.Bind ldapBind = myLdap.new Bind();
                try {
                    resultCode = myLdap.new Bind().execute(user,domain,password).get();
                    if (resultCode.equals(ResultCode.SUCCESS)) {
                        Log.i("Test: ", "Se puede salvar");

                        Toast.makeText(NewDomainActivity.this, "Conexion OK",Toast.LENGTH_SHORT).show();
                        LdappDB myDB = new LdappDB(NewDomainActivity.this);
                        //myDB.wDB(domain, user, 0);
                        myDB.wDB(domain, user, myLdap.getTypeOfUser());
                        setResult(RESULT_OK, null);
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


            }
        });


    }
}
