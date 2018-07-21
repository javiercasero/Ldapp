package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase NewDomainActivity.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.unboundid.ldap.sdk.ResultCode;

import java.util.concurrent.ExecutionException;

public class NewDomainActivity extends AppCompatActivity{
    private EditText editTextDomain, editTextUser, editTextPassword;
    private String domain, user, password;
    private ResultCode resultCode;
    private MyLdap myLdap;
    private static final int sourceId = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newdomain);

        editTextDomain = findViewById(R.id.text_newdomain);
        editTextUser = findViewById(R.id.text_newuser);
        editTextPassword = findViewById(R.id.text_newpassword);

        myLdap = new MyLdap(NewDomainActivity.this, sourceId);

        Button bTest = findViewById(R.id.test_button);
        bTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                domain = editTextDomain.getText().toString();
                user = editTextUser.getText().toString();
                password = editTextPassword.getText().toString();

                if (checkValues(user, domain, password)) {
                    Log.i("NewDomainActivity", "campos correctos");
                    try {
                        resultCode = myLdap.new Bind().execute(user,domain,password).get();
                        if (resultCode.equals(ResultCode.SUCCESS)) {
                            Log.i("Test: ", "Se puede salvar");
                            Log.i("Tipo de usuario: ", String.valueOf(myLdap.getTypeOfUser()));

                            Toast.makeText(NewDomainActivity.this, "Conexion OK",Toast.LENGTH_SHORT).show();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(NewDomainActivity.this, R.string.field_error,Toast.LENGTH_LONG).show();
                    Log.i("NewDomainActivity", "campos incorrectos");
                }

            }
        });

        Button bSave = findViewById(R.id.save_button);
        bSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                domain = editTextDomain.getText().toString();
                user = editTextUser.getText().toString();
                password = editTextPassword.getText().toString();

                if (checkValues(user, domain, password)) {
                    try {
                        resultCode = myLdap.new Bind().execute(user,domain,password).get();
                        if (resultCode.equals(ResultCode.SUCCESS)) {
                            Log.i("Test: ", "Se puede salvar");

                            Toast.makeText(NewDomainActivity.this, "Conexion OK",Toast.LENGTH_SHORT).show();
                            LdappDB myDB = new LdappDB(NewDomainActivity.this);
                            myDB.wDB(domain, user, myLdap.getTypeOfUser());
                            setResult(RESULT_OK, null);
                            finish();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(NewDomainActivity.this, R.string.field_error,Toast.LENGTH_LONG).show();
                    Log.i("NewDomainActivity", "campos incorrectos");
                }



            }
        });


    }
    private boolean checkValues(String user, String domain, String password) {

        if (user.equals(null) || domain.equals(null) || user.equals("") || domain.equals("")){
            return false;
        } else {
            if (password.equals(null)) {
                this.password = "";
            }
            return true;
        }
    }
}
