package de.kai_morich.simple_bluetooth_le_terminal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Cronometro extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cronometro);

    }

    /*
    TextView textViewHour1,textViewMinute1, textViewSecond1;
    private Button btnJugarFragment1;
    private int seconds = 0;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cronometro);
        textViewHour1 = (TextView) findViewById(R.id.text_view_hour1);
        textViewMinute1 = (TextView) findViewById(R.id.text_view_minute1);
        textViewSecond1 = (TextView) findViewById(R.id.text_view_second1);
        btnJugarFragment1 = findViewById(R.id.btnJugarFragment1);

        btnJugarFragment1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlayPause();
            }
        });
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
            //send("1");
            //contadorGolpes = 0;
        }else{
            //registrarInicioJuego(codigoUsuario,tiempo, formattedDate);  //registra el final del juego
            //insertarDataJuego(); //resultados
            //send("0");
            pause();
        }
        isPlaying = !isPlaying;
        btnJugarFragment1.setText(isPlaying ? "Detener" : "Jugar");
    }

    private void start() {
        handler.postDelayed(runnable, 1000);
    }

    private void pause() {
        handler.removeCallbacks(runnable);
    }

    private void resetarDatos(){

    }

    private void changeText() {
        int seconds = this.seconds % 60;
        int minutes = (this.seconds / 60) % 60;
        int hour = this.seconds / 3600;

        String secondsFormatted = (seconds <= 9 ? "0" : "") + String.valueOf(seconds);
        String minutesFormatted = (minutes <= 9 ? "0" : "") + String.valueOf(minutes);
        String hoursFormatted = (hour <= 9 ? "0" : "") + String.valueOf(hour);

        textViewSecond1.setText(secondsFormatted);
        textViewMinute1.setText(minutesFormatted);
        textViewHour1.setText(hoursFormatted);
    }
    */
}