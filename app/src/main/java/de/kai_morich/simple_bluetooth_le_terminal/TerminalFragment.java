package de.kai_morich.simple_bluetooth_le_terminal;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;


    private Button btnJugarFragment;
    ImageButton btnEncenderBluettoth, btnPrueba;
    boolean isOn = false;
    TextView txtCronometro, txtSpin, txtGolpes,txtFuerza,txtVelocidad,textViewHour,textViewMinute, textViewSecond; //, txtFecha;
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
    private int seconds = 0;
    private boolean isPlaying = false;

    String arraySpin [] = new String[10000];
    String arrayFuerza [] = new String[10000];
    String arrayVelocidad [] = new String[10000];

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");

        if(vg.get_codigousuario()!=0){
            codigoUsuario = vg.get_codigousuario();
        }
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
        sendText = view.findViewById(R.id.send_text);
        btnJugarFragment = view.findViewById(R.id.btnJugarFragment);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");


        //Button btnVerResumen = (Button) view.findViewById(R.id.btnVerResumen);
        btnEncenderBluettoth = (ImageButton) view.findViewById(R.id.encenderBluetooth);
        btnPrueba = (ImageButton) view.findViewById(R.id.btnPrueba);
        //txtCronometro = (TextView) view.findViewById(R.id.txtTiempo);
        //txtTiempoJuego = (TextView) view.findViewById(R.id.textTiempoJuego);
        textViewHour = (TextView) view.findViewById(R.id.text_view_hour);
        textViewMinute = (TextView) view.findViewById(R.id.text_view_minute);
        textViewSecond = (TextView) view.findViewById(R.id.text_view_second);
        txtSpin = (TextView) view.findViewById(R.id.txtcantidadSpin);
        txtGolpes = (TextView) view.findViewById(R.id.txtCantidadGolpes);
        txtFuerza = (TextView) view.findViewById(R.id.txtCantidadFuerza);
        txtVelocidad = (TextView) view.findViewById(R.id.txtCantidadVelocidad);

        //txtCronometro.setText("00:00.00");

        //View sendBtn = view.findViewById(R.id.send_btn);
        //sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));
        btnJugarFragment.setOnClickListener(v -> btnPlayPause());

        /*
        btnVerResumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent (v.getContext(), Rendimiento.class);
                 startActivityForResult(intent, 0);
            }
        });*/

        return view;
    }
    /*
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    public void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    public void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    public void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg+'\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    String datosEntrada = "";
    String datosEntrada2= "";
    String datosEntradaSPM  = "";
    int contadorGolpes = 0;
    public void receive(byte[] data) {
        Log.e("---------", "----------------------------------------------------------");
        if(hexEnabled) {
            //receiveText.append(TextUtil.toHexString(data) + '\n');
        } else {
            String msg = new String(data);
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = receiveText.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            //receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
            datosEntrada+= TextUtil.toCaretString(msg, newline.length() != 0).toString();
            datosEntrada2+= TextUtil.toCaretString(msg, newline.length() != 0).toString();
            datosEntradaSPM+= TextUtil.toCaretString(msg, newline.length() != 0).toString();
            Log.e("AL FINAL", datosEntrada2);

            //Usa RPM para los golpes

            String sTextoBuscado = "RPM";
            Log.e("IndexOf(RPM)", Integer.toString(datosEntrada.indexOf(sTextoBuscado) + +sTextoBuscado.length()));
            while (datosEntrada.indexOf(sTextoBuscado) > -1) {
                datosEntrada = datosEntrada.substring(datosEntrada.indexOf(sTextoBuscado)+sTextoBuscado.length(),datosEntrada.length());
                contadorGolpes++;
            }
            Log.e("Cantidad golpes ", Integer.toString(contadorGolpes));
            txtGolpes.setText(Integer.toString(contadorGolpes));
            ///Control de RPM

            String sTextoRpm = "RPM: ";
            String rpm = "";
            Log.e("ubi de RPM:", Integer.toString(datosEntrada2.lastIndexOf(sTextoRpm)));
            if(datosEntrada2.lastIndexOf(sTextoRpm) > -1){
                int pos = datosEntrada2.lastIndexOf(sTextoRpm) + 5;
                rpm = datosEntrada2.substring(pos, pos + 6);
                rpm = rpm.replace('F', ' ').trim();
                Log.e("RPM", rpm.trim());
            }else{
                rpm = "0";
            }

            String sTextoFuerza = "Fuerza: ";
            String fuerza = "";
            Log.e("ubi de Fuerza", Integer.toString(datosEntrada2.lastIndexOf(sTextoFuerza)));

            if(datosEntrada2.lastIndexOf(sTextoFuerza) > -1){
                int posF = datosEntrada2.lastIndexOf(sTextoFuerza) + 8;
                Log.e("pos Fuerza", Integer.toString(posF));
                Log.e("Tamaño total", Integer.toString(datosEntrada2.length()));
                Log.e("POSF + 3", Integer.toString(posF +3));
                if((posF +3) < datosEntrada2.length()){
                    fuerza = datosEntrada2.substring(posF,posF + 3 ).trim();
                }else{
                    fuerza = "0";
                }
                Log.e("Fuerza", fuerza);
            }else{
                fuerza = "0";
            }



            String sTextoVelocidad = "Velocidad: ";
            String velocidad = "";
            Log.e("ubi de Velocidad", Integer.toString(datosEntrada2.lastIndexOf(sTextoVelocidad)));

            if(datosEntrada2.lastIndexOf(sTextoVelocidad) > -1){
                int posv = datosEntrada2.lastIndexOf(sTextoVelocidad) + 11;
                Log.e("pos ve", Integer.toString(posv));
                Log.e("Tamaño total", Integer.toString(datosEntrada2.length()));
                Log.e("POSv + 3", Integer.toString(posv +3));
                if((posv +3) < datosEntrada2.length()){
                    velocidad = datosEntrada2.substring(posv,posv + 4 ).trim();
                }else{
                    velocidad = "0";
                }
                Log.e("Velocidad", velocidad);
            }else{
                velocidad = "0";
            }

            if (((rpm != "0") && (fuerza != "0") && (velocidad != "0")) || ((rpm != "juego") && (fuerza != "go"))){
                Log.e("Valores", rpm + " - " + fuerza + " - " + velocidad);
                agregarValores(rpm, fuerza, velocidad);
            }


            Log.e("---------", "----------------------------------------------------------");

        }

    }

    public void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //receiveText.append(spn);
        //Log.e("Receive text en status", receiveText.getText().toString());
    }
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seconds ++;
            changeText();
            handler.postDelayed(this, 1000);
        }
    };

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }
    /*
    public void ejecutarJugar(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        send("1");
        contadorGolpes = 0;
        btnJugarFragment.setText("Detener");

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
            send("0");
            btnJugarFragment.setText("Jugar");
            mili = 0;
            seg = 0;
            minutos = 0;
            golpes = 0;
            resultados.clear();

            //txtTiempoJuego.setText(tiempo);
        }
        cronos = new Thread(new Runnable() {
            @Override
            public void run() {

                while(true){
                    if(isOn){
                        try {
                            Thread.sleep(5);
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
    }*/

    private void btnRefresh() {
        this.seconds = -1;
    }

    private void btnPlayPause() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        if(!isPlaying){
            btnRefresh();
            start();
            resetarDatos();
            send("1");
            contadorGolpes = 0;
        }else{
            registrarInicioJuego(codigoUsuario,tiempo, formattedDate);  //registra el final del juego
            insertarDataJuego(); //resultados
            send("0");
            pause();
        }
        isPlaying = !isPlaying;
        btnJugarFragment.setText(isPlaying ? "Detener" : "Jugar");
    }

    private void start() {
        handler.postDelayed(runnable, 1000);
    }

    private void pause() {
        handler.removeCallbacks(runnable);
    }

    private void resetarDatos(){
        txtFuerza.setText("0");
        txtGolpes.setText("0");
        txtSpin.setText("0");
        txtVelocidad.setText("0");
    }

    private void changeText() {
        int seconds = this.seconds % 60;
        int minutes = (this.seconds / 60) % 60;
        int hour = this.seconds / 3600;

        String secondsFormatted = (seconds <= 9 ? "0" : "") + String.valueOf(seconds);
        String minutesFormatted = (minutes <= 9 ? "0" : "") + String.valueOf(minutes);
        String hoursFormatted = (hour <= 9 ? "0" : "") + String.valueOf(hour);

        textViewSecond.setText(secondsFormatted);
        textViewMinute.setText(minutesFormatted);
        textViewHour.setText(hoursFormatted);
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
                Log.e("ASK", "Ocurrió un error, intente nuevamente más tarde");
                //Toast.makeText(getApplicationContext(), "Ocurrió un error, intente nuevamente más tarde", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ASK", e.getMessage());
        }
    }

    //int s,f,v = 0;
    public void agregarValores(String spin, String fuerza, String velocidad){

        //int  s = Integer.parseInt(spin.trim());
        //int  f = Integer.parseInt(fuerza.trim());
        //int  v = Integer.parseInt(velocidad.trim());
       //v = (int) (Math.random() * 500) + 1;

       // Log.e("valores", spin + " - " + fuerza);


        //Agregar spin
       // resAux = new Resultados(s,f,v);

        // arraySpin[golpes] = String.valueOf(f);
        txtSpin.setText(spin);
        //Agregar fuerza
        //arrayFuerza[golpes] = String.valueOf(s);
        txtFuerza.setText(fuerza);

        //Agregar velocidad
        //arrayVelocidad[golpes] = String.valueOf(v);
        txtVelocidad.setText(velocidad);

        //golpes = golpes + 1;
        //txtGolpes.setText(String.valueOf(golpes));

        //resultados.add(resAux);

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
                Log.e("ASK", "Ocurrió un error, intente nuevamente más tarde");
            }
        }catch(Exception e){
            //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ASK", e.getMessage());
        }
    }

}
