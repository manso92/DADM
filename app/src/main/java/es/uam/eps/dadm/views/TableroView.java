package es.uam.eps.dadm.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Casilla;
import es.uam.eps.dadm.model.Ficha;
import es.uam.eps.dadm.model.MovimientoDamas;
import es.uam.eps.dadm.model.TableroDamas;
import es.uam.eps.multij.Tablero;

/**
 * Esta clase es una view personalizada que pinta el tablero
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class TableroView extends View {

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

    private Casilla origen = null;

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
        widthOfTile = w / TableroDamas.TABLEROSIZE;
        heightOfTile = h / TableroDamas.TABLEROSIZE;

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
        for (int i = 0; i < TableroDamas.TABLEROSIZE; i++)
            for (int j = 0; j <  TableroDamas.TABLEROSIZE; j++)
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
            paint.setColor(this.getContext().getResources().getColor(R.color.casillaOscura));
        if (casilla.getColor() == Casilla.Color.CLARA)
            paint.setColor(this.getContext().getResources().getColor(R.color.casillaClara));

        // Dibujamos el cuadrado del tablero
        canvas.drawRect((casilla.col()*(int) widthOfTile),
                        (casilla.row()*(int) heightOfTile),
                        ((casilla.col() + 1)*(int) widthOfTile),
                        ((casilla.row() + 1)*(int) heightOfTile),
                        paint);

        // Si la casilla tiene una ficha, la pintamos también
        if (casilla.tieneFicha())
            pintaFicha(canvas, casilla);
    }

    /**
     * Pinta la ficha encima de un recuadro
     * @param canvas Canvas en el que dibujar
     * @param casilla Casilla con la ficha que vamos a pintar
     */
    private void pintaFicha(Canvas canvas, Casilla casilla) {
        // Obtenemos la imagen vectorial que queremos dibujar
        VectorDrawableCompat mMyVectorDrawable =
                VectorDrawableCompat.create(getContext().getResources(), getFichaDrawable(casilla.getFicha()), null);

        // Colocamos la imagen del tamaño necesario
        mMyVectorDrawable.setBounds((casilla.col()*(int) widthOfTile),
                                    (casilla.row()*(int) heightOfTile),
                                    ((1+casilla.col())*(int) widthOfTile),
                                    ((1+casilla.row())*(int) heightOfTile));

        // Dibujamos la ficha
        mMyVectorDrawable.draw(canvas);
    }

    /**
     * Devuelve el identificador de recurso de la ficha que tenemos que pintar
     * @param ficha Ficha a pintar
     * @return Identificador del recurso que pintaremos
     */
    private int getFichaDrawable(Ficha ficha){
        // Comrpobamos el color de la ficha y el tipo y devolvemos el identificador en consecuencia
        if (ficha.color == Ficha.Color.BLANCA) {
            if (ficha.getTipo() == Ficha.Tipo.DAMA)  return R.drawable.dama_blanca;
            if (ficha.getTipo() == Ficha.Tipo.REINA) return R.drawable.reina_blanca;
        }
        if (ficha.color == Ficha.Color.NEGRA) {
            if (ficha.getTipo() == Ficha.Tipo.DAMA)  return R.drawable.dama_negra;
            if (ficha.getTipo() == Ficha.Tipo.REINA) return R.drawable.reina_negra;
        }
        return 0;
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

        float a = event.getDownTime();

        if (board.getEstado() != Tablero.EN_CURSO)
            return true;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (this.origen == null) {
                /*
                TODO hay que hacer una función del tablero que compruebe si los movimientos que llevamos hasta ahora
                constituyen el inicio de una cadena de movimientos válidos. De este modo si el origen no está
                ni siquiera lo almacenaremos, si se ha registrado un movimiento de dos válido pero hay uno de tres válido esperamos
                 y si solo encontramos un movimiento válido pues le enviamos
                 */
                this.origen = fromEventCasilla(event);

            } else {
                onPlayListener.onPlay(new MovimientoDamas(this.origen, fromEventCasilla(event)));
                this.origen = null;
            }
        }

        return true;
    }
}