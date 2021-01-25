package de.kai_morich.simple_bluetooth_le_terminal;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {

    public static Connection con;

    public void setConexion(){
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String ip = "200.121.128.122";
            String ConnURL = "jdbc:jtds:sqlserver://"+ip+";instance=SQLEXPRESS;user=sa;password=lolimsa@catalina;databasename=skoda";
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            con = DriverManager.getConnection(ConnURL);
            Log.e("ASK", "Connection Called");
        }catch (Exception e){
            Log.e("ASK", "Exepcion: " + e.getMessage());

        }
    }

}
