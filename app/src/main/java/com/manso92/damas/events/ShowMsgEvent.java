package com.manso92.damas.events;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Clase que se encargará de indicar el error que hay que mostrar por pantalla
 *
 * @author Pablo Manso
 * @version 14/05/2017
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

    /**
     * Nos indica si alguna actividad ha mostrado ya el mensaje o no
     */
    private boolean shown = false;

    /**
     * Contruye un nuevo mensaje que mostrar
     * @param tipo Tipo de mensaje a mostrar
     * @param msg Mensaje a mostrar
     */
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

    /**
     * Contruye un nuevo mensaje que mostrar
     * @param tipo Tipo de mensaje a mostrar
     * @param msg Mensaje a mostrar
     * @param duracion Duración que tendrá el mensaje cuando se muestre
     */
    public ShowMsgEvent(Type tipo, String msg, int duracion) {
        this.tipo = tipo;
        this.msg = msg;
        this.duracion = duracion;
    }

    /**
     * Muestra el mensaje de error  alojado a una view
     * @param view View con la que mostraremos el error
     */
    public void show(View view) {
        // Comprobamos el tipo del mensaje
        switch (this.getTipo()) {
            case SNACKBAR:
                // Si es un snackbar, lo mostramos alojado a la view
                Snackbar.make(view, this.getMsg(), this.getDuracion()).show();
                break;
            case TOAST:
                // Si es un Toast y no se ha mostrado ya, lo mostramos
                if (!this.shown)
                    Toast.makeText(view.getContext().getApplicationContext(), this.getMsg(), Toast.LENGTH_SHORT).show();
                break;
        }
        // Indicamos que el mensaje ya se ha mostrado
        this.shown = true;
    }

    // SETTERS Y GETTERS
    public Type getTipo() { return tipo; }
    public void setTipo(Type tipo) { this.tipo = tipo; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
}
