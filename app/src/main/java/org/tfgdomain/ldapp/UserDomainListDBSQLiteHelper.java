package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase UserDomainListDBSQLiteHelper.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class UserDomainListDBSQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "userdomain";

    public UserDomainListDBSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqldb) {
        sqldb.execSQL(UserDomainListDB.UserDomain.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqldb, int i, int j) {
        sqldb.execSQL("DROP TABLE IF EXISTS " + UserDomainListDB.UserDomain.TABLE_NAME);
        onCreate(sqldb);
    }
}
