package org.tfgdomain.ldapp;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase UserDomainListDB.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.provider.BaseColumns;

final class UserDomainListDB {
    private UserDomainListDB(){}

    public static class UserDomain implements BaseColumns {
        public static final String TABLE_NAME = "userdomain";
        public static final String COLUMN_DOMAIN = "domain";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_TYPEOFUSER = "typeofuser";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DOMAIN + " TEXT, " +
                COLUMN_USER + " TEXT, " +
                COLUMN_TYPEOFUSER + " INTEGER" + ")";
    }
}
