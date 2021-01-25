package de.kai_morich.simple_bluetooth_le_terminal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class Rendimiento extends AppCompatActivity {

    VariablesGlobales vg =  VariablesGlobales.getInstance();

    TextView txtFechaDiario, txtSpinDiario, txtVelocidadDiaria, txtFuerzaDiaria, txtSpinTitulo;
    int codigoUsuario = 0;

    Calendar c = Calendar.getInstance();

    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
    String formattedDate = df.format(c.getTime());

    String fecha = new SimpleDateFormat("EEEE ', ' dd MMMM ").format(new Date());

    LineChartView lineChartView;
    String[] axisData = {"6:00", "12:00", "6:00", "12:00"};
    int[] yAxisData = {26,12,35,25};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rendimiento);

        ImageButton btnSemanal = (ImageButton) findViewById(R.id.btnSemanal);
        txtFechaDiario = (TextView) findViewById(R.id.txtFechaDiario);
        txtFuerzaDiaria = (TextView) findViewById(R.id.txtFuerzaDiaria);
        txtSpinDiario = (TextView) findViewById(R.id.txtSpinDiario);
        txtVelocidadDiaria = (TextView) findViewById(R.id.txtVelocidadDiaria);
        txtSpinTitulo = (TextView) findViewById(R.id.txtTituloSpin);

        txtFechaDiario.setText(fecha);
        //txtSpinTitulo.setText(formattedDate);
        if(vg.get_codigousuario()!=0){
            codigoUsuario = vg.get_codigousuario();
        }

        obtenerPromedioSpinDia();


        lineChartView = findViewById(R.id.chart);

        List yAxisValues = new ArrayList();
        List axisValues = new ArrayList();


        Line line = new Line(yAxisValues).setColor(Color.parseColor("#C6FD01"));

        for (int i = 0; i < axisData.length; i++) {
            axisValues.add(i, new AxisValue(i).setLabel(axisData[i]));
        }

        for (int i = 0; i < yAxisData.length; i++) {
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }

        List lines = new ArrayList();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axis = new Axis();
        axis.setValues(axisValues);
        axis.setTextSize(8);
        axis.setTextColor(Color.parseColor("#FFFFFF"));
        data.setAxisXBottom(axis);

        Axis yAxis = new Axis();
        // yAxis.setName("Sales in millions");
        yAxis.setTextColor(Color.parseColor("#FFFFFF"));
        yAxis.setTextSize(8);
        data.setAxisYLeft(yAxis);

        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = 90;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);


    }

    public void obtenerPromedioSpinDia(){
        try{
            if(Conexion.con == null){
                new Conexion().setConexion();
            }
            if(Conexion.con!=null){
                Statement stmt = Conexion.con.createStatement();

                String sql = "SELECT avg(spin) as avgSpin, avg(fuerza) as avgFuerza,avg(velocidad) as avgVelocidad FROM resultados r INNER JOIN juego j ON r.idJuego= j.idjuego WHERE j.id = '"+ codigoUsuario +"';";
                ResultSet rs = stmt.executeQuery(sql);
                Log.e("ASK", "----------------------------");
                while(rs.next()){
                    Log.e("ASK", "AVG SPIN");
                    String spin = rs.getString("avgSpin");
                    Log.e("spin owo", spin );
                    int posPuntoSpin = spin.indexOf(".", 0);
                    spin = rs.getString("avgSpin") + " RPM";
                    txtSpinDiario.setText(spin.substring(0, posPuntoSpin + 3) + " RPM");

                    Log.e("ASK", "AVG FUERZA");
                    Log.e("ASK", rs.getString("avgFuerza") );
                    String fuerza = rs.getString("avgFuerza");
                    int posPuntoFuerza = fuerza.indexOf(".", 0);
                    txtFuerzaDiaria.setText(fuerza.substring(0, posPuntoFuerza + 3) + " N") ;

                    Log.e("ASK", "AVG VELOCIDAD");
                    Log.e("ASK", rs.getString("avgVelocidad") );
                    String velocidad = rs.getString("avgVelocidad");
                    int posPuntoVelocidad = velocidad.indexOf(".", 0);
                    txtVelocidadDiaria.setText(velocidad.substring(0, posPuntoVelocidad + 3) + " KM/h");
                }
                Log.e("ASK", "----------------------------");
            }else{
                Toast.makeText(getApplicationContext(), "Ocurrió un error, intente nuevamente más tarde", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ERROR SQL", e.getMessage());
        }
    }
}