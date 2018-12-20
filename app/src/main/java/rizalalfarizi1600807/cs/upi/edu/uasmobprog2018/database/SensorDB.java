package rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.helper.OpenHelper;
import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.model.BrakeDetector;

public class SensorDB {


    private SQLiteDatabase db;
    private final OpenHelper dbHelper;

    public SensorDB(Context c){
        dbHelper = new OpenHelper(c);
    }

    public void open(){
        db  = dbHelper.getWritableDatabase();
    }

    public void close(){
        db.close();
    }


    public boolean insertSensor(BrakeDetector sensor) {
        ContentValues newValues = new ContentValues();
        newValues.put("TANGGAL", sensor.date);
        newValues.put("LATLONG", "Lat:"+sensor.lattitude+";Long:"+sensor.longitude);
        newValues.put("STATUS", sensor.brake_status);

        try{
            db.insert("SENSOR", null, newValues);
            return true;
        }catch (SQLException ex){
            throw ex;
        }

    }

    public ArrayList<BrakeDetector> getAllSensor() {
        Cursor cur = null;
        ArrayList<BrakeDetector> out = new ArrayList<>();
        cur = db.rawQuery("SELECT * FROM Sensor", null);

        if (cur.moveToFirst()) {
            do {
                BrakeDetector s = new BrakeDetector();
                s.id = Integer.valueOf(cur.getString(0));
                s.date = cur.getString(1);
                s.latlong = cur.getString(2);
                s.brake_status = cur.getString(3);
                s.lattitude = 0;
                s.longitude = 0;
                out.add(s);
            } while (cur.moveToNext());
        }
        cur.close();
        return out;
    }


    public boolean deleteAllSensor(){
        try {

            db.execSQL("DELETE FROM SENSOR");
            return true;
        }catch (SQLException ex){
            return false;
        }
    }
}