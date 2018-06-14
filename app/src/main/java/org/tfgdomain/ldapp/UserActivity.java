package org.tfgdomain.ldapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class UserActivity extends AppCompatActivity{
    private static final int sourceId = 5;

    private String account, name, accountStatus, date, dN, oldPassword;
    private long maxPwdAge, pwdLastSet;
    private int accountStatusFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        TextView textAccount = findViewById(R.id.textView_account);
        TextView textCN = findViewById(R.id.textView_namelastname);
        TextView dateOfExpiraton = findViewById(R.id.textView_expires);
        TextView textAccountStatus = findViewById(R.id.textView_status);
        Button bUnlock = (Button)findViewById(R.id.unlock_button);
        Button bReset = (Button)findViewById(R.id.reset_button);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String user = intent.getStringExtra("user");
        oldPassword = intent.getStringExtra("password");
        accountStatus = intent.getStringExtra("status");

        if (accountStatus.equals("unlocked")) {
            bReset.setVisibility(View.VISIBLE);

            Filter fUser = Filter.createEqualityFilter("sAMAccountName",user);
            //Esta siguiente línea es la que vale
            //runFilter(fUser);

            //De aquí al final del método es de prueba junto con Search2 de MyLdap
            Filter fDomain = Filter.createEqualityFilter("objectClass","domain");

            SearchResult userResult = runSearch(fUser, sourceId);
            SearchResultEntry entry;

            if (userResult.getEntryCount()>0) {
                entry = userResult.getSearchEntries().get(0);
                account = entry.getAttributeValue("sAMAccountName");
                dN = entry.getAttributeValue("distinguishedName");
                name = entry.getAttributeValue("cn");
                pwdLastSet = Long.parseLong(entry.getAttributeValue("pwdLastSet"));
                accountStatusFlag = Integer.parseInt(entry.getAttributeValue("userAccountControl"));
                accountStatus = getAccountStatus(accountStatusFlag);
                userResult = runSearch(fDomain, 6);
                if (userResult.getEntryCount()>0) {
                    entry = userResult.getSearchEntries().get(0);
                    maxPwdAge = Long.parseLong(entry.getAttributeValue("maxPwdAge"));

                    date = getExpirationDate(maxPwdAge, pwdLastSet);

                    textAccount.setText(account);
                    textCN.setText(name);
                    textAccountStatus.setText(accountStatus);
                    dateOfExpiraton.setText(date);
                }
            }
            bReset.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    showNewPwdDialog();
                }
            });
        } else {
            textAccount.setText(user);
            textAccountStatus.setText(getResources().getString(R.string.data_775));
            textCN.setText("");
            dateOfExpiraton.setText("");
            bUnlock.setVisibility(View.VISIBLE);
        }



    }

    private String getAccountStatus(int flag) {
        int UF_NORMAL_ACCOUNT = 0x0200;
        int UF_ACCOUNTDISABLE = 0x0002;
        int UF_PASSWD_NOTREQD = 0x0020;
        int UF_PASSWD_EXPIRED = 0x800000;
        int UF_DONT_EXPIRE_PASSWD = 0X10000;
        int[] accountFlags = {UF_ACCOUNTDISABLE, UF_PASSWD_NOTREQD, UF_DONT_EXPIRE_PASSWD, UF_PASSWD_EXPIRED};
        String[] accountFlagsText = getResources().getStringArray(R.array.account_flags);
        boolean found = false;
        String status = getResources().getString(R.string.normal_account);;

        if (UF_NORMAL_ACCOUNT == flag){
            found = true;
        } else {
            for (int i=0;i<accountFlags.length;i++){
                if ((UF_NORMAL_ACCOUNT + accountFlags[i])==flag){
                    found = true;
                    if (i!=0){
                        status = status+" | "+accountFlagsText[i];
                    } else {
                        status = accountFlagsText[i];
                    }
                    break;
                } else {
                    for(int j=i+1;j<accountFlags.length;j++){
                        if ((UF_NORMAL_ACCOUNT + accountFlags[i] + accountFlags[j])==flag){
                            found = true;
                            if (i!=0){
                                status = status+" | "+accountFlagsText[i]+" | "+accountFlagsText[j];
                            } else {
                                status = accountFlagsText[i]+" | "+accountFlagsText[j];
                            }
                            break;
                        } else {
                            for (int k=j+1;k<accountFlags.length;k++){
                                if ((UF_NORMAL_ACCOUNT + accountFlags[i] + accountFlags[j] + accountFlags[k])==flag){
                                    found = true;
                                    if (i!=0){
                                        status = status+" | "+accountFlagsText[i]+" | "+accountFlagsText[j]+" | "+accountFlagsText[k];
                                    } else {
                                        status = accountFlagsText[i]+" | "+accountFlagsText[j]+" | "+accountFlagsText[k];
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (found) {
            return status;
        } else {
            return getResources().getString(R.string.status_not_found);
        }
    }

    private String getExpirationDate(long maxPwdAge, long pwdLastSet){
        String expirationDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - H:mm");
        calendar.setTime(new Date("1/1/1601"));
        long base_1601_time = calendar.getTimeInMillis();

        calendar.setTime(new Date("1/1/1970"));
        long base_1970_time = calendar.getTimeInMillis();


        long ms_offset = base_1970_time - base_1601_time;
        long pwdExpires = pwdLastSet+Math.abs(maxPwdAge);
        calendar.setTimeInMillis(pwdExpires / 10000 - ms_offset);
        expirationDate = sdf.format(calendar.getTime());
        return expirationDate;
    }

    protected void doPositiveClick(String newPassword){
        if(MyLdap.c.isConnected()){
            Log.d("Ldapconnected", "YES");

            MyLdap myLdap = new MyLdap(UserActivity.this, sourceId);
            try {
                LDAPResult ldapResult = myLdap.new PasswordReset().execute(dN, newPassword, oldPassword).get();
                if (ldapResult!=null){
                    if(ldapResult.getResultCode().equals(ResultCode.SUCCESS)) {
                        Toast.makeText(UserActivity.this, R.string.pwd_changed,Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(UserActivity.this, R.string.data_other,Toast.LENGTH_LONG).show();

                    }
                    //Toast.makeText(UserActivity.this, ldapResult.getResultString(),Toast.LENGTH_LONG).show();
                    Log.i("Password reset result: ", ldapResult.getResultString());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        //finish();
        //Con el finish() anterior al borrar vuelve al activity anterior
        //Con la siguiente línea cargaría el siguiente lugar al borrar
        //startActivity(getIntent());
    }

    public void showNewPwdDialog(){
        DialogFragment dialogFragment = new NewPwdDialog();
        //dialogFragment.setArguments(user_domain);
        dialogFragment.show(getSupportFragmentManager(),"Reset Password Dialog");
    }

    //metodo de prueba
    private SearchResult runSearch(Filter filter, int id) {
        SearchResult searchResult = null;
        if(MyLdap.c.isConnected()){
            Log.d("Ldapconnected", "YES");
            MyLdap myLdap = new MyLdap(UserActivity.this, id);
            try {
                searchResult = myLdap.new UniqueEntry().execute(filter).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        } else {
            Log.d("ldap: ", "not connected");
        }
        return searchResult;
    }

}
