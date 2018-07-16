package org.tfgdomain.ldapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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

import org.tfgdomain.jade.AndroidAgent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import jade.android.AgentContainerHandler;
import jade.android.AndroidGatewayAgent;
import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class UserActivity extends AppCompatActivity{
    private static final int sourceId = 5;

    private String account, name, accountStatus, date, dN, oldPassword, domain, host, user;
    private long maxPwdAge, pwdLastSet;
    private int accountStatusFlag;

    private TextView textAccount, textCN, dateOfExpiration, textAccountStatus;
    private Button bReset;

    //private String host = "192.168.0.33";

    private String port = "1099";
    //private String nickname;
    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;
    private MyReceiver myReceiver;
    private Boolean containerStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //
        myReceiver = new MyReceiver();
        IntentFilter lockedStatusFilter = new IntentFilter();
        lockedStatusFilter.addAction("unlocked");
        registerReceiver(myReceiver, lockedStatusFilter);
        containerStarted = false;
        //

        textAccount = findViewById(R.id.textView_account);
        textCN = findViewById(R.id.textView_namelastname);
        dateOfExpiration = findViewById(R.id.textView_expires);
        textAccountStatus = findViewById(R.id.textView_status);
        bReset = (Button)findViewById(R.id.reset_button);
        Button bUnlock = (Button)findViewById(R.id.unlock_button);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        user = intent.getStringExtra("user");
        oldPassword = intent.getStringExtra("password");
        accountStatus = intent.getStringExtra("status");

        //domain = intent.getStringExtra("domain");


        if (accountStatus.equals("unlocked")) {

            containerStarted = false;

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
                    dateOfExpiration.setText(date);
                }
            }
            bReset.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    showNewPwdDialog();
                }
            });

        } else if (accountStatus.equals("locked")){
            textAccountStatus.setText(getResources().getString(R.string.data_775));
            textAccount.setText(user);
            domain = intent.getStringExtra("domain");
            Log.i("UserActivity", domain);

            textCN.setText("");
            dateOfExpiration.setText("");
            bUnlock.setVisibility(View.VISIBLE);

            bUnlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    InetAddress address;

                    try {
                        //domain = "tfgdomain.org";
                        //host = "192.168.0.33";
                        host = new IpAddress().execute(domain).get();
                        //bind("aa", host, port, agentStartupCallback);
                        bind("aa", host, port, agentStartupCallback);
                        Log.i("Exito: ", "creating agent");

                    } catch (Exception ex) {
                        Log.i("Error: ", "creating agent");
                    }
                }
            });

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
                        oldPassword = newPassword;
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

    private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
        @Override
        public void onSuccess(AgentController agent) {
        }

        @Override
        public void onFailure(Throwable throwable) {
            //logger.log(Level.INFO, "Nickname already in use!");
            //myHandler.postError(getString(R.string.msg_nickname_in_use));
        }
    };

    public void bind(final String nickname, final String host,
                     final String port,
                     final RuntimeCallback<AgentController> agentStartupCallback) {

        final Properties profile = new Properties();
        profile.setProperty(Profile.MAIN_HOST, host);
        profile.setProperty(Profile.MAIN_PORT, port);
        profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
        profile.setProperty(Profile.JVM, Profile.ANDROID);
        profile.setProperty("domain", domain);
        profile.setProperty("user", user);

        if (AndroidHelper.isEmulator()) {
            // Emulator: this is needed to work with emulated devices
            profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
        } else {
            profile.setProperty(Profile.LOCAL_HOST,
                    AndroidHelper.getLocalIPAddress());
        }
        // Emulator: this is not really needed on a real device
        profile.setProperty(Profile.LOCAL_PORT, "2000");

        if (microRuntimeServiceBinder == null) {
            Log.i("bind: ", "null");
            serviceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
                    //logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
                    Log.i("bind: ", "onserviceconnected");
                    startContainer(nickname, profile, agentStartupCallback);
                }

                public void onServiceDisconnected(ComponentName className) {
                    microRuntimeServiceBinder = null;
                    //logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
                }
            };
            //logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
            bindService(new Intent(getApplicationContext(),
                            MicroRuntimeService.class), serviceConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            //logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
            startContainer(nickname, profile, agentStartupCallback);
        }
    }

    private void startContainer(final String nickname, Properties profile,
                                final RuntimeCallback<AgentController> agentStartupCallback) {
        if (!MicroRuntime.isRunning()) {
            Log.i("container: ", "not running");
            microRuntimeServiceBinder.startAgentContainer(profile,
                    new RuntimeCallback<Void>() {
                        @Override
                        public void onSuccess(Void thisIsNull) {
                            //logger.log(Level.INFO, "Successfully start of the container...");
                            containerStarted = true;
                            startAgent(nickname, agentStartupCallback);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            //logger.log(Level.SEVERE, "Failed to start the container...");
                        }
                    });
        } else {
            startAgent(nickname, agentStartupCallback);
        }
    }

    private void startAgent(final String nickname,
                            final RuntimeCallback<AgentController> agentStartupCallback) {
        microRuntimeServiceBinder.startAgent(nickname,
                AndroidAgent.class.getName(),
                new Object[] { getApplicationContext() },
                new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void thisIsNull) {
                        //logger.log(Level.INFO, "Successfully start of the "
                        //        + AndroidAgent.class.getName() + "...");
                        try {
                            agentStartupCallback.onSuccess(MicroRuntime
                                    .getAgent(nickname));
                        } catch (ControllerException e) {
                            // Should never happen
                            agentStartupCallback.onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        //logger.log(Level.SEVERE, "Failed to start the "
                        //        + SenderAgent.class.getName() + "...");
                        agentStartupCallback.onFailure(throwable);
                    }
                });
    }

    public void unBind(final RuntimeCallback<AgentController>
                                   agentStartupCallback)
    {


        microRuntimeServiceBinder.stopAgentContainer(new RuntimeCallback<Void>()
        {
            @Override
            public void onSuccess(Void thisIsNull)
            {
                agentStartupCallback.onSuccess(null);
            }

            @Override
            public void onFailure(Throwable throwable)
            {

                agentStartupCallback.onFailure(throwable);
            }
        });

        unbindService(serviceConnection);

    }

    protected class IpAddress extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String hostname = strings[0];
            String hostaddress = null;
            InetAddress address;
            try {
                address = InetAddress.getByName(hostname);
                hostaddress = address.getHostAddress();
            } catch (UnknownHostException e1) {

                e1.printStackTrace();
            }
            return hostaddress;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (containerStarted) { unBind(agentStartupCallback);}
        unregisterReceiver(myReceiver);

    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("UserActivity", action);
            /*
            if (action.equalsIgnoreCase("jade.demo.chat.KILL")) {
                finish();
            }
            */
            if (action.equalsIgnoreCase("unlocked")) {
                Log.i("UserActivity", "UNLOCKED");
                Toast.makeText(UserActivity.this, user+": "+getResources().getString(R.string.unlocked_ok),Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
