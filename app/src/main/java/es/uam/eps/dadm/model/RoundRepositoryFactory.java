package es.uam.eps.dadm.model;
import android.content.Context;
import android.support.annotation.Nullable;

import es.uam.eps.dadm.database.DataBase;

/**
 * Clase manejadora de el almacenamiento de datos, con la cual crearemos de forma estática una
 * referencia al almacenamiento de datos desde el cual se manejará la app
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class RoundRepositoryFactory {

    /**
     * Define si estamos jugando en local o con una conexión a un servidor
     */
    private static final boolean LOCAL = true;

    /**
     * Crea una instancia del almacenamiento de datos y lo devuelve para poder manejar toda la
     * lógica de la app
     * @param context Contexto desde el cual se invoca el almacenamiento
     * @return Referencia al almacenamiento de datos
     */
    @Nullable
    public static RoundRepository createRepository(Context context) {
        // Creamos una referencia de la base de datos, local o en remoto
        RoundRepository repository = LOCAL ? new DataBase(context) : new DataBase(context);
        try {
            // Abrimos la referencia en el gestor de datos
            repository.open();
        }
        catch (Exception e) {
            // Si hay alguna excepción al abrirlo, devolvemos null
            return null;
        }
        // Devolvemos la referencia a la base de datos
        return repository;
    }
}