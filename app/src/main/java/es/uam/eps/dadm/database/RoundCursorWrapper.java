package es.uam.eps.dadm.database;

import static es.uam.eps.dadm.database.RoundDataBaseSchema.UserTable;
import static es.uam.eps.dadm.database.RoundDataBaseSchema.RoundTable;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import es.uam.eps.dadm.model.Round;
import es.uam.eps.multij.ExcepcionJuego;

/**
 * Clase auxiliar que nos convertirá de lo devuelto en una query a objetos de java
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class RoundCursorWrapper extends CursorWrapper {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundCuWr";

    /**
     * Constructor de la clase
     * @param cursor Datos devueltos de una consulta
     */
    public RoundCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    /**
     * Devuelve un objeto Round correpondiente a la consulta que acabamos de hacer
     * @return Round correspondiente a la consulta
     */
    public Round getRound() {
        // Obtenemos los valores de todos los parámetros
        String playername = getString(getColumnIndex(UserTable.Cols.PLAYERNAME));
        String playeruuid = getString(getColumnIndex(UserTable.Cols.PLAYERUUID));
        String rounduuid =  getString(getColumnIndex(RoundTable.Cols.ROUNDUUID));
        String date =       getString(getColumnIndex(RoundTable.Cols.DATE));
        String size =       getString(getColumnIndex(RoundTable.Cols.SIZE));
        String board =      getString(getColumnIndex(RoundTable.Cols.BOARD));

        // Creamos una ronda y se los asignamos uno a uno
        Round round = new Round(rounduuid, Round.Type.LOCAL, date, Integer.parseInt(size));
        round.setSecondUser(playername, playeruuid);

        try {
            round.getBoard().stringToTablero(board);
        } catch (ExcepcionJuego e) {
            Log.d(DEBUG, "Error turning string into tablero");
        }
        return round;
    }
}