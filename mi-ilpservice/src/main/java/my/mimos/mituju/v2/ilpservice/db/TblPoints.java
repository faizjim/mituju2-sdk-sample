package my.mimos.mituju.v2.ilpservice.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import my.mimos.miilp.core.model.Pin;
import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 14/06/2017.
 */

public class TblPoints implements ITable {
    public static final String table_name        = "tbl_point";
    public static final String[] col_id          = {"id", "TEXT", "PRIMARY KEY"};
    public static final String[] col_name        = {"name", "TEXT"};
    public static final String[] col_profileid   = {"profileid", "TEXT"};
    public static final String[] col_profilename = {"profilename", "TEXT"};
    public static final String[] col_originalx   = {"originalx", "REAL"};
    public static final String[] col_originaly   = {"originaly", "REAL"};
    public static final String[] col_scaledx     = {"scaledx", "REAL"};
    public static final String[] col_scaledy     = {"scaledy", "REAL"};
    public static final String[][] columns       = {col_id, col_name, col_profileid, col_profilename, col_originalx, col_originaly, col_scaledx, col_scaledy};

    private final DBMiTuju db_mituju;

    public TblPoints(DBMiTuju db_mituju) {
        this.db_mituju = db_mituju;
    }
//
//    public void addPoint(String id, String name, String profile_id, String profile_name, float x, float y) {
//        SQLiteDatabase db = db_mituju.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(col_id[0], id);
//        values.put(col_name[0], name);
//        values.put(col_profileid[0], profile_id);
//        values.put(col_profilename[0], profile_name);
//        values.put(col_x[0], x);
//        values.put(col_y[0], y);
//
//        db.insert(table_name, null, values);
//        db.close();
//    }

    public void addPoint(Iterable<Pin> pins, String profile_name, float map_scale) {
        SQLiteDatabase db = db_mituju.getWritableDatabase();

        for (Pin pin : pins) {
            //Log.wtf(MiTujuApplication.TAG, "Mi-ILP >>> updating pin: " + pin.getName() + "....");
            ContentValues values = new ContentValues();
            values.put(col_id[0], pin.getId());
            values.put(col_name[0], pin.getName());
            values.put(col_profileid[0], pin.getProfileId());
            values.put(col_profilename[0], profile_name);
            values.put(col_originalx[0], Math.round(pin.getX()));
            values.put(col_originaly[0], Math.round(pin.getY()));
            values.put(col_scaledx[0], Math.round(pin.getX() * map_scale));
            values.put(col_scaledy[0], Math.round(pin.getY() * map_scale));
            db.insert(table_name, null, values);
        }

        db.close();
    }

    public void clearRows() {
        SQLiteDatabase db = db_mituju.getWritableDatabase();
        db.execSQL("DELETE FROM " + table_name);
    }

    public List<ILPPoint> getMatched(String query) {
        List<ILPPoint> ret         = new ArrayList<>();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table_name);

        Cursor cursor = builder.query(db_mituju.getReadableDatabase(), null, col_name[0] + " MATCH ?", new String[] { query + "*" }, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ret.add(new ILPPoint(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4), cursor.getFloat(5), cursor.getFloat(6), cursor.getFloat(7)));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return ret;
    }

    public List<ILPPoint> getAll() {
        List<ILPPoint> ret = new ArrayList<>();
        Cursor cursor      = db_mituju.getReadableDatabase().rawQuery("SELECT * FROM " + table_name + " order by " + col_name[0], null);
        if (cursor.moveToFirst()) {
            do {
                ret.add(new ILPPoint(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4), cursor.getFloat(5), cursor.getFloat(6), cursor.getFloat(7)));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return ret;
    }

    public int countRows() {
        int ret           = -1;
        SQLiteDatabase db = db_mituju.getReadableDatabase();
        Cursor cursor     = db.rawQuery("SELECT * FROM " + table_name, null);
        ret               = cursor.getCount();
        cursor.close();

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

    public class ILPPoint {
        public final String id;
        public final String name;
        public final String profile_id;
        public final String profile_name;
        public final float original_x;
        public final float original_y;
        public final float scaled_x;
        public final float scaled_y;

        public ILPPoint(String id, String name, String profile_id, String profile_name, float x, float y, float scaled_x, float scaled_y) {
            this.id           = id;
            this.name         = name;
            this.profile_id   = profile_id;
            this.profile_name = profile_name;
            this.original_x   = x;
            this.original_y   = y;
            this.scaled_x     = scaled_x;
            this.scaled_y     = scaled_y;
        }
    }
}
