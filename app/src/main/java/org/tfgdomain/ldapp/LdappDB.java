package org.tfgdomain.ldapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LdappDB {
    private Context context;
    private ArrayList<ListElement> arrayList;
    protected LdappDB(Context context){
        this.context = context;
    }

    public void wDB(String domain, String user, int typeofuser){
        SQLiteDatabase sqldb = new UserDomainListDBSQLiteHelper(context).getWritableDatabase();
        ContentValues entradas = new ContentValues();
        entradas.put(UserDomainListDB.UserDomain.COLUMN_DOMAIN, domain);
        entradas.put(UserDomainListDB.UserDomain.COLUMN_USER, user);
        entradas.put(UserDomainListDB.UserDomain.COLUMN_TYPEOFUSER, typeofuser);

        long row = sqldb.insert(UserDomainListDB.UserDomain.TABLE_NAME, null, entradas);

        Toast.makeText(context, "El nuevo ID es: " + row, Toast.LENGTH_LONG).show();
        Log.i("LdappDB", "El nuevo ID es: " + row);
    }

    public ArrayList<ListElement> rDB(){

        SQLiteDatabase sqldb = new UserDomainListDBSQLiteHelper(context).getReadableDatabase();
        arrayList = new ArrayList<ListElement>();
        Cursor cursor = sqldb .rawQuery("SELECT * FROM userdomain", null);
        //if (cursor.getCount()>0){
        if (cursor != null){
            if (cursor.moveToFirst()) {
                do {
                    //arrayList.add(new ListUserDomain(R.drawable.ic_supervisor_account_black_24px, cursor.getString(cursor.getColumnIndexOrThrow(UserDomainListDB.UserDomain.COLUMN_DOMAIN)),cursor.getString(cursor.getColumnIndexOrThrow(UserDomainListDB.UserDomain.COLUMN_USER))));
                    arrayList.add(new ListElement(cursor.getInt(cursor.getColumnIndexOrThrow(UserDomainListDB.UserDomain.COLUMN_TYPEOFUSER)), cursor.getString(cursor.getColumnIndexOrThrow(UserDomainListDB.UserDomain.COLUMN_DOMAIN)), cursor.getString(cursor.getColumnIndexOrThrow(UserDomainListDB.UserDomain.COLUMN_USER))));
                } while (cursor.moveToNext());
            }
            //String dominio = cursor.getString(1);
            //Toast.makeText(context, cursor.getCount() + dominio, Toast.LENGTH_LONG).show();
        }
        cursor.close();
        return arrayList;
    }
}
