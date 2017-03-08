package es.uam.eps.dadm;


import es.uam.eps.dadm.core.*;
import es.uam.eps.multij.*;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
	
	/*
	 * El proyecto ha sido enteramente desarrollado mediante el IDE Jetbrains IntelliJ IDEA el cual me ha permitido imprimir el tablero en colores,
     * cosa que eclipse no me ha permitido. Es gratuito para el entorno educativo. Si se ejecuta en eclipse, no hay que hacer nada. En caso de ejecutarse en 
     * IntelliJ se puede cambiar la constante ECLIPSE de TableroDamas para que pase de imprimirse en texto plano a con colores.
     * 
     * El paquete para el programa es com.manso92 dado que ya tengo comprado ese dominio y una vez finalizada la aplicación quiero subirla a google play.
     * 
     * De todas las normas de las damas se han obviado dos:
     *  - La Reina puede moverse de delante y detrás, pero de uno en uno, no permitiendo saltos largos.
     *  - No es obligatorio comer, por lo que si puedes comer y no lo haces, no perderás la ficha.
	 */

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

        // Debug para crear un tablero
        //tablero.stringToTablero("37/000100030007001400210023003000341041004311451047105010541061106310671070107210741076");

        partida.comenzar();
    }
}
