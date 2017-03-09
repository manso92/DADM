package es.uam.eps.dadm.model;

/**
 * Ficha de las damas, que puede ser una dama o una reina
 * @author Pablo Manso
 * @version 11/02/2017
 */
public class Ficha {

    /**
     * Tipos de fichas
     */
    public enum Tipo { DAMA, REINA }

    /**
     * Color de la ficha
     */
    public enum Color { BLANCA, NEGRA}

    /**
     * Tipo de la ficha
     */
    private Tipo tipo;

    /**
     * Color de la ficha
     */
    public Color color;

    /**
     * Crea una nueva ficha en el tablero
     * @param color Color de la ficha con el que será pintado en el tablero
     */
    public Ficha (Color color){
        this.tipo = Tipo.DAMA;
        this.color = color;
    }

    /**
     * Devuelve el Tipo de la ficha, Reina o Dama
     * @return tipo de la ficha
     */
    public Tipo getTipo() { return tipo; }

    /**
     * Cambia el tipo de una ficha de Dama a Reina
     */
    public void reina() { this.tipo = Tipo.REINA; }

    /**
     * Escapa un string para que se pinte la ficha con el color que le corresponde
     * @return Cadena que representa la ficha
     */
    public String string() {
        return (char)27 + "[1;" + (this.color == Color.BLANCA ? "30" : "31") + "m" + (this.tipo == Tipo.REINA ? "R" : "D");
    }

    /**
     * Representación de la ficha
     * @return Cadena que representa la ficha
     */
    @Override
    public String toString() {
        return (this.tipo == Tipo.REINA ? (this.color == Color.BLANCA ? "O" : "X") : (this.color == Color.BLANCA ? "o" : "x"));
    }

    /**
     * Representación de la ficha
     * @return Cadena que representa la ficha
     */
    @Override
    public boolean equals(Object o) {
        return ((this.getTipo() == ((Ficha)o).getTipo()) && (this.color == ((Ficha)o).color));
    }

    /**
     * Clona el objeto para que no haya errores de modificaciones de punteros
     * @return Objeto clonado
     */
    @Override
    public Ficha clone(){
        Ficha f = new Ficha(this.color);
        if (this.getTipo() == Tipo.REINA) f.reina();
        return f;
    }
}
