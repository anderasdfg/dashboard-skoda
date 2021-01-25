package de.kai_morich.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Dashboard extends AppCompatActivity{

    private final int REQUEST_ENABLE_BT = 2;

    Button btnJugar;
    ImageButton btnEncenderBluettoth, btnPrueba;
    boolean isOn = false;
    TextView txtCronometro, txtTiempoJuego, txtSpin, txtGolpes,txtFuerza,txtVelocidad, txtFecha;
    Thread cronos;
    int mili = 0, seg = 0, minutos = 0;
    Handler h = new Handler();
    VariablesGlobales vg =  VariablesGlobales.getInstance();
    int codigoUsuario = 0;
    String tiempo;
    String dt;
    Resultados resAux;
    static ArrayList<Resultados> resultados = new ArrayList();
    int golpes = 0;
    double fuerza = 0 , spin = 0, velocidad = 0;

    String arraySpin [] = new String[10000];
    String arrayFuerza [] = new String[10000];
    String arrayVelocidad [] = new String[10000];

    Double arrayNSpin [] = new Double[10000];
    Double arrayNFuerza [] = new Double[10000];
    Double arrayNVelocidad [] = new Double[10000];


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if(vg.get_codigousuario()!=0){
            codigoUsuario = vg.get_codigousuario();
        }

       // Button btnVerResumen = (Button) findViewById(R.id.btnVerResumen);
        btnJugar = (Button) findViewById(R.id.btnJugar);
        btnEncenderBluettoth = (ImageButton) findViewById(R.id.encenderBluetooth);
        btnPrueba = (ImageButton) findViewById(R.id.btnPrueba);
        txtCronometro = (TextView) findViewById(R.id.txtTiempo);
        txtTiempoJuego = (TextView) findViewById(R.id.textTiempoJuego);
        txtSpin = (TextView) findViewById(R.id.txtcantidadSpin);
        txtGolpes = (TextView) findViewById(R.id.txtCantidadGolpes);
        txtFuerza = (TextView) findViewById(R.id.txtCantidadFuerza);
        txtVelocidad = (TextView) findViewById(R.id.txtCantidadVelocidad);
        txtFecha = (TextView) findViewById(R.id.txtFechaCabecera);
        String fecha = new SimpleDateFormat("EEEE ', ' dd MMMM ").format(new Date());
        txtFecha.setText(fecha);


        txtCronometro.setText("00:00.00");
     /*   btnVerResumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent (v.getContext(), Rendimiento.class);
                //startActivityForResult(intent, 0);
            }
        });*/

        btnPrueba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarValores("Spin: 245 Fuerza: 205 Velocidad: 78");
            }
        });



        btnEncenderBluettoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent (v.getContext(), MainActivity.class);
                startActivityForResult(intent, 0);

            }
        });


        btnJugar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                btnJugar.setText("Detener");

                txtFuerza.setText("0");
                txtGolpes.setText("0");
                txtSpin.setText("0");
                txtVelocidad.setText("0");

                if (!isOn){
                    isOn = true;
                    txtFuerza.setText("0");
                    txtGolpes.setText("0");
                    txtSpin.setText("0");
                    txtVelocidad.setText("0");
                }else{
                    isOn = false;
                    registrarInicioJuego(codigoUsuario,tiempo, formattedDate);  //registra el final del juego
                    insertarDataJuego(); //resultados

                    btnJugar.setText("Jugar");
                    mili = 0;
                    seg = 0;
                    minutos = 0;
                    golpes = 0;
                    resultados.clear();

                   txtTiempoJuego.setText(tiempo);
                }
                cronos = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while(true){
                            if(isOn){
                                try {
                                    Thread.sleep(1000);
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                                mili ++;
                                if(mili == 99){
                                    seg++;
                                    mili = 0;
                                }
                                if(seg == 59){
                                    minutos ++;
                                    seg = 0;
                                }
                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String m="", s="", mi="";
                                        if(mili<10){
                                            m=""+mili;
                                        }else if(mili<100){
                                            m=""+mili;
                                        }else{
                                            m=""+mili;
                                        }
                                        if(seg<10){
                                            s="0"+seg;
                                        }else{
                                            s=""+seg;
                                        }
                                        if(minutos <10){
                                            mi = "0"+minutos;
                                        }else{
                                            mi = "" + minutos;
                                        }
                                        txtCronometro.setText(mi+":"+s+ "." + m);
                                        tiempo = mi+":"+s+ "." + m;
                                    }
                                });
                            }
                        }
                    }
                });
                cronos.start();
            }
        });

    }
    public void registrarInicioJuego(int codigoUsuario, String tiempo, String fecha){

        try{
            if(Conexion.con == null){
                new Conexion().setConexion();
            }
            if(Conexion.con!=null){
                PreparedStatement comm;
                try {
                    comm = Conexion.con.prepareStatement("insert into juego values (" + codigoUsuario + ",'" + tiempo + "','" + fecha + "');");
                    comm.executeUpdate();
                } catch (SQLException e) {
                    Log.e("ASK", e.toString());
                }
            }else{
                Toast.makeText(getApplicationContext(), "Ocurrió un error, intente nuevamente más tarde", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ASK", e.getMessage());
        }
    }

    int s,f,v = 0;
    public void agregarValores(String data){



        s = (int) (Math.random() * 500) + 1;
        f = (int) (Math.random() * 500) + 1;
        v = (int) (Math.random() * 500) + 1;
        //Agregar spin
        resAux = new Resultados(s,f,v);

        arraySpin[golpes] = String.valueOf(f);
        txtSpin.setText(arraySpin[golpes]);
        //Agregar fuerza
        arrayFuerza[golpes] = String.valueOf(s);
        txtFuerza.setText(arrayFuerza[golpes]);

        //Agregar velocidad
        arrayVelocidad[golpes] = String.valueOf(v);
        txtVelocidad.setText(arrayVelocidad[golpes]);

        golpes = golpes + 1;
        txtGolpes.setText(String.valueOf(golpes));

        resultados.add(resAux);

    }
    public void insertarDataJuego() {
        try{
            if(Conexion.con == null){
                new Conexion().setConexion();
            }
            if(Conexion.con!=null){
                Statement stmt = Conexion.con.createStatement();

                String sql = "select top 1 * from Juego where id = " + codigoUsuario + "order by fechaJuego desc";
                ResultSet rs = stmt.executeQuery(sql);

                while(rs.next()){
                    Log.e("ASK", rs.getString("idJuego") + " - " +  rs.getString("id"));

                    String idJuego = rs.getString("idJuego");
                    vg.set_codigojuego(Integer.parseInt(idJuego));

                    for(Resultados res : resultados){
                        PreparedStatement comm;
                        try {
                            comm = Conexion.con.prepareStatement("insert into resultados values (" + vg.get_codigojuego() + ",'" + res.getSpin() + "','" + res.getFuerza() + "','"+ res.getVelocidad() +"');");
                            comm.executeUpdate();
                        } catch (SQLException e) {
                            Log.e("ASK", e.toString());
                        }
                    }
                }
                Log.e("ASK", "----------------------------");
            }else{
                Toast.makeText(getApplicationContext(), "Ocurrió un error, intente nuevamente más tarde", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ASK", e.getMessage());
        }
    }
}