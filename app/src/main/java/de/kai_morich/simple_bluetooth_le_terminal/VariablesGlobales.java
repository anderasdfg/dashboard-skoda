package de.kai_morich.simple_bluetooth_le_terminal;

public class VariablesGlobales {
    private  static VariablesGlobales instance;

    //AQUI DECLARAREMOS NUESTRAS VARIABLES GLOBALES PARA ALMACENDAR LOS DATOS DE LA FUNCION DE INSIO DE SESION Y LUEGO MOSTRARLOS EN EL
    //ACTIVITY MENU

    private  static  int _codigousuario=0;
    private  static  String _nombre="";
    private  static  String _apellido="";
    private  static  String _correo="";
    private  static  int _codigojuego = 0;

    public  int get_codigousuario() {
        return _codigousuario;
    }

    public  void set_codigousuario(int _codigousuario) {
        VariablesGlobales._codigousuario = _codigousuario;
    }

    public  String get_nombre() {
        return _nombre;
    }

    public  void set_nombre(String _nombre) {
        VariablesGlobales._nombre = _nombre;
    }

    public  String get_apellido() {
        return _apellido;
    }

    public  void set_apellido(String _apellido) {
        VariablesGlobales._apellido = _apellido;
    }

    public  String get_correo() {
        return _correo;
    }

    public  void set_correo(String _correo) {
        VariablesGlobales._correo = _correo;
    }

    public  int get_codigojuego() {
        return _codigojuego;
    }

    public  void set_codigojuego(int _codigojuego) {
        VariablesGlobales._codigojuego = _codigojuego;
    }

    public static synchronized VariablesGlobales getInstance() {
        if (instance == null) {
            instance = new VariablesGlobales();
        }
        return instance;
    }
}

