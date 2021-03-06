package com.manso92.damas.view.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import com.manso92.damas.R;
import com.manso92.damas.events.ShowMsgEvent;
import com.manso92.damas.model.Casilla;
import com.manso92.damas.model.ColorManager;
import com.manso92.damas.model.Ficha;
import com.manso92.damas.model.MovimientoDamas;
import com.manso92.damas.model.TableroDamas;
import com.manso92.damas.view.activities.Jarvis;
import es.uam.eps.multij.Tablero;

/**
 * Esta clase es una view personalizada que pinta el tablero
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class TableroView extends View {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.TableroView";

    /**
     * Pincel que pintará el tablero de juego
     */
    private Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Alto del cuadro del tablero
     */
    private float heightOfTile;

    /**
     * Ancho del cuadro del tablero
     */
    private float widthOfTile;

    /**
     * Tablero de damas que representa el juego
     */
    private TableroDamas board;

    /**
     * Controlador al que enviaremos los movimientos que realiza el jugador
     */
    private OnPlayListener onPlayListener;

    /**
     * Movimiento que será introducido a través de las pulsaciones en el tablero
     */
    private MovimientoDamas movimiento = null;

    /**
     * Movimiento que será introducido a través de las pulsaciones en el tablero
     */
    private ArrayList<Casilla> movimientosSugeridos = null;

    /**
     * Turno en el que juega el jugador local en caso de ser una partida de servidor
     */
    private int turn = -1;

    /**
     * Interfaz que define como enviaremos los movimientos al controlador
     */
    public interface OnPlayListener {
        void onPlay(MovimientoDamas movimientoDamas);
    }

    /**
     * Constructor de la vista del tablero
     * @param context Contexto para dibujar
     * @param attrs Atributos de la vista
     */
    public TableroView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Registra el controlador de los eventos que sucederán en la pantalla
     * @param listener Controlador de los eventos
     */
    public void setOnPlayListener(OnPlayListener listener) {
        this.onPlayListener = listener;
    }

    /**
     * Guarda el tablero de la partida que vamos a pintar
     * @param board Tablero de la partida de las Damas
     */
    public void setBoard(TableroDamas board) {
        this.board = board;
    }

    /**
     * Guarda el tablero de la partida que vamos a pintar
     * @param board Tablero de la partida de las Damas
     * @param turn Turno en el que juega el jugador local
     */
    public void setBoard(TableroDamas board, int turn) {
        this.board = board;
        this.turn = turn;
    }

    /**
     * Configura las diensiones de la vista
     * @param widthMeasureSpec Propiedades del ancho
     * @param heightMeasureSpec Propiedades del alto
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;

        // Obtenemos el ancho y el alto que nos dan las propiedades
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // Ponemos el ancho y el alto al menor de los dos para que sea cuadrado
        width = height = widthSize < heightSize ? widthSize: heightSize;

        // Ajustamos las dimensiones a las que hemos configurado antes
        setMeasuredDimension(width, height);
    }

    /**
     * Actualizamos el tamaño cuando cambie el tamaño
     * @param w Ancho de la vista
     * @param h Alto de la vista
     * @param oldw Antiguo ancho de la vista
     * @param oldh Antiguo alto de la vista
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Actualizamos el alto y el ancho que tendrán los cuadrados del tablero
        widthOfTile = w / this.board.size;
        heightOfTile = h / this.board.size;

        // Enviamos esa información a la clase padre
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Dibuja todo lo que contendrá el view
     * @param canvas Canvas en el que dibujar
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Llamamos a la clase padre  y empezamos ap intar el tablero
        super.onDraw(canvas);
        pintaTablero(canvas, brush);
    }

    /**
     * Dibuja el tablero de las damas y las fichas que lo componen
     * @param canvas Canvas en el que dibujar
     * @param paint Pincel con las propiedades de lo que pintaremos
     */
    private void pintaTablero(Canvas canvas, Paint paint) {
        // Imprimimos cada uno de los cuadros del tablero
        for (int i = 0; i < this.board.size; i++)
            for (int j = 0; j < this.board.size; j++)
                pintaCuadro(canvas, paint, this.board.getCasilla(i,j));
    }

    /**
     * Cambiamos el pincel al color del cuadro que haya que pintar
     * @param canvas Canvas en el que dibujar
     * @param paint Pincel del que modificaremos
     * @param casilla Casilla que vamos a pintar
     */
    private void pintaCuadro(Canvas canvas, Paint paint, Casilla casilla) {
        // Comprueba si el cuadro es oscuro o claro y cambia el pincel en consecuencia
        if (casilla.getColor() == Casilla.Color.OSCURA)
            paint.setColor(this.getContext().getResources().getColor(ColorManager.casillaOscura(getContext())));
        if (casilla.getColor() == Casilla.Color.CLARA)
            paint.setColor(this.getContext().getResources().getColor(ColorManager.casillaClara(getContext())));

        // Si es una de las casillas sugeridas la ponemos de verde
        if ((this.movimientosSugeridos != null) && (this.movimientosSugeridos.size()>0) && (this.movimientosSugeridos.indexOf(casilla) != -1))
            paint.setColor(this.getContext().getResources().getColor(ColorManager.casillaSugerida(getContext())));

        // Dibujamos el cuadrado del tablero
        canvas.drawRect((casilla.col()*(int) widthOfTile),
                (casilla.row()*(int) heightOfTile),
                ((casilla.col() + 1)*(int) widthOfTile),
                ((casilla.row() + 1)*(int) heightOfTile),
                paint);

        // Si la casilla tiene una ficha, la pintamos también
        if (casilla.tieneFicha())
            pintaFicha(canvas, paint, casilla);
    }

    /**
     * Pinta la ficha encima de un recuadro
     * @param canvas Canvas en el que dibujar
     * @param casilla Casilla con la ficha que vamos a pintar
     */
    private void pintaFicha(Canvas canvas, Paint paint, Casilla casilla) {
        // Comprobamos el color de la ficha que tenemos que pintar
        if (casilla.getFicha().color == Ficha.Color.BLANCA)
            paint.setColor(this.getContext().getResources().getColor(ColorManager.fichaBlanca(getContext())));
        if (casilla.getFicha().color == Ficha.Color.NEGRA)
            paint.setColor(this.getContext().getResources().getColor(ColorManager.fichaNega(getContext())));

        // Dibujamos la ficha en el tablero
        canvas.drawCircle((casilla.col()*(int) widthOfTile) + (widthOfTile/2),
                (casilla.row()*(int) heightOfTile) + (widthOfTile/2) ,widthOfTile/2,
                paint);

        if (casilla.getFicha().getTipo() == Ficha.Tipo.REINA){
            // Comprueba si el cuadro es oscuro o claro y cambia el pincel en consecuencia
            if (casilla.getColor() == Casilla.Color.OSCURA)
                paint.setColor(this.getContext().getResources().getColor(ColorManager.casillaOscura(getContext())));
            if (casilla.getColor() == Casilla.Color.CLARA)
                paint.setColor(this.getContext().getResources().getColor(ColorManager.casillaClara(getContext())));

            // Dibujamos el circulo interior de las damas
            canvas.drawCircle((casilla.col()*(int) widthOfTile) + (widthOfTile/2),
                    (casilla.row()*(int) heightOfTile) + (widthOfTile/2) ,widthOfTile/3,
                    paint);
        }
    }

    /**
     * Nos devuelve la casilla en la que ha pulsado el usuario
     * @param event Evento que capturamos con el toque en la pantalla
     * @return Casilla que ha tocado el usuario
     */
    private Casilla fromEventCasilla(MotionEvent event) {
        // Calculamos la casilla en la que ha pulsado el usuario
        return new Casilla((int) (event.getY() / heightOfTile),(int) (event.getX() / widthOfTile));
    }

    /**
     * Capturamos los eventos en los que el usuario toca la pantalla
     * @param event Evento que registra las caracterísitcas sobre dónde y cómo ha tocado
     * @return estado
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Si no hay partida en curso no hay nada que hacer
        if (board.getEstado() != Tablero.EN_CURSO)
            return true;
        // Si el evento que se realiza es un toque, lo procesamos
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            this.seleccionaCasilla(fromEventCasilla(event));

        return true;
    }

    /**
     * Captura la información de la casilla seleccionada para procesarla en el juego
     * @param casilla Casilla que el jugador ha seleccionado
     */
    public void seleccionaCasilla(Casilla casilla){
        if (this.onPlayListener != null) {
            if ((this.turn != -1) && (this.turn != board.getTurno())){
                Jarvis.error(ShowMsgEvent.Type.TOAST, "It's not your turn");
                return;
            }
            if (this.movimiento == null) {
                // Creamos un movimiento y le asignamos la casilla de inicio
                this.movimiento = new MovimientoDamas();
                this.movimiento.setOrigen(casilla);

                // Si no hay un movimiento válido con esa primer casilla, se lo indicamos al usuario y limpiamos el movimiento
                if (this.board.mismoComienzo(this.movimiento).size() == 0) {
                    this.movimiento = null;
                    Jarvis.error(ShowMsgEvent.Type.TOAST, R.string.game_move_invalid, getContext());
                }
            } else {
                // Añadimos el nuevo destino
                this.movimiento.addDestino(casilla);

                // Si no existe un movimiento válido que comience igual
                if (this.board.mismoComienzo(this.movimiento).size() == 0) {
                    // Limpiamos el movimiento
                    this.movimiento = null;
                    // Volvemos a ejecutar la función, porque aunque no nos valga como segunda casilla
                    // puede que nos valga como primera
                    this.seleccionaCasilla(casilla);
                } else {
                    // Si es final de un camino
                    if (this.board.proximasCasillasMovimiento(this.movimiento).size() == 1) {
                        // Ejecutamos el movimiento
                        onPlayListener.onPlay(this.movimiento);
                        // Limpiamos el movimiento
                        this.movimiento = null;
                    }
                }
            }
            // Recargamos la interfaz y las sugerencias
            this.sugiereCasillas();
        }
    }

    /**
     * Actualiza en la interfaz las casillas sugeridas
     */
    public void sugiereCasillas(){
        // Obtenemos la lista de casillas sugeridas
        this.movimientosSugeridos = this.board.proximasCasillasMovimiento(this.movimiento);
        // Recargamos la interfaz
        this.invalidate();
    }

    /**
     * Reinicia los valores conflictivos que puedan sugerir
     */
    public void reset(){
        this.movimiento = null;
        this.movimientosSugeridos = null;
    }
}