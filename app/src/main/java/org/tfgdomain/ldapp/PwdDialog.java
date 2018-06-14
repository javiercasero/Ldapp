package org.tfgdomain.ldapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.unboundid.ldap.sdk.ResultCode;

import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

public class PwdDialog extends DialogFragment{
    private EditText pwdEditText;
    private TextView domTextView;
    private TextView userTextView;
    private String user, domain, password;
    private int typeOfUser;
    private static final int sourceId = 3;


    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        /*builder.setTitle(R.string.pwd_dialog);
        builder.setMessage("Escribe la contraseña para el usuario ");*/

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        builder.setView(layoutInflater.inflate(R.layout.dialog_pwd, null))

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Intentar conectar con credenciales de lista y password introducido
                        //y pasar a activity_admin
                        pwdEditText = (EditText)getDialog().findViewById(R.id.pwdText);
                        password = pwdEditText.getText().toString();
                        user = getArguments().getString("user");
                        domain = getArguments().getString("domain");
                        typeOfUser = getArguments().getInt("typeofuser");

                        MyLdap myLdap = new MyLdap(builder.getContext(), sourceId);
                        //dismiss();
                        //myLdap.new Bind().execute(user,domain,password);

                        /*
                        MyLdap.Bind ldapBind = myLdap.new Bind();
                        ldapBind.execute(user,domain,password);
                        */
                        myLdap.new Bind().execute(user,domain,password,String.valueOf(typeOfUser));
                        dismiss();

                        /*
                        try {
                            //ResultCode resultCodePost = myLdap.new Bind().execute(user,domain,password).get();




                            if (resultCodePost.equals(ResultCode.SUCCESS)){
                                Log.i("ldap: ", "Conexion OK.");

                                switch (typeOfUser){
                                    case 1:
                                        Intent intent = new Intent(getDialog().getContext(), AdminActivity.class);
                                        intent.putExtra("user", user);
                                        intent.putExtra("domain", domain);
                                        intent.putExtra("password", password);
                                        startActivity(intent);
                                        break;
                                    case 0:
                                        intent = new Intent(getDialog().getContext(), UserActivity.class);
                                        startActivity(intent);
                                        break;

                                    default:

                                        break;
                                }


                            } else {
                                //msg = "Conexion Fallida: "+bindResult.getResultCode().toString();
                                Log.d("ldapNoOk", resultCodePost.toString());
                                Toast.makeText(getDialog().getContext(), "Conexion Fallida",Toast.LENGTH_SHORT).show();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }*/

                        //dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
