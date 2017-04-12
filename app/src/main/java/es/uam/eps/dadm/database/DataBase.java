package es.uam.eps.dadm.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;

import static es.uam.eps.dadm.database.RoundDataBaseSchema.RoundTable;
import static es.uam.eps.dadm.database.RoundDataBaseSchema.UserTable;


/**
 * Clase controladora de la base de datos de usuarios y partidas. Implementa la interfaz
 * {@link RoundRepository} para que funcione con el resto de la lógica de la aplicación
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class DataBase implements RoundRepository {

    /**
     * TAG que utilizaremos para escribir en el log
     */
    private final String DEBUG_TAG = "DEBUG";

    /**
     * Nombre que le daremos a la base de datos en el sistema de ficheros
     */
    private static final String DATABASE_NAME = "er.db";

    /**
     * Versión de la base de datos que utilizaremos
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Clase auxiliar que nos ayudará en el manejo de la base de datos
     */
    private DatabaseHelper helper;

    /**
     * Referencia de la base de datos con la que operaremos
     */
    private SQLiteDatabase db;

    /**
     * Constructor de la base de datos
     * @param context Contexto desde el cual se la invoca
     */
    public DataBase(Context context) {  }

    /**
     * Clase auxiliar que nos ayudará a crear y actualizar la base de datos cuando sea necesario
     * @author Pablo Manso
     * @version 12/02/2017
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        /**
         * Crea una instancia de la base de datos con referencia a las constantes que corresponden
         * a la clase DataBase
         * @param context Referencia a la clase que nos está creando
         */
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Función que se ejecutará cuando se cree la base de datos para configurarla
         * @param db Base de datos que se acaba de crear
         */
        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        /**
         * Función que actualizará la base de datos cuando la versión de la misma se modificque
         * @param db Base de datos a acutlizar
         * @param oldVersion Versión de la base de datos antigua
         * @param newVersion Nueva versión de la base de datos
         */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Eliminamos las tablas antiguas de la base de datos
            db.execSQL("DROP TABLE IF EXISTS " + UserTable.NAME);
            db.execSQL("DROP TABLE IF EXISTS " + RoundTable.NAME);

            // Creamos las tablas de nuevo
            createTable(db);
        }

        /**
         * Crea las tablas necesarias en la base de datos
         * @param db Base de datos donde crearlas
         */
        private void createTable(SQLiteDatabase db) {
            // String que define la creación de la tabla de usuarios
            String str1 = "CREATE TABLE " + RoundDataBaseSchema.UserTable.NAME + " ("
                    + "_id integer primary key autoincrement, "
                    + UserTable.Cols.PLAYERUUID + " TEXT UNIQUE, "
                    + UserTable.Cols.PLAYERNAME + " TEXT UNIQUE, "
                    + UserTable.Cols.PLAYERPASSWORD + " TEXT);";

            // String que define la creación de la tabla de las partidas
            String str2 = "CREATE TABLE " + RoundDataBaseSchema.RoundTable.NAME + " ("
                    + "_id integer primary key autoincrement, "
                    + RoundTable.Cols.ROUNDUUID + " TEXT UNIQUE, "
                    + RoundTable.Cols.PLAYERUUID + " TEXT REFERENCES "+ UserTable.Cols.PLAYERUUID + ", "
                    + RoundTable.Cols.DATE + " TEXT, "
                    + RoundTable.Cols.TITLE + " TEXT, "
                    + RoundTable.Cols.SIZE + " TEXT, "
                    + RoundTable.Cols.BOARD + " TEXT);";
            try {
                // Ejecutamos las dos consultas en la base de datos
                db.execSQL(str1);
                db.execSQL(str2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene una referencia de la base de datos para que podamos empezar a escribir en ella
     * @throws SQLException En caso de que algo salga mal, se lanzará la expcepción
     */
    @Override
    public void open() throws SQLException {
        // Obtiene una referencia de la base de datos
        db = helper.getWritableDatabase();
    }

    /**
     * Cierra la referencia de la base de datos que se obtuvo con la función {@link #open()}
     */
    @Override
    public void close() {
        db.close();
    }

    /**
     * Realiza la comprobación de usuario y contraseña buscando en la base de datos si hay correspondencia
     * e invoca el callback con el resultado
     *
     * @param playername Nombre del usuario
     * @param playerpassword Contraseña del usuario
     * @param callback Callback al que invocar con el resultado de la función
     */
    @Override
    public void login(String playername, String playerpassword,
                      LoginRegisterCallback callback) {
        // Registra en el log el usuario que quiere hacer login
        Log.d(DEBUG_TAG, "Login " + playername);
        // Consultamos a la base de datos por el nombre de usuario y la contraseña
        Cursor cursor = db.query(UserTable.NAME,
                new String[]{UserTable.Cols.PLAYERUUID},
                UserTable.Cols.PLAYERNAME + " = ? AND " + UserTable.Cols.PLAYERPASSWORD + " = ?",
                new String[]{playername, playerpassword}, null, null, null);
        // Obtenemos el número de registros devueltos
        int count = cursor.getCount();
        // Obtenemos el uuid del usuario en caso de que el login sea correcto
        String uuid = count == 1 && cursor.moveToFirst() ? cursor.getString(0) : "";
        // Eliminamos la respuesta
        cursor.close();
        // Si el login es correcto, llamamamos al callback del login
        if (count == 1)
            callback.onLogin(uuid);
        // Si el login es incorrecto, llamamamos al callback con el error
        else
            callback.onError(Resources.getSystem().getString(R.string.loginerror));
    }

    /**
     * Registra un usuario en la base de datos
     * @param playername Nombre de usuario
     * @param password Contraseña del usuario
     * @param callback Callback a ejecutar una vez ejecutada
     */
    @Override
    public void register(String playername, String password, LoginRegisterCallback callback) {
        // Creamos un uuid para el usuario
        String uuid = UUID.randomUUID().toString();

        // Creamos un contenedor para almacenar los datos a insertar
        ContentValues values = new ContentValues();
        // Insertamos el uuid, el nombre de usuario y la password
        values.put(UserTable.Cols.PLAYERUUID, uuid);
        values.put(UserTable.Cols.PLAYERNAME, playername);
        values.put(UserTable.Cols.PLAYERPASSWORD, password);

        // Insertamos los valores en la tabla de usuario
        long id = db.insert(UserTable.NAME, null, values);

        // Si la respuesta es negativa devolvemos un error
        if (id < 0)
            callback.onError(Resources.getSystem().getString(R.string.registererror) + playername);
        // Si la respuesta es afirmativa, llamamos al login con el uuid generado
        else
            callback.onLogin(uuid);
    }

    /**
     * Rellena los datos de un contendor para insertar en la base de datos
     * @param round Ronda de la que obtener los datos
     * @return Contenedor listo para insertar en la base de datos
     */
    private ContentValues getContentValues(Round round) {
        ContentValues values = new ContentValues();
        values.put(RoundTable.Cols.PLAYERUUID, round.getPlayerUUID());
        values.put(RoundTable.Cols.ROUNDUUID,  round.getId());
        values.put(RoundTable.Cols.DATE,       round.getDate());
        values.put(RoundTable.Cols.TITLE,      round.getTitle());
        values.put(RoundTable.Cols.SIZE,       8); // TODO hacer referencia a cambiar el tamaño del tablero
        values.put(RoundTable.Cols.BOARD,      round.getBoard().tableroToString());

        return values;
    }

    /**
     * Añade una nueva partida a la base de datos
     * @param round Partida que se va a insertar en la base de datos
     * @param callback Función a llamar tras la creación
     */
    @Override
    public void addRound(Round round, BooleanCallback callback) {
        // Creamos un contenedor con los datos de la partida
        ContentValues values = getContentValues(round);

        // Insertamos la partida en la base de datos
        long id = db.insert(RoundTable.NAME, null, values);

        // Si nos pasan un callback, lo ejecutamos con la respuesta de si se ha realizado
        // de forma correcta
        if (callback != null)
            callback.onResponse(id >= 0);
    }

    /**
     * Actualiza una ronda en la base de datos
     * @param round Ronda que tenemos que actualizar
     * @param callback Callback al que llamar tras la actualización
     */
    @Override
    public void updateRound(Round round, BooleanCallback callback) {
        // Creamos un contenedor con los datos de la partida
        ContentValues values = getContentValues(round);

        // Actualizamos la partida en la base de datos
        long id = db.update(RoundTable.NAME,
                            values,
                            RoundTable.Cols.ROUNDUUID + " = ?",
                            new String[]{round.getId()});

        // Si nos pasan un callback, lo ejecutamos con la respuesta de si se ha realizado
        // de forma correcta
        if (callback != null)
            callback.onResponse(id >= 0);

    }

    /**
     * Consulta en la base de datos por todas las partidas disponibles y se las asigna al objeto
     * {@link RoundCursorWrapper} que devuelve para que quien la invoca maneje el resultado
     *
     * @return {@link RoundCursorWrapper} con el contenido de la consulta
     */
    private RoundCursorWrapper queryRounds() {
        // Query que devuelve todas las partidas disponibles
        String sql = "SELECT " +
                UserTable.Cols.PLAYERNAME + ", " +
                UserTable.Cols.PLAYERUUID + ", " +
                RoundTable.Cols.ROUNDUUID + ", " +
                RoundTable.Cols.DATE + ", " +
                RoundTable.Cols.TITLE + ", " +
                RoundTable.Cols.SIZE + ", " +
                RoundTable.Cols.BOARD + " " +
                "FROM " + UserTable.NAME + " AS p, " +
                RoundTable.NAME + " AS r " +
                "WHERE " + "p." + UserTable.Cols.PLAYERUUID + "=" +
                "r." + RoundTable.Cols.PLAYERUUID + ";";
        // Ejecutamos la consulta que nos devolverá la partida
        Cursor cursor = db.rawQuery(sql, null);

        // Creamos un RoundCursorWrapper con la respuesta a la consulta
        return new RoundCursorWrapper(cursor);
    }

    /**
     * Busca en la base de datos todas las partidas disponibles para el usuario playeruuid y enviamos
     * esa lista al callback en caso de una consulta exitosa y un error en caso de fallo
     *
     * @param playeruuid Identificador de usuario
     * @param orderByField Orden en el que se devolverán las partidas
     * @param group No voy a mentir, aún no sé para que es esto
     * @param callback Callback a ejecutar al que se le notificará cómo funcionó la función
     */
    @Override
    public void getRounds(String playeruuid, String orderByField, String group,
                          RoundsCallback callback) {
        // Creamos una lista de rondas y obtenemos las partidas siponibles
        List<Round> rounds = new ArrayList<>();
        RoundCursorWrapper cursor = queryRounds();
        cursor.moveToFirst();
        // Mientras queden partidas por revisar, iteramos
        while (!cursor.isAfterLast()) {
            // Obtenemos la ronda correspondiente
            Round round = cursor.getRound();
            // Si nosotros jugamos la partida, la añadimos a la lista
            if (round.getPlayerUUID().equals(playeruuid))
                rounds.add(round);
            // Movemos al siguiente
            cursor.moveToNext();
        }
        // Cerramos la respuesta
        cursor.close();
        // Si hay partidas disponibles, le enviamos las rondas correspondientes
        if (cursor.getCount() > 0)
            callback.onResponse(rounds);
        // Si no hay rondas o error, enviamos un error
        else
            callback.onError(Resources.getSystem().getString(R.string.noroundindatabase));
    }
}