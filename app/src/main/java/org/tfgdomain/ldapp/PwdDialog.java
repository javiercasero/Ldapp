package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase PwdDialog.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

public class PwdDialog extends DialogFragment{
    private EditText pwdEditText;
    private String user, domain, password;
    private int typeOfUser;
    private static final int sourceId = 3;


    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        builder.setView(layoutInflater.inflate(R.layout.dialog_pwd, null))

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Intentar conectar con credenciales de lista y password introducido
                        //y pasar a activity_admin
                        pwdEditText = getDialog().findViewById(R.id.pwdText);

                        password = pwdEditText.getText().toString();


                        assert getArguments() != null;
                        user = getArguments().getString("user");
                        domain = getArguments().getString("domain");
                        typeOfUser = getArguments().getInt("typeofuser");

                        MyLdap myLdap = new MyLdap(builder.getContext(), sourceId);

                        myLdap.new Bind().execute(user,domain,password,String.valueOf(typeOfUser));
                        dismiss();
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
