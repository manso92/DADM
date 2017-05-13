package es.uam.eps.dadm.events;

/**
 * Clase que se encargará de indicar el error que hay que mostrar por pantalla
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class ShowMsgEvent {

    /**
     * Tipos de errores que se pueden mostrar
     */
    public enum Type{SNACKBAR, TOAST};

    /**
     * Tipo de mensaje a mostrar
     */
    private Type tipo;

    /**
     * Mesnaje de error a mostrar
     */
    private String msg;
    
    public ShowMsgEvent(Type tipo, String msg) {
        this.tipo = tipo;
        this.msg = msg;
    }

    public Type getTipo() { return tipo; }
    public void setTipo(Type tipo) { this.tipo = tipo; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
}
