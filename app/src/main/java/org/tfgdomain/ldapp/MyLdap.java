package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase MyLdap.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.RootDSE;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocketFactory;

public class MyLdap {
    private final Context mContext;
    private ProgressDialog pDialog;
    private static String user, domain, password;
    static LDAPConnection c;
    private String msg = null;
    private final int source;
    private static int typeOfUser;


    public MyLdap(final Context context, int source){
        this.mContext = context;
        this.source = source;

    }



    private void showProgress() {
        if(pDialog==null) {

            pDialog = new ProgressDialog(mContext);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pDialog.setMessage(mContext.getString(R.string.dialog_wait));
            pDialog.show();
        }
    }


    class Bind extends AsyncTask<String, Void, ResultCode> {
        private LDAPException ldex;
        private RootDSE rootDSE;
        private boolean isActiveDirectory;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected ResultCode doInBackground(String... params){
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());

            /*
            //Inicio de conexión mediante StartTLS
            StartTLSExtendedRequest startTLSRequest;
            ExtendedResult startTLSResult;

            c = new LDAPConnection();

            try {
                SSLContext sslContext = sslUtil.createSSLContext();
                try {

                    startTLSRequest = new StartTLSExtendedRequest(sslContext);

                    startTLSResult = c.processExtendedOperation(startTLSRequest);
                } catch (LDAPException le) {
                    startTLSResult = new ExtendedResult(le);
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            //Fin de conexión mediante StartTLS
            */
            // Conexión mediante LDAPS
            SSLSocketFactory socketFactory = null;

            try {
                socketFactory = sslUtil.createSSLSocketFactory("TLSv1.2");
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
            }
            c = new LDAPConnection(socketFactory);


            ResultCode resultCode;
            try {
                if (c.isConnected()) {
                    c.close();
                    Log.d("LDAPConnection", "CERRADA");
                }
                user = params[0];
                domain = params[1];
                Log.d("domain", domain);
                password = params[2];


                //Conexión para LDAPS
                c.connect(domain, 636);
                //Conexión para StartTLS
                //c.connect(domain, 389);

                String userDN = user+"@"+domain;

                BindRequest bindRequest = new SimpleBindRequest(userDN,password);

                BindResult bindResult = c.bind(bindRequest);

                //Determinar si el directorio es de tipo Active Directory
                rootDSE = c.getRootDSE();
                isActiveDirectory = rootDSE.hasAttribute("forestFunctionality");
                Log.i("MyLdap.Bind", String.valueOf(isActiveDirectory));

                Log.i("MyLdap.Bind", bindResult.getResultCode().toString());
                resultCode = bindResult.getResultCode();

            } catch (LDAPException le) {
                //Log.d("ldapexcp", le.getExceptionMessage());
                //Log.d("ldapexcp", le.getDiagnosticMessage());
                resultCode = ResultCode.INVALID_CREDENTIALS;
                Log.i("LDAPException: ",getError(le));
                isActiveDirectory = true;
                ldex = le;
                //Toast.makeText(mContext, getError(le),Toast.LENGTH_SHORT).show();

            } finally {
                //c.close();
            }
            if (isActiveDirectory) {
                if (resultCode == ResultCode.SUCCESS) {
                    try {
                        typeOfUser = Integer.valueOf(params[3]);
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        Log.i("test", "outofbound");
                        if (checkIfAdmin(user)) {
                            Log.i("isAdmin: ", "TRUE");
                            typeOfUser = 1;
                        } else {
                            typeOfUser = 0;
                        }
                    }
                }
            } else {
                resultCode = ResultCode.NO_SUCH_ATTRIBUTE;
            }
            /* Probar cómo sería con un dominio que no fuera Active Directory
            resultCode = ResultCode.NO_SUCH_ATTRIBUTE;
            isActiveDirectory = false;
            */

            return resultCode;
        }
        @Override
        protected void onPostExecute(ResultCode resultCodePost) {
            Intent intent;

            //Log.d("msgPost", msgPost);
            //Toast.makeText(mContext, msgPost,Toast.LENGTH_SHORT).show();

            if (pDialog.isShowing()) {pDialog.dismiss();}


            if (resultCodePost.equals(ResultCode.SUCCESS)){
                Log.i("ldap: ", "Conexion OK.");
                if (source == 3){
                    switch (typeOfUser) {
                        case 1:
                            intent = new Intent(mContext, AdminActivity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("domain", domain);
                            intent.putExtra("password", password);
                            mContext.startActivity(intent);
                            break;
                        case 0:
                            intent = new Intent(mContext, UserActivity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("password", password);
                            intent.putExtra("status", "unlocked");

                            mContext.startActivity(intent);
                            break;

                        default:

                            break;
                    }
                }


            } else if (resultCodePost.equals(ResultCode.NO_SUCH_ATTRIBUTE) && !isActiveDirectory) {
                Log.i("MyLdap.Bind", resultCodePost.toString());
                Log.i("MyLdap.Bind", String.valueOf(isActiveDirectory));
                Toast.makeText(mContext, R.string.ad_error,Toast.LENGTH_LONG).show();
            } else if (ldex.getResultCode().equals(ResultCode.CONNECT_ERROR)) {
                Toast.makeText(mContext, R.string.conn_error,Toast.LENGTH_LONG).show();
            } else if (ldex.getResultCode().equals(ResultCode.PARAM_ERROR)) {
                Toast.makeText(mContext, R.string.param_error,Toast.LENGTH_LONG).show();
                Log.i("MyLdap.Bind", ldex.getResultCode().toString());
            } else {
                Log.d("ldapNoOk", resultCodePost.toString());
                Toast.makeText(mContext, getError(ldex),Toast.LENGTH_LONG).show();
                if (typeOfUser == 0 && ldex.getDiagnosticMessage() != null) {
                    String status = null;
                    if (ldex.getDiagnosticMessage().contains("data 775")) {
                        status = "locked";
                    }
                    if (status != null) {
                        intent = new Intent(mContext, UserActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("password", password);
                        intent.putExtra("status", status);
                        //añadido para sacar la IP del dominio desde UserActivity
                        intent.putExtra("domain", domain);
                        mContext.startActivity(intent);
                    }
                }
            }
        }



    }

    class Search extends AsyncTask<Filter, Void, ArrayList<ListElement>> {
        private ArrayList<ListElement> listElementArrayList;
        private String msg, secondary;
        private SearchResultEntry entry;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }
        @Override
        protected ArrayList<ListElement> doInBackground(Filter... params){
            Filter fAdmin = params[0];

            Log.d("usuario: ", user);
            try {
                SearchResult searchResult = c.search(getDN(domain), SearchScope.SUB, fAdmin,"sAMAccountName", "cn", "distinguishedName", "userPrincipalName", "userAccountControl");
                Log.d("Num. resultados: ", String.valueOf(searchResult.getEntryCount()));
                msg = String.valueOf(searchResult.getEntryCount());
                if (searchResult.getEntryCount()>0) {
                    listElementArrayList = new ArrayList<>();
                    ListElement listElement;
                    for (int i = 0; i < searchResult.getEntryCount(); i++) {
                        entry = searchResult.getSearchEntries().get(i);
                        secondary = entry.getAttributeValue("userPrincipalName");
                        if (secondary == null) {
                            secondary = entry.getAttributeValue("sAMAccountName");
                        }
                        listElement = new ListElement(0,entry.getAttributeValue("cn"),secondary);
                        listElement.setUserDN(entry.getAttributeValue("distinguishedName"));
                        listElement.setAccountControl(entry.getAttributeValueAsInteger("userAccountControl"));
                        listElementArrayList.add(listElement);
                        //listElementArrayList.add(new ListElement(0,entry.getAttributeValue("cn"),secondary));
                    }
                } else {
                    Log.d("Search: ", "Sin resultados");
                }
            } catch (LDAPException le) {
                //Log.d("ldapexcp", le.getExceptionMessage());
                //Log.d("ldapexcp", le.getDiagnosticMessage());

                msg = getError(le);
            } finally {
                //Cerrar conexión LDAP
                //c.close();
            }
            return listElementArrayList;
        }
        @Override
        protected void onPostExecute(ArrayList<ListElement> arrayListPost) {
            if (pDialog.isShowing()) {pDialog.dismiss();}
            Toast.makeText(mContext, msg,Toast.LENGTH_SHORT).show();


        }
    }

    class UniqueEntry extends AsyncTask<Filter, Void, SearchResult> {
        private String msg;
        private Filter filter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();

        }
        @Override
        protected SearchResult doInBackground(Filter... params){
            filter = params[0];
            SearchResult searchResult = null;
            Log.d("usuario: ", user);
            try {
                switch (source){
                    case 2:
                        searchResult = c.search(getDN(domain), SearchScope.SUB, filter,"distinguishedName");
                        break;
                    case 4:

                        break;
                    case 5:
                        searchResult = c.search(getDN(domain), SearchScope.SUB, filter,"sAMAccountName", "cn", "pwdLastSet", "userPrincipalName", "userAccountControl", "distinguishedName");
                        Log.d("Num. resultados: ", String.valueOf(searchResult.getEntryCount()));
                        break;
                    case 6:
                        searchResult = c.search(getDN(domain), SearchScope.BASE, filter,"maxPwdAge");

                        break;
                    default:
                        break;
                }

            } catch (LDAPException le) {
                //Log.d("ldapexcp", le.getExceptionMessage());
                //Log.d("ldapexcp", le.getDiagnosticMessage());

                msg = getError(le);
            } finally {
                //Cerrar conexión LDAP
                //c.close();
            }
            return searchResult;
        }
        @Override
        protected void onPostExecute(SearchResult searchResultPost) {
            if (pDialog.isShowing()) {pDialog.dismiss();}
            Toast.makeText(mContext, msg,Toast.LENGTH_SHORT).show();


        }
    }

    class PasswordReset extends AsyncTask<String, Void, LDAPResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }
        @Override
        protected LDAPResult doInBackground(String... params){
            String dN = params[0];
            String newPwd = params[1];
            String oldPwd = params[2];
            String newQuotedPwd = "\"" + newPwd + "\"";
            byte[] newUnicodePwd = null;
            String oldQuotedPwd = "\"" + oldPwd + "\"";
            byte[] oldUnicodePwd = null;
            LDAPResult modifyResult = null;

            try {
                newUnicodePwd = newQuotedPwd.getBytes("UTF-16LE");
                oldUnicodePwd = oldQuotedPwd.getBytes("UTF-16LE");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            final ArrayList<Modification> modifications = new ArrayList<>();
            assert oldUnicodePwd != null;
            Modification pwdModOld = new Modification(ModificationType.DELETE,"unicodePwd", oldUnicodePwd);
            assert newUnicodePwd != null;
            Modification pwdModNew = new Modification(ModificationType.ADD,"unicodePwd", newUnicodePwd);
            modifications.add(pwdModOld);
            modifications.add(pwdModNew);

            ModifyRequest modifyRequest = new ModifyRequest(dN, modifications);

            try {
                modifyResult = c.modify(modifyRequest);
                msg = modifyResult.getResultString();
            } catch (LDAPException le) {
                le.printStackTrace();
                if (le.getResultCode().equals(ResultCode.CONSTRAINT_VIOLATION)){
                    msg = mContext.getString(R.string.pwd_constraint1)+"\n"+mContext.getString(R.string.pwd_constraint2);
                } else {
                    msg = mContext.getString(R.string.data_other);
                }
            }
            return modifyResult;
        }
        @Override
        protected void onPostExecute(LDAPResult modifyResultPost) {
            if (pDialog.isShowing()) {pDialog.dismiss();}
            Toast.makeText(mContext, msg,Toast.LENGTH_LONG).show();


        }
    }
    class ModUser extends AsyncTask<String, Void, LDAPResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected LDAPResult doInBackground(String... strings) {
            String dN = strings[0];
            LDAPResult modifyResult = null;
            Modification mod = null;

            if (strings[1].equals("0")){
                mod = new Modification(ModificationType.REPLACE,"lockoutTime","0");

            } else if (strings[1].equals("1")) {
                int aC = Integer.parseInt(strings[2]);
                int newAC = aC ^ 2;
                mod = new Modification(ModificationType.REPLACE,"userAccountControl",String.valueOf(newAC));

            }

            assert mod != null;
            ModifyRequest modifyRequest = new ModifyRequest(dN,mod);

            try {
                modifyResult = c.modify(modifyRequest);
            } catch (LDAPException le) {
                le.printStackTrace();

            }

            return modifyResult;
        }
        @Override
        protected void onPostExecute(LDAPResult modifyResultPost) {
            if (pDialog.isShowing()) {pDialog.dismiss();}

        }
    }


    private String getError(LDAPException le) {
        String mensaje;
        if (le.getDiagnosticMessage() == null) {

            mensaje = le.getResultCode().getName();
            Log.d("ldapexcp", le.getExceptionMessage());
        } else {
            String[] diagnosis = le.getDiagnosticMessage().split(",");

            switch (diagnosis[2].trim()){
                case "data 0":
                    mensaje = mContext.getString(R.string.data_0);
                    break;
                case "data 773":
                    mensaje = mContext.getString(R.string.data_773);
                    break;
                case "data 775":
                    mensaje = mContext.getString(R.string.data_775)+"\n"+le.getDiagnosticMessage();
                    break;
                case "data 533":
                    mensaje = mContext.getString(R.string.data_533)+"\n"+le.getResultCode().getName();
                    break;
                case "data 52e":
                    mensaje = mContext.getString(R.string.data_52e);
                    break;
                default:
                    mensaje = mContext.getString(R.string.data_other)+"\n"+le.getDiagnosticMessage();
                    break;
            }
        }

        return mensaje;
    }
    protected String getDN(String host){
        String dominioDN = "";
        String[] dominioDNArray = host.split("\\.");

        for (int i=0;i<dominioDNArray.length;i++){
            dominioDN=dominioDN.concat("DC=").concat(dominioDNArray[i]);
            if (i != dominioDNArray.length-1){
                dominioDN=dominioDN.concat(",");
            }
        }
        return dominioDN;
    }

    private boolean checkIfAdmin(String user) {
        boolean isAdmin = false;
        try {
            Filter fAdmin = Filter.create("(&(&(objectClass=group)(sAMAccountName=Domain Admins)))");

            SearchResult adminSearchResult = c.search(getDN(domain), SearchScope.SUB, fAdmin,"distinguishedName");

            SearchResultEntry adminResultEntry = adminSearchResult.getSearchEntries().get(0);
            String dn = adminResultEntry.getAttributeValue("distinguishedName");

            Filter fUser = Filter.create("(&(sAMAccountName="+user+")(memberof:1.2.840.113556.1.4.1941:="+dn+"))");

            SearchResult userSearchResult = c.search(getDN(domain), SearchScope.SUB, fUser,"distinguishedName");
            if (userSearchResult.getEntryCount()==1){
                isAdmin = true;
                Log.i("isAdmin: ", "TRUE");
            }

        } catch (LDAPException e) {
            e.printStackTrace();
        }
        return isAdmin;
    }

    int getTypeOfUser(){
        return typeOfUser;
    }

}
