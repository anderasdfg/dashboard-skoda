package de.kai_morich.simple_bluetooth_le_terminal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.Statement;

public class Login extends AppCompatActivity {

    //Declaramos nuestra variable de conexion

    Button btnIniciar;
    EditText txtusuario, txtclave;
    VariablesGlobales vg = VariablesGlobales.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnIniciar = (Button) findViewById(R.id.btnIniciar);
        txtusuario = (EditText) findViewById(R.id.txtUsuario);
        txtclave   = (EditText) findViewById(R.id.txtPassword);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth LE no soportado", Toast.LENGTH_SHORT).show();
            //finish();
        }

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckConnection();
                InciarSesion(txtusuario.getText().toString(),txtclave.getText().toString());
            }
        });

    }

    //Check connection
    public void CheckConnection(){
        try{
            if(Conexion.con == null){
                new Conexion().setConexion();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ASK", e.getMessage());
        }
    }



    //Crearemos la Función para Iniciar Sesion de Postgresql
    public  void InciarSesion(String usuario, String clave){
        Log.e("ASK", usuario + " - " + clave);

        if(usuario!="" && clave !=""){
            try{
                if(Conexion.con == null){
                    new Conexion().setConexion();
                }
                if(Conexion.con!=null){
                    Statement stmt = Conexion.con.createStatement();

                    String sql = "select * from usuarios where usuario = '" + usuario + "' and clave = '" + clave + "'";
                    ResultSet rs = stmt.executeQuery(sql);
                    Log.e("ASK", "----------------------------");
                    while(rs.next()){
                        if(rs.getString("id") != "0"){
                            Log.e("ASK", "ta bien");
                            Log.e("ASK", rs.getString("usuario") + " - " +  rs.getString("clave"));

                            String codigo = rs.getString("id");
                            String nombre = rs.getString("nombre");
                            String apellido = rs.getString("apellido");
                            String email = rs.getString("email");

                            vg.set_codigousuario(Integer.parseInt(codigo));
                            vg.set_nombre(nombre);
                            vg.set_apellido(apellido);
                            vg.set_correo(email);

                            //Abre el dashboard
                            Intent intent = new Intent (this, MainActivity.class);
                            startActivityForResult(intent, 0);

                        }else{
                            Toast.makeText(getApplicationContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show();
                            Log.e("ASK", "ta mal");
                            Log.e("ASK", rs.getString("usuario") + " - " +  rs.getString("clave"));
                        }

                    }
                    Log.e("ASK", "----------------------------");
                    // Toast.makeText(getApplicationContext(), "Query ejecutada OK", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Ocurrió un error, intente nuevamente más tarde", Toast.LENGTH_LONG).show();
                }
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ASK", e.getMessage());
            }
        }else{
            Toast.makeText(getApplicationContext(), "Debe completar todos los campos", Toast.LENGTH_LONG).show();
        }



    }
}