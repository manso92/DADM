package com.manso92.damas;


import com.manso92.damas.core.*;
import es.uam.eps.multij.*;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws ExcepcionJuego, IOException {
        // Cargamos los jugadores
        ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
        jugadores.add(new JugadorAleatorio("Máquina"));
        jugadores.add(new JugadorHumano("Humano"));

        // Creamos el tablero y repartimos las fichas
        Tablero tablero = new TableroDamas();
        ((TableroDamas)tablero).colocaFichas();

        // Creamos la partida con jugadores y tablero, y a empezar
        Partida partida = new Partida(tablero, jugadores);
        //partida.addObservador(new JugadorHumano(""));
        partida.comenzar();
    }
}
