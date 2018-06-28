package my.mimos.mituju.v2.ilpservice.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 14/06/2017.
 */

public class DBMiTuju extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MiTujuDB";
    private static final int DATABASE_VERSION = 9;


    public final TblPoints tbl_point          = new TblPoints(this);
    public final TblSites tbl_sites           = new TblSites(this);
    private final ITable[] tables;

    public DBMiTuju(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.tables = new ITable[]{ tbl_point, tbl_sites };
    }

    public DBMiTuju(Context context, String db_name, int version, ITable[] tables) {
        super(context, db_name, null, version);
        this.tables = tables;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (ITable table : tables) {
            Log.w(ILPConstants.TAG, "creating table '" + table.getName() + "'");
            table.createTable(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
        Log.w(ILPConstants.TAG, "Upgrading database from version " + old_version + " to " + new_version + ", which will destroy all old data");
        for (ITable table : tables) {
            Log.w(ILPConstants.TAG, "deleting table '" + table.getName() + "'");
            table.deleteTable(db);
        }
        onCreate(db);
    }
}
