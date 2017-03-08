package es.uam.eps.dadm.core;

import es.uam.eps.multij.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Tablero describe el tablero de las damas y evalúa los movimientos que se pueden o no hacer, las jugadas,
 * las fichas y todo lo relativo al teclado
 * @author Pablo Manso
 * @version 12/02/2017
 */
public class TableroDamas extends Tablero {
	
	/**
	 * Esta constante nos indica si está siendo compilado con ECLIPSE o no
	 * para activar o desactivar el esquema de colores
	 */
	public final static boolean ECLIPSE = true;

    /**
     * Tamaño que tiene el tablero
     */
    public final static int TABLEROSIZE = 8;

    /**
     * Doble array que contendrá toda la info del tablero
     */
    Casilla[][] casillas;

    /**
     * Registra una lista con los movimientos validos en cada turno
     */
    ArrayList<Movimiento> movimientos = null;

    /**
     * Construye el tablero para jugar a las damas
     */
    public TableroDamas() {
        super();
        this.casillas = new Casilla[TABLEROSIZE][TABLEROSIZE];
        this.limpiaTablero();
        this.numJugadas=0;
        this.numJugadores=2;
        this.estado=EN_CURSO;
    }

    /**
     * Crea un tablero vacío
     */
    public void limpiaTablero(){
        // Limpia el tablero poniendo todo a cero
        for(int i=0; i<this.casillas.length; i++)
            for(int j=0; j<this.casillas[i].length; j++)
                this.casillas[i][j] = new Casilla(i,j);
    }

    /**
     * Coloca en el tablero las fichas en la posición original
     */
    public void colocaFichas(){
        // Colocamos las fichas del primer jugador en la parte superior del tablero
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < TABLEROSIZE; j++)
                if ((i + j) % 2 == 1)
                    this.casillas[i][j].ponFicha(new Ficha(Ficha.Color.BLANCA));


        // Colocamos las fichas del segundo jugador en la parte inferior del tablero
        for (int i = TABLEROSIZE - 3; i < TABLEROSIZE; i++)
            for (int j = 0; j < TABLEROSIZE; j++)
                if ((i + j) % 2 == 1)
                    this.casillas[i][j].ponFicha(new Ficha(Ficha.Color.NEGRA));

        // Actualizamos los vmovimientos válidos
        this.movimientos = this.movimientosValidos();
    }

    /**
     * Ejecuta un movimiento en el tablero
     * @param m Movimiento a realizar
     * @throws ExcepcionJuego Se lanzará esta excepción en caso de no poder ejecutar el movmiento
     */
    @Override
    protected void mueve(Movimiento m) throws ExcepcionJuego {
        if (!this.esValido(m))
            throw new ExcepcionJuego("El movimiento indicado no es un movimiento válido.");

        this.ejecutaMovimiento(m);
        this.cambiaTurno();
        this.movimientos = this.movimientosValidos();
    }

    /**
     * Hace los cambios en tablero de un movmiento definido.
     * Se presupone eque el movimiento que se le pasa a la función ha debido pasar por la función es válido
     * @param m Movimiento a realizar
     */
    private void ejecutaMovimiento(Movimiento m){
        Casilla origen  = ((MovimientoDamas) m).getOrigen();
        Casilla destino = ((MovimientoDamas) m).getDestino();

        // Si el movmiento tiene distancia 1, es un movimiento simple, y simplemente movemos la ficha
        if (((MovimientoDamas) m).distancia() == 1)
            this.casillas[destino.row()][destino.col()].ponFicha(this.casillas[origen.row()][origen.col()].quitaFicha());

        // Si el movimiento tiene distancia , implica que hemos comido una ficha
        // Movemos nuestra ficha al destino y nos comemos la ficha que hay en medio
        else {
            this.getCasilla(destino.row(),destino.col()).ponFicha(this.getCasilla(origen.row(),origen.col()).quitaFicha());
            ((MovimientoDamas) m).casillaAnterior(this).quitaFicha();
            //this.casillas[(destino.row() + origen.row())/2][(destino.col() + origen.col())/2].quitaFicha();
        }

        // Si la ficha llega al final de donde debe llegar, lo convertimos en reina
        if (((this.getCasilla(destino.row(), destino.col()).getFicha().color == Ficha.Color.BLANCA) && (destino.row() == 7)) ||
                ((this.getCasilla(destino.row(), destino.col()).getFicha().color == Ficha.Color.NEGRA) && (destino.row() == 0)))
            this.getCasilla(destino.row(), destino.col()).getFicha().reina();

        // Si el movmiento es encadenado, ejecutamos el siguiente movimiento
        if (((MovimientoDamas) m).issetProximoMovimiento())
            this.ejecutaMovimiento(((MovimientoDamas) m).getProximoMovimiento());

    }

    /**
     * Comprueba si un movimiento es válido mirando si está en la lista de movimientos
     * @param m Movimiento a comprobar si es válido o no
     * @return Nos indica la validez o no de un movimiento
     */
    @Override
    public boolean esValido(Movimiento m) { return this.movimientos.indexOf(m) != -1;  }

    /**
     * Genera una lista de movimientos válidos para el turno en el que se invoca la función
     * @return Lista de movimientos válidos
     */
    @Override
    public ArrayList<Movimiento> movimientosValidos() {
        ArrayList<ArrayList> movimientos = new ArrayList<ArrayList>();
        Ficha.Color color = this.getTurno() == 0 ? Ficha.Color.BLANCA : Ficha.Color.NEGRA ;

        // Recorremos todas las casillas para ver cuales de las fichas pertenecen al turno
        for(int i=0 ; i<this.casillas.length ; i++)
            for(int j=0 ; j<this.casillas[i].length ; j++)
                if (this.getCasilla(i,j).tieneFicha() && this.getCasilla(i,j).getFicha().color == color){
                    // Cogemos los movimientos de las fichas avanzando
                    movimientos.add(this.movimientosValidosCasilla(this.getCasilla(i,j).clone(), this.getSentidoDelJuego(), true));

                    // Añadimos también, si es una reina, los movimientos de volver
                    if (this.getCasilla(i,j).getFicha().getTipo() == Ficha.Tipo.REINA)
                        movimientos.add(this.movimientosValidosCasilla(this.getCasilla(i,j).clone(), -this.getSentidoDelJuego(),true));

                }

        // Cargamos todos los movimientos en un solo array para devolverlos
        ArrayList<Movimiento> devolverMovimientos = new ArrayList<Movimiento>();
        for (ArrayList<Movimiento> armov:  movimientos)
            for (Movimiento m : armov)
                devolverMovimientos.add(m);

        return devolverMovimientos;
    }

    /**
     * Coge los movimientos válidos de una casilla
     * @param casilla Casilla desde la que mirar
     */
    public ArrayList<Movimiento> movimientosValidosCasilla(Casilla casilla, int sentido, boolean sencillo) {
        ArrayList<Movimiento> movimientos = new ArrayList<Movimiento>();

        // Si es un primer movimiento, puede ser un movimiento sencillo, pero si ya hemos comido, solo podemos volver a comer
        if (sencillo) {
        	// Movimientos sencillos, moviéndonos un cuadro delante o detrás
            for (Movimiento m : this.movimientosValidosCasillaDireccion(casilla, sentido, 1)) movimientos.add(m);
            for (Movimiento m : this.movimientosValidosCasillaDireccion(casilla, sentido, -1)) movimientos.add(m);
        }
        // Movimientos de distancia dos, lo que implica comer la ficha del enemigo que esté en medio
        for (Movimiento m : this.movimientosValidosCasillaDireccion(casilla,2*sentido,2)) movimientos.add(m);
        for (Movimiento m : this.movimientosValidosCasillaDireccion(casilla,2*sentido,-2)) movimientos.add(m);

        // Si alguno de los movimientos es válido, lo devolvemos
        return movimientos;
    }

    /**
     * Nos devuelve los movimientos válidos que hay con una dirección inicial dada
     * @param origen Casilla de origen del movimiento
     * @param sentido Si el movimiento es ascenten o descendente
     * @param salto La distancia a la que se realizará el salto
     * @return Movimientos válidos
     */
    public ArrayList<Movimiento> movimientosValidosCasillaDireccion(Casilla origen, int sentido, int salto){
        ArrayList<Movimiento> movimientos = new ArrayList<Movimiento>();
        ArrayList<Movimiento> movimientosaux;

        // Si la casilla destino no está en el tablero, no hay movimiento válido
        if ((new Casilla(origen, sentido, salto)).enTablero()) {
            Casilla destino = this.getCasilla(new Casilla(origen, sentido, salto)).clone();
            // Comprobamos si el movimiento directo es válido
            if (this.comrpuebaMovimiento(new MovimientoDamas(origen, destino))) {
            	// Lo añadimos a devolver
                movimientos.add(new MovimientoDamas(origen, destino));
                
                // Si hemos comido una ficha, vemos si podemos hacer un nuevo movimiento
                if (Math.abs(salto) > 1) {
                    destino.ponFicha(origen.getFicha());
                    // Obtenemos movimientos iterados
                    movimientosaux = this.movimientosValidosCasilla(destino, sentido / Math.abs(sentido), false);

                    // Encadenamos los movimientos
                    for (Movimiento mov : movimientosaux) {
                        MovimientoDamas aux = new MovimientoDamas(origen, destino);
                        aux.setProximoMovimiento((MovimientoDamas) mov);
                        movimientos.add(aux);
                    }
                }
            }
        }
        
        // Devolvemos los movimientos
        return movimientos;
    }

    /**
     * Nos indica si un movimiento es bueno para este turno. A diferencia de {@link TableroDamas#esValido(Movimiento)}
     * este es de uso interno y para añadir movimientos a la lista de movimientos válidos. Sin embargo {@link TableroDamas#esValido(Movimiento)}
     * simplemente nos devuelve si el movimiento está entre los válidos.
     * @param m Movimiento a comprobar
     * @return Si el movimiento es bueno o no
     */
    private boolean comrpuebaMovimiento(MovimientoDamas m){
        Casilla origen  = ((MovimientoDamas) m).getOrigen();
        Casilla destino = ((MovimientoDamas) m).getDestino();

        // Comprobamos que el destino sea una casilla del tablero
        if (!destino.enTablero()) return false;

        // Si el movmiento tiene distancia 1, es un movimiento simple, y simplemente comprobamos si el destino está vacío
        if (m.distancia() == 1)
            return (!destino.tieneFicha());

        // Si el movimiento es de distancia 2
        if (m.distancia() > 1){
            // Si no hay una ficha en medio no podemos mover
            Casilla media = this.getCasilla(m.casillaAnterior(this));
            if (!media.tieneFicha()) return false;

            // Si la fecha de en medio es un enemigo y donde queremos ir está libre, adelante
            return (origen.getFicha().color != media.getFicha().color) && !destino.tieneFicha();
        }
        return false;
    }

    /**
     * Exporta una partida para que se pueda jugar en otro momento. Ésta será cargada por
     * el método {@link TableroDamas#stringToTablero(String)}
     * @return String que contiene la partida
     */
    @Override
    public String tableroToString() {
        String ret = "" + this.getNumJugadas() + "/";
        // Para cada celda del tablero
        for(int i=0;i<TABLEROSIZE;i++)
            for (int j = 0; j < TABLEROSIZE; j++)
            // Si no tiene fichas no conviene exportarlo
                if (this.casillas[i][j].tieneFicha()){
                    // Añadimos el color a la salida
                    if (this.casillas[i][j].getFicha().color == Ficha.Color.BLANCA) ret += "0";
                    if (this.casillas[i][j].getFicha().color == Ficha.Color.NEGRA)  ret += "1";

                    // Añadimos el tipo de ficha que es
                    if (this.casillas[i][j].getFicha().getTipo() == Ficha.Tipo.DAMA)  ret += "0";
                    if (this.casillas[i][j].getFicha().getTipo() == Ficha.Tipo.REINA) ret += "1";

                    // Añadimos la posición de la ficha
                    ret += i + "" + j;
                }

        return ret;
    }

    /**
     * Carga una partida anterior para continuar jugándola
     * @param cadena Descripción de la partida generada por el método {@link TableroDamas#tableroToString()}
     * @throws ExcepcionJuego Se lanzará esta excepción si el tablero pasado no cumple con lo establecido
     */
    @Override
    public void stringToTablero(String cadena) throws ExcepcionJuego {
        // Partimos la cadena por el separador de los parámetros
        StringTokenizer stok = new StringTokenizer(cadena, "/");

        // Si no hay exáctamente dos parámetros hay algo mal
        if (stok.countTokens() != 2)
            throw new ExcepcionJuego("String no válido para un TableroDamas");

        // Copiamos los dos parámetros
        int jugadas = Integer.parseInt(stok.nextToken());
        String tablero = stok.nextToken();

        // Creamos un tablero vacío
        Casilla[][] casillas = new Casilla[TABLEROSIZE][TABLEROSIZE];
        for (int i = 0; i < TABLEROSIZE; i++)
            for (int j = 0; j < TABLEROSIZE; j++)
                casillas[i][j] = new Casilla(i,j);

        // Cada ficha ocupa cuatro caracteres, si no es múltiplo de 4 es que hay un error
        if ((tablero.length() % 4) != 0)
            throw new ExcepcionJuego("String no válido para un TableroDamas");


        for (int i = 0; i < tablero.length(); i+=4) {
            // Por cada 4, cogemos los parámetros y comprobamos que estén entre los valores adecuados
            int color = Character.getNumericValue(tablero.charAt(i));
            int tipo = Character.getNumericValue(tablero.charAt(i+1));
            int fila = Character.getNumericValue(tablero.charAt(i+2));
            int columna = Character.getNumericValue(tablero.charAt(i+3));
            if (color != 0 && color != 1) throw new ExcepcionJuego("String no válido para un TableroDamas");
            if (tipo != 0 && tipo != 1)   throw new ExcepcionJuego("String no válido para un TableroDamas");
            if (fila < 0 || fila >= TABLEROSIZE)       throw new ExcepcionJuego("String no válido para un TableroDamas");
            if (columna < 0 || columna >= TABLEROSIZE) throw new ExcepcionJuego("String no válido para un TableroDamas");

            // Creamos la ficha y la ponemos en el tablero
            Ficha.Color colorFicha = (color == 0) ? Ficha.Color.BLANCA : Ficha.Color.NEGRA;
            casillas[fila][columna].ponFicha(new Ficha(colorFicha));
            if (tipo == 1) casillas[fila][columna].getFicha().reina();
        }

        // Ajustamos los parámetros de partida y cargamos el tablero
        this.numJugadas = jugadas;
        if ((this.numJugadas % 2) == 1) this.turno = 1; else this.turno = 0;
        this.casillas = casillas;
        System.out.println(this.toString());
        this.movimientos = this.movimientosValidos();
    }

    /**
     * Convierte el tablero en algo que un humano pueda entender para poder jugar la partida de forma gráfica
     * @return String con la partida a pintar
     */
    @Override
    public String toString() {
        if(this.ECLIPSE)
        	return toStringPlain();
        return toStringColor();
    }

    /**
     * Imprime el tablero de forma plana, sin colores
     * @return String con la partida a pintar
     */
    public String toStringPlain() {
        String tablero = "";

        // Colocamos el nombre de las columnas
        tablero += "  |1 |2 |3 |4 |5 |6 |7 |8 |\n";
        // Por cada fila
        for(int i=0; i<this.casillas.length; i++) {
            // Pintamos el nombre de la fila
        	tablero += "  -------------------------\n";
            tablero += Character.toString((char) (i+'A')) + " ";
            // Imprimimos cada una de las casillas
            for(int j=0; j<this.casillas[i].length; j++)
                tablero += "|" + this.casillas[i][j].string();
            tablero += "|\n";
        }
    	tablero += "  -------------------------\n";
        return tablero + "\n";
    }

    /**
     * Imprime el tablero con códigos de colores
     * @return String con la partida a pintar
     */
    public String toStringColor() {
        String tablero = "";

        // Colocamos el nombre de las columnas
        tablero += " -12345678-\n";
        // Por cada fila
        for(int i=0; i<this.casillas.length; i++) {
            // Pintamos el nombre de la fila
            tablero += Character.toString((char) (i+'A')) + " ";
            // Imprimimos cada una de las casillas
            for(int j=0; j<this.casillas[i].length; j++)
                tablero += this.casillas[i][j].string();
            tablero += "\n";
        }
        return tablero + "\n";
    }

    /**
     * Limpia el tablero de la partida actual y coloca las fichas para una partida nueva
     * @return true
     */
    @Override
    public boolean reset(){
        this.numJugadas = 0;
        this.limpiaTablero();
        this.colocaFichas();
        return true;
    }

    /**
     * Devuelve el contenido de una casilla del tablero
     * @param x Posición x del tablero
     * @param y Posición y del tablero
     * @return Contenido de la casilla
     */
    public Casilla getCasilla(int x, int y) { return this.casillas[x][y]; }

    /**
     * Devuelve el contenido de una casilla del tablero
     * @param casilla Casilla de la que mirar el contenido
     * @return Contenido de la casilla
     */
    public Casilla getCasilla(Casilla casilla) { return this.getCasilla(casilla.row(), casilla.col());
    }

    /**
     * Nos indica el sentido del juego 1 si el jugador juega de arriba a abajo, -1 si el jugador juega de abajo a arriba
     * @return Sentido del juego
     */
    public int getSentidoDelJuego(){ return (this.getTurno() == 0) ? 1 : -1; }
}
