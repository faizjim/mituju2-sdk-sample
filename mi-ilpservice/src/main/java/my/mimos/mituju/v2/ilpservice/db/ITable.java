package my.mimos.mituju.v2.ilpservice.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ariffin.ahmad on 14/06/2017.
 */

public interface ITable {
    String getName();
    void createTable(SQLiteDatabase db);
    void deleteTable(SQLiteDatabase db);
}
