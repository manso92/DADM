package es.uam.eps.dadm.view.alerts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.GREB;
import es.uam.eps.dadm.events.NewChatEvent;

/**
 * NewMessageDialogFragment es un dialog que mustra un cuadro de texto en el que se purede introducir
 * el nombre de un usuario con el que iniciar una conversación
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class NewMessageDialogFragment extends DialogFragment {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.NewMessageDF";

    /**
     * Crea un dialog para preguntar al usuario con quién quiere chatear
     * @param savedInstanceState Pares clave valor con valores que necesitará nuestro dialog
     * @return Dialog que acabamos de crear
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Creamos un constructor para el dialog y le colocamos el título
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // TODO coger el string de propiedades
        builder.setTitle("Title");

        // Cargamos el layout y se lo añadimos al builder
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.fragment_dialog_userchat, (ViewGroup) getView(), false);
        builder.setView(viewInflated);

        // Capturamos el EditText que nos dará el usuario con el que chatear
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);

        // Colocamos el botón afirmativo y su acción
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Enviamos el evento para que se cree la conversación y cerramos el dialog
                GREB.inst().post(new NewChatEvent(input.getText().toString()));
                dialog.dismiss();
            }
        });
        // Colocamos el botón netgativo y su acción
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerramos el dialog
                dialog.cancel();
            }
        });

        // Devolvemos el dialog que acabamos de crear
        return builder.create();
    }
}