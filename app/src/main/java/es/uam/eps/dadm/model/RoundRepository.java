package es.uam.eps.dadm.model;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase almacenará los datos de las partidas en la aplicación
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundRepository {
    /**
     * Instancia para la creación del singleton
     */
    private static RoundRepository repository;
    /**
     * Lista de partidas que tiene la aplicación
     */
    private List<Round> rounds;

    /**
     * Función que nos devolverá la instancia de la clase
     * @param context Conexto desde el que se le llama
     * @return Instancia de la clase
     */
    public static RoundRepository get(Context context) {
        // Si no está instanciada, creamos una clase
        if (repository == null)
            repository = new RoundRepository(context);
        // Devolvemos la instancia
        return repository;
    }

    /**
     * Constructor de la clase
     * @param context Contexto desde el que se le llama
     */
    private RoundRepository(Context context) {
        // Creamos la una lista de Partidas
        rounds = new ArrayList<Round>();
        // Llenamos la lista con 50 partidas generadas automáticamente
        for (int i = 0; i < 50; i++) {
            Round round = new Round();
            rounds.add(round);
        }
    }

    /**
     * Añade una partida a la lista
     * @param round Partida a añadir
     */
    public void addRound(Round round) { rounds.add(round); }

    /**
     * Devuelve la lista de partidas de la app
     * @return Lista de partidas
     */
    public List<Round> getRounds() {
        return rounds;
    }

    /**
     * Devuelve una partida en base a su identificador
     * @param id Id de la partida
     * @return Partida que se ha encontrado
     */
    public Round getRound(String id) {
        // Por cada ronda de la lista comparamos el id y si cuadra se devuelve
        for (Round round : rounds)
            if (round.getId().equals(id))
                return round;

        // Si no la encontramos, devolvemos null
        return null;
    }
}