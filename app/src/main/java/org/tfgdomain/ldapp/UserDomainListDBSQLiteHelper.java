package org.tfgdomain.ldapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDomainListDBSQLiteHelper extends SQLiteOpenHelper {

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
