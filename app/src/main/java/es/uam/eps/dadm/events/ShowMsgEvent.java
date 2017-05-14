package es.uam.eps.dadm.events;


import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;


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

    /**
     * Duración del mensaje
     */
    private int duracion;

    private boolean shown = false;

    public ShowMsgEvent(Type tipo, String msg) {
        this.tipo = tipo;
        this.msg = msg;
        switch (tipo) {
            case SNACKBAR:
                this.duracion = Snackbar.LENGTH_LONG;
                break;
            case TOAST:
                this.duracion = Toast.LENGTH_LONG;
                break;
        }
    }

    public ShowMsgEvent(Type tipo, String msg, int duracion) {
        this.tipo = tipo;
        this.msg = msg;
        this.duracion = duracion;
    }

    public void show(View view) {
        if (!this.shown) {
            switch (this.getTipo()) {
                case SNACKBAR:
                    Snackbar.make(view, this.getMsg(), this.getDuracion()).show();
                    break;
                case TOAST:
                    Toast.makeText(view.getContext().getApplicationContext(), this.getMsg(), Toast.LENGTH_SHORT).show();
                    break;
            }
            this.shown = true;
        }
    }

    public Type getTipo() { return tipo; }
    public void setTipo(Type tipo) { this.tipo = tipo; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
}
