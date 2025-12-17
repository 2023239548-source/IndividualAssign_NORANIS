package com.example.individualassign_noranis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BillDB";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "electricity_bills";

    // Column Definitions (These MUST remain consistent across all files)
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MONTH = "month_name";
    public static final String COLUMN_UNIT = "unit_used";
    public static final String COLUMN_REBATE = "rebate_percent";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_FINAL_COST = "final_cost";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MONTH + " TEXT UNIQUE, " + // UNIQUE prevents repeated months
                COLUMN_UNIT + " REAL, " +
                COLUMN_REBATE + " REAL, " +
                COLUMN_TOTAL_CHARGES + " REAL, " +
                COLUMN_FINAL_COST + " REAL" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Inserts or Updates a bill based on the month (UPSERT logic).
     */
    public long saveMonthlyBill(String month, double unit, double rebate, double total, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNIT, unit);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, total);
        values.put(COLUMN_FINAL_COST, finalCost);

        // Attempt to UPDATE the existing row for that month
        int updatedRows = db.update(TABLE_NAME,
                values,
                COLUMN_MONTH + " = ?",
                new String[]{month});

        // If no rows were updated (month did not exist), INSERT a new row
        if (updatedRows == 0) {
            return db.insert(TABLE_NAME, null, values);
        } else {
            return 1;
        }
    }

    // Method to fetch all data for the History List View (only month and final cost)
    public Cursor getAllBillsCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Select ONLY the columns required for the List View (ID, Month, Final Cost)
        String[] columns = {COLUMN_ID, COLUMN_MONTH, COLUMN_FINAL_COST};
        return db.query(TABLE_NAME, columns, null, null, null, null, null);
    }

    // Method to fetch a single record's details for the DetailActivity
    public Cursor getBillById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME,
                null, // Select ALL columns for the detail page
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }
    // Method to update an existing bill record
    public boolean updateBill(long id, String month, double unit, double rebate, double total, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNIT, unit);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, total);
        values.put(COLUMN_FINAL_COST, finalCost);

        int count = db.update(TABLE_NAME,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        return count > 0;
    }

    // Method to delete a bill record
    public boolean deleteBill(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_NAME,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        return count > 0;
    }
}