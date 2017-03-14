package es.uam.eps.dadm.model;

import es.uam.eps.multij.*;

/**
 * Movimiento dentro del Juego de las damas
 * @author Pablo Manso
 * @version 10/02/2017
 */
public class MovimientoDamas extends Movimiento {

    /**
     * Casilla de origen de donde se cogerá la ficha
     */
    private Casilla origen;

    /**
     * Casilla de destino de la ficha que se ha cogido del origen
     */
    private Casilla destino;

    /**
     * En caso de ser un movimiento múltiple, este será el siguiente movimiento
     */
    private MovimientoDamas proximoMovimiento = null;

    /**
     * Movmiento simple dentro del Juego de las Damas, de un origen a un final
     * @param origen {@link es.uam.eps.dadm.model.Casilla} de orígen del movmiento
     * @param destino{@link es.uam.eps.dadm.model.Casilla} de destino del movmiento
     */
    public MovimientoDamas(Casilla origen, Casilla destino){
        this.origen = origen;
        this.destino = destino;
    }

    /**
     * Movmiento compuesto dentro del Juego de las Damas, de un origen a un final y un
     * @param origen {@link es.uam.eps.dadm.model.Casilla} de orígen del movmiento
     * @param destino {@link es.uam.eps.dadm.model.Casilla} de destino del movmiento
     * @param movimiento {@link es.uam.eps.dadm.model.MovimientoDamas} movimiento posterior a este, que debe tener como origen el destino de este
     */
    public MovimientoDamas(Casilla origen, Casilla destino, MovimientoDamas movimiento){
        this.origen = origen;
        this.destino = destino;
        this.proximoMovimiento = movimiento;
    }

    /**
     * Devuelve la casilla anterior al destino del movimiento, que presumiblemente será la que se coma la ficha
     * @param t Tablero del que coger la casilla
     * @return Casilla seleccionada
     */
    public Casilla casillaAnterior(TableroDamas t){
        int colFactor = (this.getOrigen().col() - this.getDestino().col()) / Math.abs((this.getOrigen().col() - this.getDestino().col()));
        int rowFactor = (this.getOrigen().row() - this.getDestino().row()) / Math.abs((this.getOrigen().row() - this.getDestino().row()));
        return t.getCasilla(this.getDestino().row() + rowFactor , this.getDestino().col() + colFactor);
    }

    /**
     * Comprueba si un movimiento es igual a otro pero a la inversa
     * @param m Movimiento a comprobar
     * @return Si los movimientos son simétricos o no
     */
    public boolean esSimétrico(MovimientoDamas m){
        return m.getOrigen().equals(this.getDestino()) && m.getDestino().equals(this.getOrigen());
    }

    /**
     * Distancia máxima entre dos casillas, de modo que si entre la casilla de origen y la de fin hay dos columnas y
     * tres filas, devolverá tres
     * @return Máxima distancia
     */
    public int distancia(){ return Math.max(Math.abs(this.destino.col() - this.origen.col()), Math.abs(this.destino.col() - this.origen.col()));}

    /**
     * Nos devuelve la {@link es.uam.eps.dadm.model.Casilla} en la que se origina el movimiento
     * @return {@link es.uam.eps.dadm.model.Casilla} de origen
     */
    public Casilla getOrigen() {
        return origen;
    }

    /**
     * Cambia la {@link es.uam.eps.dadm.model.Casilla} de origen del movimiento
     * @param origen {@link es.uam.eps.dadm.model.Casilla} en la que se origina el movimiento
     */
    public void setOrigen(Casilla origen) {
        this.origen = origen;
    }

    /**
     * Nos devuelve la {@link es.uam.eps.dadm.model.Casilla} en la que se finaliza el movimiento
     * @return {@link es.uam.eps.dadm.model.Casilla} de destino
     */
    public Casilla getDestino() {
        return destino;
    }

    /**
     * Cambia la {@link es.uam.eps.dadm.model.Casilla} de destino del movimiento
     * @param destino {@link es.uam.eps.dadm.model.Casilla} en la que finaliza el movimiento
     */
    public void setDestino(Casilla destino) {
        this.destino = destino;
    }

    /**
     * Nos devuelve el movimiento que se deberá realizar después de éste
     * @return {@link es.uam.eps.dadm.model.MovimientoDamas} a realizar
     */
    public MovimientoDamas getProximoMovimiento() {
        return proximoMovimiento;
    }

    /**
     * Nos indica si después de otro movimiento habrá que realizar otro
     * @return true o false, dependiendo de si hay que realizar más movimeintos o no
     */
    public boolean issetProximoMovimiento() {
        return this.getProximoMovimiento() != null;
    }

    /**
     * Cambia el {@link es.uam.eps.dadm.model.MovimientoDamas} que hay que realizar después de éste
     * @param proximoMovimiento {@link es.uam.eps.dadm.model.MovimientoDamas} a realizar después
     * @return {@link es.uam.eps.dadm.model.MovimientoDamas} a realizar después
     */
    public MovimientoDamas setProximoMovimiento(MovimientoDamas proximoMovimiento) {
        this.proximoMovimiento = proximoMovimiento;
        return this.proximoMovimiento;
    }

    /**
     * Convierte a una cadena de texto el movimiento
     * @return Cadena de texto con la descripción del movimiento
     */
    @Override
    public String toString() {
        String movimiento = "Mover de " + this.getOrigen().toString() + " a " + this.getDestino().toString() + ". ";
        if (this.issetProximoMovimiento())
            movimiento += this.getProximoMovimiento().toString();
        return movimiento;
    }

    /**
     * Compara si son dos movimientos iguales o no
     * @param o Movimiento a comparar
     * @return Si los dos movimientos son iguales o no
     */
    @Override
    public boolean equals(Object o) {
        // Si los orígenes son distintos, no son el mismo movimiento
        if (!this.getOrigen().equals(((MovimientoDamas)o).getOrigen()))
            return false;
        // Si los destinos son distintos, no son el mismo movimiento
        if (!this.getDestino().equals(((MovimientoDamas)o).getDestino()))
            return false;
        // Si el origen y el destino son iguales y es el final movimiento, si es el mismo movimiento
        if (!this.issetProximoMovimiento() && !((MovimientoDamas)o).issetProximoMovimiento())
            return true;
        // Si uno tiene próximo movimiento y el otro no, no son el mismo movimiento
        if (this.issetProximoMovimiento() ^ ((MovimientoDamas)o).issetProximoMovimiento())
            return false;
        // Si el próximo movmiento son igual para los dos, son el mismo movimiento, sino no
        if (this.getProximoMovimiento().equals(((MovimientoDamas)o).getProximoMovimiento()))
            return true;
        return false;
    }

    /**
     * Clona el objeto para que no haya errores de modificaciones de punteros
     * @return Objeto clonado
     */
    @Override
    public MovimientoDamas clone(){
        MovimientoDamas m = new MovimientoDamas(this.getOrigen().clone(), this.getDestino().clone());
        if (issetProximoMovimiento()) m.setProximoMovimiento(this.getProximoMovimiento().clone());
        return m;
    }
}
