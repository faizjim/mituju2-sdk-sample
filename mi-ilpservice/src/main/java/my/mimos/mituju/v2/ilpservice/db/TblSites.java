package my.mimos.mituju.v2.ilpservice.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import my.mimos.mituju.v2.ilpservice.ILPConstants;
import my.mimos.mituju.v2.ilpservice.struc.ILPSiteInfo;

/**
 * Created by ariffin.ahmad on 14/06/2017.
 */

public class TblSites implements ITable {
    public static final String table_name          = "tbl_site";
    public static final String[] col_id            = {"site_id", "INTEGER", "PRIMARY KEY"};
    public static final String[] col_fp_id         = {"fp_id", "INTEGER"};
    public static final String[] col_map_id        = {"map_id", "INTEGER"};
    public static final String[] col_map_type      = {"map_type", "INTEGER", "PRIMARY KEY"};
    public static final String[] col_org           = {"org", "TEXT"};
    public static final String[] col_site          = {"site", "TEXT"};
    public static final String[] col_fp_timestamp  = {"fp_timestamp", "REAL"};
    public static final String[] col_map_timestamp = {"map_timestamp", "REAL"};
    public static final String[][] columns         = {col_id, col_fp_id, col_map_id, col_map_type, col_org, col_site, col_fp_timestamp, col_map_timestamp};

    private final DBMiTuju db_mituju;

    public TblSites(DBMiTuju db_mituju) {
        this.db_mituju = db_mituju;
    }

    public ILPSiteInfo addSite(int id, int fp_id, int map_id, int map_type, String org, String site) {
        SQLiteDatabase db = db_mituju.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(col_id[0], id);
        values.put(col_fp_id[0], fp_id);
        values.put(col_map_id[0], map_id);
        values.put(col_map_type[0], map_type);
        values.put(col_org[0], org);
        values.put(col_site[0], site);

        db.insert(table_name, null, values);
        db.close();

        return new ILPSiteInfo(this, id, fp_id, map_id, map_type, org, site, 0, 0);
    }

    public int updateSite(int id, int fp_id, int map_id, int map_type, String org, String site) {
        SQLiteDatabase db    = db_mituju.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col_fp_id[0], fp_id);
        values.put(col_map_id[0], map_id);
//        values.put(col_map_type[0], map_type);
        values.put(col_org[0], org);
        values.put(col_site[0], site);

        int ret = db.update(table_name, values, col_id[0] + "=" + id + " and " + col_map_type[0] + "=" + map_type, null);
        db.close();

        return ret;
    }

    public int updateFingerprintTimestamp(int id, int map_type, long timestamp) {
        SQLiteDatabase db = db_mituju.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(col_fp_timestamp[0], timestamp);
        int ret = db.update(table_name, cv, col_id[0] + "=" + id + " and " + col_map_type[0] + "=" + map_type, null);
        db.close();

        return ret;
    }

    public int updateMapTimestamp(int id, int map_type, long timestamp) {
        SQLiteDatabase db = db_mituju.getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(col_map_timestamp[0], timestamp);
        int ret = db.update(table_name, cv, col_id[0] + "=" + id + " and " + col_map_type[0] + "=" + map_type, null);
        db.close();

        return ret;
    }

    public ArrayList<ILPSiteInfo> get() {
        ArrayList<ILPSiteInfo> ret  = new ArrayList<>();
        SQLiteDatabase db       = db_mituju.getReadableDatabase();
        Cursor cursor           = db.rawQuery("SELECT * FROM " + table_name + " ORDER BY CASE " + col_map_type[0] + " WHEN 2 THEN 1 WHEN 3 THEN 2 WHEN 1 THEN 3 ELSE 4 END", null);
        if (cursor.moveToFirst()) {
            do {
                ret.add(new ILPSiteInfo(this, cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5), cursor.getLong(6), cursor.getLong(7)));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return ret;
    }

    public int deleteSite(int id, int map_type) {
        SQLiteDatabase db = db_mituju.getWritableDatabase();
        int ret = db.delete(table_name, col_id[0] + "=" + id + " and " + col_map_type[0] + "=" + map_type, null);
        db.close();

        return ret;
    }

    @Override
    public String getName() {
        return table_name;
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        String sql = "CREATE VIRTUAL TABLE " + table_name + " USING fts3(";
        for (int i = 0; i < columns.length; i++) {
            String[] column  = columns[i];
            if (i > 0)
                sql += ", ";
            sql     += column[0] + " " + column[1] + (column.length > 2 ? " " + column[2] : "");
        }
        sql         += ")";
        Log.wtf(ILPConstants.TAG, "tbl_point > create table: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void deleteTable(SQLiteDatabase db) {
        Log.wtf(ILPConstants.TAG, "tbl_point > recreating table...");
        String sql = "DROP TABLE IF EXISTS " + table_name;
        db.execSQL(sql);
    }
}
