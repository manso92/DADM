package es.uam.eps.dadm.view.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * RecyclerItemClickListener manejará los clicks que se realicen en la lista de partidas disponibles
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    /**
     * Creamos un listener para los eventos a escuchar
     */
    private OnItemClickListener mListener;

    /**
     * Interfaz que definirá para los eventos tactiles
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Capturará los eventos en la interfaz gráfica
     */
    private GestureDetector mGestureDetector;

    /**
     * Creamos un manejador de eventos para la interfaz
     * @param context Conexto que la invoca
     * @param listener Listener que ejecutaremos con los eventos
     */
    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new
                GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });
    }

    /**
     * Captura los eventos dela interfaz y los maneja
     * @param view View que maneja el evento
     * @param e Evento que se realiza en la pantall
     * @return boolean
     */
    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        // Miramos donde se ha clickado
        View childView = view.findChildViewUnder(e.getX(), e.getY());

        // Si es una vista válida ejecutamos el listener con la visa
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e))
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
        return false;
    }
    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}