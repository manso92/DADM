package es.uam.eps.dadm.database;

/**
 * Clase auxiliar con las cadenas de texto que hacen referencia a la base de datos
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class RoundDataBaseSchema {
    /**
     * Clase estática que contiene las cadenas de la tabla de los usuarios
     */
    public static final class UserTable {
        /**
         * Nombre de la tabla
         */
        public static final String NAME = "users";

        /**
         * Nombres de las columnas
         */
        public static final class Cols {
            public static final String PLAYERUUID = "playeruuid1";
            public static final String PLAYERNAME = "playername";
            public static final String PLAYERPASSWORD = "playerpassword";
        }
    }

    /**
     * Clase estática que contiene las cadenas de la tabla de las partidas
     */
    public static final class RoundTable {
        /**
         * Nombre de la tabla
         */
        public static final String NAME = "rounds";

        /**
         * Nombres de las columnas
         */
        public static final class Cols {
            public static final String PLAYERUUID = "playeruuid2";
            public static final String ROUNDUUID = "rounduuid";
            public static final String DATE = "date";
            public static final String TITLE = "title";
            public static final String SIZE = "size";
            public static final String BOARD = "board";
        }
    }
}