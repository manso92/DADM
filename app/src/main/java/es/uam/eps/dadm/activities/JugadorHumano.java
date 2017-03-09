package es.uam.eps.dadm.activities;


import android.support.design.widget.Snackbar;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Casilla;
import es.uam.eps.dadm.model.MovimientoDamas;
import es.uam.eps.dadm.model.TableroDamas;
import es.uam.eps.multij.AccionMover;
import es.uam.eps.multij.Evento;
import es.uam.eps.multij.Jugador;
import es.uam.eps.multij.Partida;
import es.uam.eps.multij.Tablero;

/**
 * Jugador que juega según las entradas que el usuario vaya haciendo por teclado
 * @author Pablo Manso
 * @version 12/02/2017
 */
public class JugadorHumano implements Jugador, View.OnClickListener {

    private final int ids[][] = {
            {R.id.er1, R.id.er2, R.id.er3},
            {R.id.er4, R.id.er5, R.id.er6},
            {R.id.er7, R.id.er8, R.id.er9}};
    private int SIZE = 3;
    Partida game;

    /**
     * Nombre que identifica al jugador
     */
    private String nombre = "Local player";

    /**
     * Construye un jugador humano con el nombre por defecto
     */
    public JugadorHumano() {
    }

    /**
     * Construye un jugador humano
     * @param string Nombre del jugador
     */
    public JugadorHumano(String string) {
        this.nombre=string;
    }

    /**
     * Gestiona las respuestas del jugador a determinados eventos del juego
     * @param evento Evento hasta el que tenemos que generar una respuesta
     */
    @Override
    public void onCambioEnPartida(Evento evento) {
        // Haremos distintas acciones dependiendo de lo que se nos pida
        switch (evento.getTipo()) {
            // Si hay un cambio en el juego imprimimos el tablero para que se vea el cambio
            case Evento.EVENTO_CAMBIO:
                System.out.print(evento.getPartida().getTablero().toString());
                break;

            // Si nos llega un evento que confirmar, preguntamos al usuario y enviamos una confirmación positiva
            // o negativa dependiendo del usuario
            case Evento.EVENTO_CONFIRMA:
                System.out.print("¿Estás a favor o en contra (s/n)?;");
                try {
                    evento.getPartida().confirmaAccion(this, evento.getCausa(), this.askConfirmacion());
                } catch (Exception e) {}
                break;

            // Si hay este evento, es que nos toca jugar
            case Evento.EVENTO_TURNO:

                try {
                    // Preguntamos al usuario por el orígen y el final de su movimiento de ficha
                    System.out.print("Introduce la casilla que contiene la ficha a mover: ");
                    Casilla origen = this.askCasilla();
                    System.out.print("Introduce la casilla a la que se moverá la ficha: ");
                    Casilla destino = this.askCasilla();

                    // Creamos un movimiento para ver por donde se moverá la ficha
                    MovimientoDamas movimiento = new MovimientoDamas(origen, destino);
                    MovimientoDamas movimiento2 = movimiento;

                    // Por si es uno de estos movimientos múltiples, preguntamos al usuario
                    while (true){
                        // Preguntamos si va a hacer algún movimiento extra
                        System.out.print("¿Hay que mover la ficha a otra casilla después de " +
                                Character.toString((char) (movimiento2.getDestino().row() + 'A')) +
                                Integer.toString(movimiento2.getDestino().col()+1) + "? (s/n) :");
                        boolean pregunta = this.askConfirmacion();
                        if (pregunta){
                            // Preguntamos por la nueva casilla de destino, encadenamos el movimiento al que nos acaban de decir y
                            // así hasta que diga que ninguno más
                            System.out.print("Introduce la casilla a la que se moverá la ficha: ");
                            destino = this.askCasilla();
                            movimiento2 = movimiento2.setProximoMovimiento(new MovimientoDamas(movimiento2.getDestino(), destino));
                        } else
                            break;
                    }

                    // Decimos a la partida que queremos hacer el movmiento
                    evento.getPartida().realizaAccion(new AccionMover(this, movimiento));
                }
                catch(Exception e) {
                    // Si no es posible ejecutar el movimiento, pues volvemos a preguntar
                    System.out.print(e.getMessage()+ "\n");
                    this.onCambioEnPartida(evento);
                }
                break;
        }

    }

    /**
     * Pregunta al usuario mediante del teclado por una posición del tablero.
     * @return Casilla elegida por el usuario
     */
    private Casilla askCasilla() throws IOException {
        // Miramos que nos dice el usuario y lo pasamos a mayúsculas, por evitar comprobaciones
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String posicion = br.readLine().toUpperCase();

        // Si la cadena tiene más o menos de dos caracteres es que algo han introducido mal
        if (posicion.length() != 2) {
            System.out.print("La casilla debe tener solo dos caracteres, fila y columna (ej: A6): ");
            return this.askCasilla();
        }

        // Si lo primero no es una letra entre las permitidas comunicamos el error
        if ((posicion.charAt(0) < 'A') || (posicion.charAt(0) > 'A' + TableroDamas.TABLEROSIZE)){
            System.out.print("La fila debe ser una letra mayúscula entre A y " +
                    Character.toString((char) ('A' + TableroDamas.TABLEROSIZE-1)) + " (ej: A6): ");
            return this.askCasilla();
        }

        // Si lo segundo no es un número permitido pues otro error que mandaremos al usuario
        if ((Character.getNumericValue(posicion.charAt(1)) <= 0) ||
            (Character.getNumericValue(posicion.charAt(1)) > TableroDamas.TABLEROSIZE)){
            System.out.print("La columna debe ser un número entre 1 y " + TableroDamas.TABLEROSIZE + " (ej: A6): ");
            return this.askCasilla();
        }

        // Devolvemos una Casilla con los datos que nos ha comunicado el usuario
        return new Casilla(posicion.charAt(0) - 'A', posicion.charAt(1) - '1');
    }

    /**
     * Pregunta al usuario mediante el teclado una pregunta de si o no (s/n)
     * @return true o false, lo que el usuario nos diga
     */
    private boolean askConfirmacion() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String posicion = br.readLine().toLowerCase();

        // Si la respuesta tiene más de un carácter, es que ha escrito más de lo que debía
        if (posicion.length() != 1) {
            System.out.print("Por favor, instroduce solo una de las letras permitidas (s/n): ");
            return this.askConfirmacion();
        }

        // Si la respuesta no es un simple si o no, está mal
        if ((posicion.charAt(0) != 's') && (posicion.charAt(0) != 'n')){
            System.out.print("Por favor, instroduce solo una de las letras permitidas (s/n): ");
            return this.askConfirmacion();
        }

        // Devolvemos un true o false dependiendo de la respuesta
        return (posicion.charAt(0) == 's');
    }























    private int fromViewToI(View view) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (view.getId() == ids[i][j])
                    return i;
        return -1;
    }
    private int fromViewToJ(View view) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (view.getId() == ids[i][j])
                    return j;
        return -1;
    }
    @Override
    public void onClick(View v) {
        try {
            // TODO cuando se clickee por primera vez se guarda la casilla de inicio y se marca que se va a mover
            // Cuando se clickee por segunda se ejecuta el movimiento
            if (game.getTablero().getEstado() != Tablero.EN_CURSO) {
                Snackbar.make(v, R.string.round_already_finished,
                                 Snackbar.LENGTH_SHORT).show();
                return;
            }

            // TODO cambiar el movimiento para que coja el inicio y el fin sobre todo hay que mirar porque esto solo recorre un
            MovimientoDamas m = new MovimientoDamas(new Casilla(fromViewToI(v), fromViewToJ(v)),new Casilla(fromViewToI(v), fromViewToJ(v)));
            game.realizaAccion(new AccionMover(this, m));
        } catch (Exception e) {
            Snackbar.make(v, R.string.invalid_movement, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Devuelve 'true' si este jugador sabe jugar al juego indicado
     * @param tablero Tablero de la partida
     * @return true o false, dependiendo de si puede jugar o no
     */
    @Override
    public boolean puedeJugar(Tablero tablero) {
        return tablero instanceof TableroDamas;
    }


    /**
     * Asigna al jugador una partida
     * @param game Parida que va a jugar el jugador
     */
    public void setPartida(Partida game) {
        this.game = game;
    }

    /**
     * Nombre del jugador
     * @return string con el nombre del jugador
     */
    @Override
    public String getNombre() {
        return this.nombre;
    }
}
