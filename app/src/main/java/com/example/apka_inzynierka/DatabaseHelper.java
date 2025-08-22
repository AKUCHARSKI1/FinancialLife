package com.example.apka_inzynierka;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {

    static final String databaseName = "financialLife.db";
    static final int databaseVersion = 1;
//----------------------------------------- USER TABLE----------------------------------------------
    static final String tableUsers = "users";
    static final String username = "username";
    static final String hashedPin = "hashedPin";
    static final String photo = "photo";
    static final String createUsersTable = "CREATE TABLE " + tableUsers + " (" + username + " TEXT PRIMARY KEY, " + hashedPin + " TEXT, " + photo + " TEXT);";



    //----------------------------------------- TARGET TABLE----------------------------------------------
    static final String tableTargets = "targets";
    static final String targetId = "targetId";
    static final String targetName = "targetName";
    static final String targetAmount = "targetAmount";
    static final String targetState = "targetState";
    static final String targetEndDate = "targetEndDate";
    static final String targetUpdateDate = "targetUpdateDate";
    static final String createTargetsTable = "CREATE TABLE " + tableTargets + " (" +
            targetId + " INTEGER PRIMARY KEY, " +
            targetName + " TEXT, " +
            targetAmount + " REAL, " +
            targetState + " TEXT, " +
            targetEndDate + " DATETIME, " +
            targetUpdateDate + " DATETIME, " +
            username + " TEXT, " +
            "FOREIGN KEY(" + username + ") REFERENCES " + tableUsers + "(" + username + ")" +
            ")";


    //-------------------------------------- TRANSACTION TABLE --------------------------------------
    static final String tableTransactions = "transactions";
    static final String transactionId = "transactionId";
    static final String transactionName = "transactionName";
    static final String transactionAmount = "transactionAmount";
    static final String transactionCategory = "transactionCategory";
    static final String transactionDescription = "transactionDescription";
    static final String transactionDate = "transactionDate";
    static final String obligationId = "obligationId";
    static final String createTransactionsTable = "CREATE TABLE " + tableTransactions + " (" +
            transactionId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            transactionName + " TEXT, " +
            transactionAmount + " REAL, " +
            username + " TEXT, " +
            transactionCategory + " TEXT, " +
            transactionDescription + " TEXT, " +
            transactionDate + " DATETIME, " +
            targetId + " INTEGER, " +
            obligationId + " INTEGER, " +
            "FOREIGN KEY(" + targetId + ") REFERENCES " + tableTargets + "(" + targetId + ")," +
            "FOREIGN KEY(" + username + ") REFERENCES " + tableUsers + "(" + username + ")" +
            ")";

    //--------------------------------------- END---------------------------------------------------
    public DatabaseHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createUsersTable);
        sqLiteDatabase.execSQL(createTargetsTable);
        sqLiteDatabase.execSQL(createTransactionsTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableUsers);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableTargets);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableTransactions);
        onCreate(sqLiteDatabase);
    }
}
