package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase NewPwdDialog.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewPwdDialog  extends DialogFragment {
    private EditText pwdEditText, pwdEditText2;
    private static final int sourceId = 6;
    private String pwd1, pwd2;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.dialog_new_pwd, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

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
    @Override
    public void onResume()
    {
        super.onResume();
        final AlertDialog alertDialog = (AlertDialog)getDialog();
        if(alertDialog != null)
        {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean goBack = false;
                    pwdEditText = alertDialog.findViewById(R.id.newPwdText);
                    pwdEditText2 = alertDialog.findViewById(R.id.newPwdText2);

                    pwd1 = pwdEditText.getText().toString();
                    pwd2 = pwdEditText2.getText().toString();
                    if (pwd1!=null && (pwd1.equals(pwd2))) {
                        ((UserActivity)getActivity()).doPositiveClick(pwd1);
                        goBack = true;
                    } else {
                        Log.d("pwd1pwd2:", "no match");
                        Toast.makeText(alertDialog.getContext(), R.string.pwd_not_match,Toast.LENGTH_LONG).show();

                    }

                    if(goBack)
                        alertDialog.dismiss();
                }
            });
        }
    }


}
