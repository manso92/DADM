package com.manso92.damas.model;

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
     * el callback de {@link com.manso92.damas.model.RoundRepository.LoginRegisterCallback}
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
     * @param filter No voy a mentir, aún no sé para que es esto
     * @param callback Callback a ejecutar al que se le notificará cómo funcionó la función
     */
    void getRounds(String playeruuid, String orderByField, Round.Type filter,
                   RoundsCallback callback);

    /**
     * Interfaz que deberán implementar el callback que pasaremos a las funciones
     * {@link #getRounds(String, String, Round.Type, RoundsCallback)}
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
     * Identifica el filtro por defecto que se utilizará para recuperar las partidas
     * @return Tipo por defecto del friltro para seleccionar partidas
     */
    Round.Type getDefaultFilter();

}