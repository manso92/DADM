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
     * Etiqueta que define la escritura en el DEBUG
     */
    private final String DEBUG = "DEBUG";


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
        String playername = getString(getColumnIndex(UserTable.Cols.PLAYERNAME));
        String playeruuid = getString(getColumnIndex(UserTable.Cols.PLAYERUUID));
        String rounduuid =  getString(getColumnIndex(RoundTable.Cols.ROUNDUUID));
        String date =       getString(getColumnIndex(RoundTable.Cols.DATE));
        String title =      getString(getColumnIndex(RoundTable.Cols.TITLE));
        String size =       getString(getColumnIndex(RoundTable.Cols.SIZE));
        String board =      getString(getColumnIndex(RoundTable.Cols.BOARD));

        Round round = new Round();
        round.setPlayerName(playername);
        round.setPlayerUUID(playeruuid);
        round.setId(rounduuid);
        round.setDate(date);
        round.setTitle(title);

        try {
            round.getBoard().stringToTablero(board);
        } catch (ExcepcionJuego e) {
            Log.d(DEBUG, "Error turning string into tablero");
        }
        return round;
    }
}