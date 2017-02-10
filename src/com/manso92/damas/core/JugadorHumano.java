package com.manso92.damas.core;

import es.uam.eps.multij.*;
import java.util.Scanner;

/**
 * Jugador que juega según las entradas que el usuario vaya haciendo por teclado
 * @author Pablo Manso
 * @version 10/02/2017
 */
public class JugadorHumano implements Jugador {

    /**
     * Nombre que identifica al jugador
     */
    private String nombre;

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
                break;

            // Si nos llega un evento que confirmar, preguntamos al usuario y enviamos una confirmación positiva
            // o negativa dependiendo del usuario
            case Evento.EVENTO_CONFIRMA:
                System.out.println("¿Estás a favor o en contra (s/n)?;");
                try {
                    if(this.askConfirmacion())
                        evento.getPartida().confirmaAccion(
                                this, evento.getCausa(), true);
                    else
                        evento.getPartida().confirmaAccion(
                                this, evento.getCausa(), false);

                }
                catch(Exception e) {}
                break;

            // Si hay este evento, es que nos toca jugar
            case Evento.EVENTO_TURNO:

                // Preguntamos al usuario por el orígen y el final de su movimiento de ficha
                System.out.print("Introduce la casilla que contiene la ficha a mover:");
                Casilla origen = this.askCasilla();
                System.out.print("\nIntroduce la casilla a la que se moverá la ficha:");
                Casilla destino = this.askCasilla();

                // Creamos un movimiento para ver por donde se moverá la ficha
                MovimientoDamas movimiento = new MovimientoDamas(origen, destino);
                MovimientoDamas movimiento2 = movimiento;

                // Por si es uno de estos movimientos múltiples, preguntamos al usuario
                while (true){
                    // Preguntamos si va a hacer algún movimiento extra
                    System.out.println("Hay que mover la ficha a otra casilla después de " + movimiento2.getDestino().toString());
                    boolean pregunta = this.askConfirmacion();
                    if (pregunta){
                        // Preguntamos por la nueva casilla de destino, encadenamos el movimiento al que nos acaban de decir y
                        // así hasta que diga que ninguno más
                        destino = this.askCasilla();
                        movimiento2 = movimiento2.setProximoMovimiento(new MovimientoDamas(movimiento2.getDestino(), destino));
                    } else
                        break;
                }

                try {
                    // Decimos a la partida que queremos hacer el movmiento
                    evento.getPartida().realizaAccion(new AccionMover(
                            this, movimiento));
                }
                catch(Exception e) {
                    // Si no es posible ejecutar el movimiento, pues volvemos a preguntar
                    System.out.println(e.getMessage());
                    this.onCambioEnPartida(evento);
                }
                break;
        }

    }

    /**
     * Pregunta al usuario mediante del teclado por una posición del tablero.
     * @return Casilla elegida por el usuario
     */
    private Casilla askCasilla() {
        // Miramos que nos dice el usuario y lo pasamos a mayúsculas, por evitar comprobaciones
        Scanner in = new Scanner ( System.in );
        String posicion = in.next().toUpperCase();
        in.close();

        // Si la cadena tiene más o menos de dos caracteres es que algo han introducido mal
        if (posicion.length() != 2) {
            System.out.println("La casilla debe tener solo dos caracteres, fila y columna (ej: A6).\n");
            return this.askCasilla();
        }

        // Si lo primero no es una letra entre las permitidas comunicamos el error
        if ((posicion.charAt(0) < 'A') || (posicion.charAt(0) > 'A' + TableroDamas.TABLEROSIZE)){
            System.out.println("La fila debe ser una letra mayúscula entre A y " +
                    Character.toString((char) ('A' + TableroDamas.TABLEROSIZE-1)) + " (ej: A6).\n");
            return this.askCasilla();
        }

        // Si lo segundo no es un número permitido pues otro error que mandaremos al usuario
        if ((Character.getNumericValue(posicion.charAt(1)) <= 0) ||
            (Character.getNumericValue(posicion.charAt(1)) > TableroDamas.TABLEROSIZE)){
            System.out.println("La columna debe ser un número entre 1 y " + TableroDamas.TABLEROSIZE + " (ej: A6).\n");
            return this.askCasilla();
        }

        // Devolvemos una Casilla con los datos que nos ha comunicado el usuario
        return new Casilla(posicion.charAt(0) - 'A', posicion.charAt(1) - '1');
    }

    /**
     * Pregunta al usuario mediante el teclado una pregunta de si o no (s/n)
     * @return true o false, lo que el usuario nos diga
     */
    private boolean askConfirmacion() {
        // Miramos que nos dice el usuario y lo pasamos a minúsuculas, por evitar comprobaciones
        Scanner in = new Scanner ( System.in );
        String posicion = in.next().toLowerCase();
        in.close();

        // Si la respuesta tiene más de un carácter, es que ha escrito más de lo que debía
        if (posicion.length() != 1) {
            System.out.println("Por favor, instroduce solo una de las letras permitidas: s o n.\n");
            return this.askConfirmacion();
        }

        // Si la respuesta no es un simple si o no, está mal
        if ((posicion.charAt(0) != 's') && (posicion.charAt(0) != 'n')){
            System.out.println("Por favor, instroduce solo una de las letras permitidas: s o n.\n");
            return this.askConfirmacion();
        }

        // Devolvemos un true o false dependiendo de la respuesta
        return (posicion.charAt(0) == 's');
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
     * Nombre del jugador
     * @return string con el nombre del jugador
     */
    @Override
    public String getNombre() {
        return this.nombre;
    }
}
