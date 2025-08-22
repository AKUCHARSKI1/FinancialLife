package com.example.apka_inzynierka;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLDataException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DatabaseManager(Context ctx){
        context = ctx;
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
       // context.deleteDatabase(DatabaseHelper.databaseName);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }
//-------------------------------------------user table--------------------------------------------
    public void insertUser(String name, String hashedPin, String photo){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.username, name);
        contentValues.put(DatabaseHelper.hashedPin, hashedPin);
        contentValues.put(DatabaseHelper.photo, photo);
        database.insert(DatabaseHelper.tableUsers,null, contentValues);
    }

    public Cursor fetchUser(){
        String [] columns = new String[] {DatabaseHelper.username,DatabaseHelper.hashedPin,DatabaseHelper.photo};
        Cursor cursor = database.query(DatabaseHelper.tableUsers, columns, null, null, null, null, null);
        if(cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int updateUser(String name, String hashedPin, String photo){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.username, name);
        contentValues.put(DatabaseHelper.hashedPin, hashedPin);
        contentValues.put(DatabaseHelper.photo, photo);
        int ret = database.update(DatabaseHelper.tableUsers, contentValues, DatabaseHelper.username + "=" + name,null);
        return ret;
    }

    public int selectUserUsername(String name){
        String countQuery = "SELECT COUNT(username) FROM users WHERE username = ?";
        Cursor cursor = database.rawQuery(countQuery, new String[]{name});
        int userCount = 0;
        if (cursor.moveToFirst()) {
            userCount = cursor.getInt(0);

        }

        cursor.close();
        return userCount;
    }

    public User selectUserByUsername(String name) {
        User user = null;

        String selectQuery = "SELECT * FROM users WHERE username = ?";
        Cursor cursor = database.rawQuery(selectQuery, new String[]{name});

        if (cursor.moveToFirst()) {

            int usernameIndex = cursor.getColumnIndex("username");
            int hashedPinIndex = cursor.getColumnIndex("hashedPin");
            int photoIndex = cursor.getColumnIndex("photo");

            if (usernameIndex != -1 && hashedPinIndex != -1 && photoIndex != -1) {
                String username = cursor.getString(usernameIndex);
                String hashedPin = cursor.getString(hashedPinIndex);
                String photo = cursor.getString(photoIndex);
                user = new User(username, hashedPin, photo);
            }
        }

        cursor.close();
        return user;
    }

    public void deleteUser(String name){
        database.delete(DatabaseHelper.tableUsers,DatabaseHelper.username + "=" + name,null);
    }

    //--------------------------------------target table-------------------------------------------


    public void insertTarget(String targetName, String username, Double targetAmount, Date targetEndDate){
        ContentValues contentValues = new ContentValues();
        String maxTargetIdQuery = "SELECT MAX(targetId) FROM targets";
        Cursor cursor = database.rawQuery(maxTargetIdQuery, null);
        int maxTargetId = 0;
        if (cursor.moveToFirst()) {
            maxTargetId = cursor.getInt(0) + 1;
        }
        cursor.close();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String targetUpdateDate = dateFormat.format(new Date());
        contentValues.put(DatabaseHelper.targetId, maxTargetId);
        contentValues.put(DatabaseHelper.targetName, targetName);
        contentValues.put(DatabaseHelper.targetAmount, targetAmount);
        contentValues.put(DatabaseHelper.targetState, "Aktywny");

        contentValues.put(DatabaseHelper.targetEndDate, dateFormat.format(targetEndDate));
        contentValues.put(DatabaseHelper.targetUpdateDate, targetUpdateDate);
        contentValues.put(DatabaseHelper.username, username);
        database.insert(DatabaseHelper.tableTargets,null, contentValues);
    }

    public List<Target> getTargetsList(String name, String state, String cardsCounter) throws ParseException {
        List<Target> targetsList = new ArrayList<>();

        String selectQuery = "SELECT targets.*, COALESCE(SUM(transactions.transactionAmount), 0) AS targetActualAmount " +
                "FROM targets " +
                "LEFT JOIN transactions ON targets.targetId = transactions.targetId " +
                "WHERE targets.username = ? AND targetState = ? " +
                "GROUP BY targets.targetId " +
                "ORDER BY targets.targetUpdateDate DESC " +
                "LIMIT 10 OFFSET ?";
        Cursor cursor = database.rawQuery(selectQuery, new String[]{name, state, cardsCounter});
        while (cursor.moveToNext()) {
            int targetIdIndex = cursor.getColumnIndex("targetId");
            int targetNameIndex = cursor.getColumnIndex("targetName");
            int usernameIndex = cursor.getColumnIndex("username");
            int targetAmountIndex = cursor.getColumnIndex("targetAmount");
            int targetStateIndex = cursor.getColumnIndex("targetState");
            int targetEndDateIndex = cursor.getColumnIndex("targetEndDate");
            int targetUpdateDateIndex = cursor.getColumnIndex("targetUpdateDate");
            int targetActualAmountIndex = cursor.getColumnIndex("targetActualAmount");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            int targetId = cursor.getInt(targetIdIndex);
            String targetName = cursor.getString(targetNameIndex);
            String username = cursor.getString(usernameIndex);
            Double targetAmount = cursor.getDouble(targetAmountIndex);
            String targetState = cursor.getString(targetStateIndex);
            Date targetEndDate = dateFormat.parse(cursor.getString(targetEndDateIndex));
            Date targetUpdateDate = dateFormat.parse(cursor.getString(targetUpdateDateIndex));
            Double targetActualAmount = cursor.getDouble(targetActualAmountIndex);
            targetsList.add(new Target(targetId, targetName, username, targetAmount, targetActualAmount, targetState, targetEndDate, targetUpdateDate));
        }

        cursor.close();


        return targetsList;
    }

    public void insertTransaction(String transactionName, Double transactionAmount, String username, String transactionCategory, String transactionDescription, int targetId, Date date){
        ContentValues contentValues = new ContentValues();
        String maxTransactionIdQuery = "SELECT MAX(transactionId) FROM transactions";
        Cursor cursor = database.rawQuery(maxTransactionIdQuery, null);
        int maxTransactionId = 0;
        if (cursor.moveToFirst()) {
            maxTransactionId = cursor.getInt(0) + 1;
        }
        cursor.close();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String transactionDate;
        if(date == null) {
            transactionDate = dateFormat.format(new Date());
        }else{
            transactionDate = dateFormat.format(date);
        }

        contentValues.put(DatabaseHelper.transactionId, maxTransactionId);
        contentValues.put(DatabaseHelper.transactionName, transactionName);
        contentValues.put(DatabaseHelper.transactionAmount, transactionAmount);
        contentValues.put(DatabaseHelper.username, username);
        contentValues.put(DatabaseHelper.transactionCategory, transactionCategory);
        contentValues.put(DatabaseHelper.transactionDescription, transactionDescription);
        contentValues.put(DatabaseHelper.transactionDate, transactionDate);
        contentValues.put(DatabaseHelper.targetId, targetId);

        database.insert(DatabaseHelper.tableTransactions,null, contentValues);
    }

    public void updateArchiveTarget(int targetId){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String targetUpdateDate = dateFormat.format(new Date());

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.targetState, "Nieaktywny");
        contentValues.put(DatabaseHelper.targetUpdateDate, String.valueOf(targetUpdateDate));
        database.update(DatabaseHelper.tableTargets, contentValues, DatabaseHelper.targetId + "=" + targetId,null);
    }

    public void updateUpdateDateTarget(int targetId){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String targetUpdateDate = dateFormat.format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.targetUpdateDate, String.valueOf(targetUpdateDate));
        database.update(DatabaseHelper.tableTargets, contentValues, DatabaseHelper.targetId + "=" + targetId,null);
    }

    public void deleteTargetAndTransactions(int targetId) {
        String selection = DatabaseHelper.targetId + " = ?";
        String[] selectionArgs = { String.valueOf(targetId) };
        database.delete(DatabaseHelper.tableTransactions, selection, selectionArgs);
        database.delete(DatabaseHelper.tableTargets, selection, selectionArgs);
    }


    public Double getBalance(String name) throws ParseException {
        String selectActiveQuery = "SELECT COALESCE(SUM(transactionAmount), 0) AS targetActualAmount " +
                "FROM transactions " +
                "WHERE username = ? AND transactionCategory = ?";
        String selectInctiveQuery = "SELECT COALESCE(SUM(transactionAmount), 0) AS targetActualAmount " +
                "FROM transactions " +
                "WHERE username = ? AND NOT transactionCategory = ?";
        Cursor cursor = database.rawQuery(selectActiveQuery, new String[]{name, "Wpłata"});
        double balance = 0.0;
        while (cursor.moveToNext()) {
            int targetActualAmountIndex = cursor.getColumnIndex("targetActualAmount");
            balance += cursor.getDouble(targetActualAmountIndex);
        }

        cursor.close();
        cursor = database.rawQuery(selectInctiveQuery, new String[]{name, "Wpłata"});
        while (cursor.moveToNext()) {
            int targetActualAmountIndex = cursor.getColumnIndex("targetActualAmount");
            balance -= cursor.getDouble(targetActualAmountIndex);
        }
        cursor.close();

        return balance;
    }

    public void deleteTransaction(int transactionId) {
        String selection = DatabaseHelper.transactionId + " = ?";
        String[] selectionArgs = { String.valueOf(transactionId) };
        database.delete(DatabaseHelper.tableTransactions, selection, selectionArgs);
    }

    public List<Transaction> getTransactionsList(String name, String cardsCounter,List<String> categoriesList, String transactionNamePattern, String topAmountBorder, String bottomAmountBorder, String topDateBorder, String bottomDateBorder) throws ParseException {
        List<Transaction> transactionsList = new ArrayList<>();
        List<String> paramsList = new ArrayList<>();
        paramsList.add(name);



        StringBuilder query = new StringBuilder();
        query.append("SELECT transactions.*, targets.targetState FROM transactions LEFT JOIN targets ON targets.targetId = transactions.targetId WHERE transactions.username = ? ");
        if(categoriesList.size() > 1){
            query.append("AND (");
            for(int i = 0; i < categoriesList.size(); i++){
                query.append("transactions.transactionCategory = ? OR ");
                paramsList.add(categoriesList.get(i));
            }

            query.delete(query.length()-4,query.length());
            query.append(") ");
        } else if(categoriesList.size() == 1){
            query.append("AND transactions.transactionCategory = ? ");
            paramsList.add(categoriesList.get(0));
        }
        if(transactionNamePattern.length() != 0){
            transactionNamePattern = "%" + transactionNamePattern + "%";
            query.append("AND transactions.transactionName LIKE ? ");
            paramsList.add(transactionNamePattern);
        }
        if(topAmountBorder.length() != 0){
            query.append("AND transactions.transactionAmount <= ? ");
            paramsList.add(topAmountBorder);
        }
        if(bottomAmountBorder.length() != 0){
            query.append("AND transactions.transactionAmount >= ? ");
            paramsList.add(bottomAmountBorder);
        }
        if(topDateBorder.length() != 0){

            SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy");
            Date date = inputFormat.parse(topDateBorder);

            date.setHours(23);
            date.setMinutes(59);
            date.setSeconds(59);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = outputFormat.format(date);


            query.append("AND transactions.transactionDate <= ? ");
            paramsList.add(formattedDate);
        }
        if(bottomDateBorder.length() != 0){
            SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy");
            Date date = inputFormat.parse(bottomDateBorder);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = outputFormat.format(date);

            query.append("AND transactions.transactionDate >= ? ");
            paramsList.add(formattedDate);
        }
        query.append("ORDER BY transactions.transactionDate DESC, transactions.transactionId ASC LIMIT 10 OFFSET ?");
        paramsList.add(cardsCounter);
        String[] params = paramsList.toArray(new String[0]);
        Cursor cursor = database.rawQuery(query.toString(), params);
        while (cursor.moveToNext()) {
            int transactionIdIndex = cursor.getColumnIndex("transactionId");
            int transactionNameIndex = cursor.getColumnIndex("transactionName");
            int transactionAmountIndex = cursor.getColumnIndex("transactionAmount");
            int usernameIndex = cursor.getColumnIndex("username");
            int transactionCategoryIndex = cursor.getColumnIndex("transactionCategory");
            int transactionDescriptionIndex = cursor.getColumnIndex("transactionDescription");
            int transactionDateIndex = cursor.getColumnIndex("transactionDate");
            int targetIdtIndex = cursor.getColumnIndex("targetId");
            int stateIndex = cursor.getColumnIndex("targetState");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            int transactionId = cursor.getInt(transactionIdIndex);
            String transactionName = cursor.getString(transactionNameIndex);
            Double transactionAmount = cursor.getDouble(transactionAmountIndex);
            String username = cursor.getString(usernameIndex);
            String transactionCategory = cursor.getString(transactionCategoryIndex);
            String transactionDescription = cursor.getString(transactionDescriptionIndex);
            Date transactionDate = dateFormat.parse(cursor.getString(transactionDateIndex));
            int targetId = cursor.getInt(targetIdtIndex);
            String state = cursor.getString(stateIndex);
            transactionsList.add(new Transaction(transactionId, transactionName, transactionAmount, username, transactionCategory, transactionDescription, transactionDate, targetId, -1, state));
        }

        cursor.close();


        return transactionsList;
    }

    public ArrayList<Double> getDataToMainChart(String name, String dateStart, String dateEnd, String mode, int howManyBars, String category, List<String> dates) {
        int dateLength = 0;
        if(mode.equals("Rok")){
            dateLength = 7;
        }else{
            dateLength = 10;
        }
        Cursor cursor;
        if(category.length() == 0){
            String selectOutcomeQuery = "SELECT SUBSTR(transactionDate, 1, ?) as date, SUM(transactionAmount) as sum " +
                    "FROM transactions " +
                    "WHERE username = ? AND DATE(transactionDate) BETWEEN ? AND ? AND NOT transactionCategory = ? " +
                    "GROUP BY SUBSTR(transactionDate, 1, ?)";
            cursor = database.rawQuery(selectOutcomeQuery, new String[]{String.valueOf(dateLength), name, dateStart, dateEnd, "Wpłata", String.valueOf(dateLength)});

        } else {
            String selectOutcomeQuery = "SELECT SUBSTR(transactionDate, 1, ?) as date, SUM(transactionAmount) as sum " +
                    "FROM transactions " +
                    "WHERE username = ? AND DATE(transactionDate) BETWEEN ? AND ? AND transactionCategory = ? " +
                    "GROUP BY SUBSTR(transactionDate, 1, ?)";
            cursor = database.rawQuery(selectOutcomeQuery, new String[]{String.valueOf(dateLength), name, dateStart, dateEnd, category, String.valueOf(dateLength)});

        }
        ArrayList<Double> values = new ArrayList<>();
        while (cursor.moveToNext()) {
            int dateIndex = cursor.getColumnIndex("date");
            int sumIndex = cursor.getColumnIndex("sum");
            for(int i = values.size(); i < dates.indexOf(cursor.getString(dateIndex)); i++){
                values.add(0.0);
            }
            values.add(cursor.getDouble(sumIndex));
        }

        if(values.size() < howManyBars) {
            for(int i = values.size(); i < howManyBars; i++)
                values.add(0.0);
        }
        cursor.close();
        return values;
    }

    public ArrayList<Double> getDataToMainChartBalance(String name, String dateStart, String dateEnd, String mode, int howManyBars, String category, List<String> dates) {
        ArrayList<Double> balance = new ArrayList<>();
        String selectIncomeQuery = "SELECT COALESCE(SUM(transactionAmount), 0) AS targetActualAmount " +
                "FROM transactions " +
                "WHERE username = ? AND transactionCategory = ? AND transactionDate < ?";
        String selectOutcomeQuery = "SELECT COALESCE(SUM(transactions.transactionAmount), 0) AS targetActualAmount " +
                "FROM transactions " +
                "WHERE username = ? AND NOT transactionCategory = ? AND transactionDate < ?";
        Cursor cursor = database.rawQuery(selectIncomeQuery, new String[]{name, category, dateStart});
        double tmpBalance = 0.0;
        while (cursor.moveToNext()) {
            int targetActualAmountIndex = cursor.getColumnIndex("targetActualAmount");
            tmpBalance += cursor.getDouble(targetActualAmountIndex);
        }

        cursor.close();
        cursor = database.rawQuery(selectOutcomeQuery, new String[]{name, category, dateStart});
        while (cursor.moveToNext()) {
            int targetActualAmountIndex = cursor.getColumnIndex("targetActualAmount");
            tmpBalance -= cursor.getDouble(targetActualAmountIndex);
        }
        cursor.close();
        ArrayList<Double> income = getDataToMainChart(name, dateStart, dateEnd, mode, howManyBars, category, dates);
        ArrayList<Double> outcome = getDataToMainChart(name, dateStart, dateEnd, mode, howManyBars, "", dates);
        //System.out.println("income");
        //System.out.println(income);
        //System.out.println("outcome");
        //System.out.println(outcome);
       // System.out.println(mode);
       // System.out.println(dates);
        SimpleDateFormat dataFormat;
        if(mode.equals("Rok")){
            dataFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        } else {
            dataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        //System.out.println(dataFormat.format(calendar.getTime()));
        balance.add(tmpBalance + income.get(0) - outcome.get(0));
        try {
            if(!dataFormat.format(dataFormat.parse(dates.get(0))).equals(dataFormat.format(calendar.getTime()))){

                for(int i = 1; i < howManyBars; i++){


                   // System.out.println(dataFormat.format(dataFormat.parse(dates.get(i))).equals(dataFormat.format(calendar.getTime())));
                   // System.out.println(dataFormat.format(dataFormat.parse(dates.get(i))));
                   // System.out.println(dataFormat.format(calendar.getTime()));
                    if(dataFormat.format(dataFormat.parse(dates.get(i))).equals(dataFormat.format(calendar.getTime()))){
                        break;
                    }

                    balance.add(balance.get(i-1) + income.get(i) - outcome.get(i));
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

       // System.out.println(balance);
        return balance;
    }


    public ArrayList<Double> getDataToCategoriesChart(String name, String dateStart, String dateEnd) {
        ArrayList<Double> categoriesAmount = new ArrayList<>();
        List<String> categories = Arrays.asList("Cel","Edukacja", "Elektronika", "Prezenty", "Rachunki", "Rozrywka", "Samochód", "Transport", "Trening", "Wypoczynek", "Zakupy", "Zdrowie");
        String selectOutcomeQuery = "SELECT transactionCategory, SUM(transactionAmount) as sum " +
                "FROM transactions " +
                "WHERE username = ? AND DATE(transactionDate) BETWEEN ? AND ? AND NOT transactionCategory = ? " +
                "GROUP BY transactionCategory " +
                "ORDER BY transactionCategory ASC";
        Cursor cursor = database.rawQuery(selectOutcomeQuery, new String[]{name, dateStart, dateEnd, "Wpłata"});

        while (cursor.moveToNext()) {
            int transactionCategoryIndex = cursor.getColumnIndex("transactionCategory");
            int sumIndex = cursor.getColumnIndex("sum");
            for(int i = categoriesAmount.size(); i < categories.indexOf(cursor.getString(transactionCategoryIndex)); i++){
                categoriesAmount.add(0.0);
            }
            categoriesAmount.add(cursor.getDouble(sumIndex));
        }
        if(categoriesAmount.size() < categories.size()) {
            for(int i = categoriesAmount.size(); i < categories.size(); i++)
                categoriesAmount.add(0.0);
        }
        cursor.close();
       // System.out.println(categories.size());
       // System.out.println(categoriesAmount.size());
       // System.out.println(categories);
       // System.out.println(categoriesAmount);
        return categoriesAmount;
    }
}
