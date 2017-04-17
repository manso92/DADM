package es.uam.eps.dadm.model;

import java.util.List;

/**
 * Esta interfaz ayuda a la gestión de las partidas y de usuarios. De este modo nos da igual cómo
 * se gestionen, ya que mientras ese gestor implemente esta interfaz, nuestro código no
 * necesitará variar en nada
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public interface RoundRepository {
    /**
     * Abre una conexión con el gestor
     * @throws Exception Se lanzará una excepción en caso de que haya algún error
     */
    void open() throws Exception;

    /**
     * Cierra la conexión con el gestor
     */
    void close();

    /**
     * Interfaz que deberá implementar el callback que pasaremos a la función {@link #login(String, String, LoginRegisterCallback)}
     */
    interface LoginRegisterCallback {

        /**
         * Función que se ejecutará si el login se efectúa de forma correcta
         * @param playerUuid Identificador del jugador que se acaba de loguear
         */
        void onLogin(String playerUuid);

        /**
         * Función que se ejecutará si el logín no se efectúa de forma incorrecta
         * @param error Cadena con el error que se ha producido
         */
        void onError(String error);
    }

    /**
     * Función que comprueba si el usuario y la clave son correctos, y envía la respuesta a
     * el callback de {@link es.uam.eps.dadm.model.RoundRepository.LoginRegisterCallback}
     * @param playername Nombre del jugador que va a hacer login
     * @param password Password del usuario que va a hacer login
     * @param callback Callback que se ejecutará como respuesta al login
     */
    void login(String playername, String password, LoginRegisterCallback callback);

    /**
     * Registra un nuevo usuario en el gestor
     * @param playername Nombre del jugador
     * @param password Contraseña del jugador
     * @param callback Callback que se ejecutará como respuesta al registro
     */
    void register(String playername, String password, LoginRegisterCallback callback);

    /**
     * Interfaz que deberán implementar el callback que pasaremos a las funciones
     * {@link #addRound(Round, BooleanCallback)} y {@link #updateRound(Round, BooleanCallback)}
     */
    interface BooleanCallback {
        /**
         * Función a ejecutar en respuesta a determinadas acciones con las rondas
         * @param ok Boolean respuesta a cómo se ha ejecutado la función
         */
        void onResponse(boolean ok);
    }

    /**
     * Añade una partida al respositorio
     * @param round Partida que queremos añadir
     * @param callback Callback a ejecutar con la respuesta a la función
     */
    void addRound(Round round, BooleanCallback callback);

    /**
     * Actualiza una partida en el repositorio
     * @param round Partida que queremos actualizar
     * @param callback Callback a ejecutar con la respuesta a la función
     */
    void updateRound(Round round, BooleanCallback callback);

    /**
     * Devolverá la lista de partidas disponibles en el gestor que pertenecen al usuario
     * @param playeruuid Identificador de usuario
     * @param orderByField Orden en el que se devolverán las partidas
     * @param group No voy a mentir, aún no sé para que es esto
     * @param callback Callback a ejecutar al que se le notificará cómo funcionó la función
     */
    void getRounds(String playeruuid, String orderByField, String group,
                   RoundsCallback callback);

    /**
     * Interfaz que deberán implementar el callback que pasaremos a las funciones
     * {@link #getRounds(String, String, String, RoundsCallback)}
     */
    interface RoundsCallback {

        /**
         * En el caso de que se haya podido recuperar la lista de partidas, se le enviarán como
         * respuesta la lista de partidas que se nos había pedido
         * @param rounds Lista de partidas
         */
        void onResponse(List<Round> rounds);

        /**
         * En caso de error, se notificará con el mensaje que lo identifica
         * @param error Mensaje de error que se mandará
         */
        void onError(String error);
    }

    /**
     * Busca en el repositorio los usuarios registrados y el número de partidas que está jugando
     * y se lo manda al callback
     * @param callback Callback al que se le mandará la información
     */
    void getScores(ScoresCallback callback);

    /**
     * Callback que manejará la respuesta de {@link #getScores(ScoresCallback)}
     */
    interface ScoresCallback {

        /**
         * Maneja la lísta de usuarios y sus partidas
         * @param players Lista de jugadores
         * @param rounds Lista de partidas
         */
        void onResponse(List<String> players, List<Integer> rounds);

        /**
         * Maneja el error de que no haya datos de usuarios en el repositorio
         * @param error Mensaje de error para mostrarse
         */
        void onError(String error);
    }
}