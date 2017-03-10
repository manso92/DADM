package es.uam.eps.dadm.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;

/**
 * AlertDialogFragment es un dialog que se mostrará al final de las partidas para preguntar si se
 * desea realizar una nueva partida o no
 * @author Pablo Manso
 * @version 10/03/2017
 */
public class AlertDialogFragment extends DialogFragment {
    /**
     * Crea un dialog para preguntar al usuario si desea una nueva partida o no
     * @param savedInstanceState Pares clave valor con valores que necesitará nuestro dialog
     * @return Dialog que acabamos de crear
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Instanciamos la actividad
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        // Creamos el constructor del diálogo
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        // Ponemos un título al dialog
        // TODO revisar esta string
        alertDialogBuilder.setTitle(R.string.game_over);

        // TODO revisar esta string
        // Añadimos el mensaje que mostrará
        alertDialogBuilder.setMessage(R.string.game_over_message);

        // TODO revisar esta string
        // Añadimos el botón afirmativo y capturamos el evento al hacer click
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Creamos una nueva partida y la añadimos a nuestro repositorio
                        Round round = new Round(RoundRepository.SIZE);
                        RoundRepository.get(getActivity()).addRound(round);
                        // Si estamos en pantalla dividida, actualizamos la lista de partidas
                        if (activity instanceof RoundListActivity)
                            ((RoundListActivity) activity).onRoundUpdated(round);
                        // Sino, finalizamos la actividad de la partida
                        else
                            ((RoundActivity) activity).finish();

                        // Y cerramos el diálogo
                        dialog.dismiss();
                    }
                });

        // TODO revisar esta string
        // Añadimos el botón negativo y capturamos el evento al hacer click
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // En caso de que estemos en pantalla simple y no dividida, finalizamos la actividad
                        if (activity instanceof RoundActivity)
                            activity.finish();

                        // Y cerramos el diálogo
                        dialog.dismiss();
                    }
                });

        // Devolvemos el diálogo que acabamos de crear
        return alertDialogBuilder.create();
    }
}
