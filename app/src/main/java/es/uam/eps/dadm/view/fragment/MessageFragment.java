package es.uam.eps.dadm.view.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.GreenRobotEventBus;
import es.uam.eps.dadm.events.NewMessageEvent;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.server.ServerRepository;
import es.uam.eps.dadm.view.activities.Jarvis;
import es.uam.eps.dadm.view.adapters.Message;
import es.uam.eps.dadm.view.adapters.MessagesAdapter;
import es.uam.eps.multij.ExcepcionJuego;

/**
 * MessageFragment es un fragmento que mostrará una lista de mensajes
 *
 * @author Pablo Manso
 * @version 11/05/2017
 */
public class MessageFragment extends Fragment {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.MessageFrag";

    /**
     * Recyclerview que contendrá la lista de mensajes
     */
    @BindView(R.id.messageRecyclerView)
    RecyclerView messageRecyclerView;

    /**
     * Edittext del que obtendremos el texto a enviar
     */
    @BindView(R.id.editTxtMessage)
    EditText editTxtMessage;

    /**
     * Constante que nos indicará a quién hay que enviar los mensajes
     */
    private static final String ARG_SENDER_ID = "recipient";

    /**
     * Ronda o UUID del usuario del que enviaremos y recibiremos los mensajes
     */
    private String recipient;

    /**
     * Constante que nos indicará qué tipo de conversación estamos empezando
     */
    private static final String ARG_TYPE = "type";

    /**
     * Nos indica si la conversación se produce en una ronda o con un usuario particular
     */
    private boolean round = false;

    /**
     * Adaptador donde colocaremos todas nuestros mensajes
     */
    private MessagesAdapter adapter;

    /**
     * Instancia necesaria para hacer unbinding de los componentes
     */
    private Unbinder unbinder;

    /**
     * Repositorio del servidor del que obtendremos y enviaremos los mensajes
     */
    private ServerRepository serverRepository;

    /**
     * Crea una instancia del Fragmento con todos los parámetros colocados
     * @param to Ronda o UUID de usuario del que obtener y enviar mensajes
     * @return Instancia del fragmento
     */
    public static MessageFragment newInstance(String to, boolean round) {
        // Creamos un bundle de argumentos y rellenamos lod datos
        Bundle args = new Bundle();
        args.putString(ARG_SENDER_ID, to);
        args.putBoolean(ARG_TYPE, round);

        // Creamos un fragmento, colocamos los datos y devolvemos el framento
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setArguments(args);
        return messageFragment;
    }

    /**
     * Prepara el fragmento para su ejecución
     * @param savedInstanceState Pares clave-valor con los datos que serán necesarios para la ejecución del fragmento
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Coge el parámetro del remitente y creamos el servidor
        this.recipient = getArguments().getString(ARG_SENDER_ID);
        this.round = getArguments().getBoolean(ARG_TYPE);
        this.serverRepository = (ServerRepository) RoundRepositoryFactory.createRepository(this.getContext(), true);
    }

    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        super.onStart();

        // Empezamos a capturar los eventos
        GreenRobotEventBus.getInstance().register(this);

        // Actualizamos la lista de mensajes
        this.updateUI();
    }

    /**
     * Ejecución al final del fragmento
     */
    @Override
    public void onStop() {
        super.onStop();

        // Dejamos de campturar eventos
        GreenRobotEventBus.getInstance().unregister(this);
    }

    /**
     * Ejecutará las acciones necesarias para cuando se cree la vista
     * @param inflater Clase que se encargará de mostrar los elementos del fragment
     * @param container Contenedor del fragment
     * @param savedInstanceState Pares clave valor que se nos dan como parámetro
     * @return View que se acaba de crear
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargamos el layout del fragmento y hacemos binding de las vistas
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        this.unbinder = ButterKnife.bind(this, view);

        // Cogemos el layout manager y le ajustamos un linearlayout para la lista
        this.messageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.messageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }

    /**
     * Función que se ejecutará cuando se destruya la view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Hacemos unbinding de todas las vistas que Butterknife utilizara antes
        unbinder.unbind();
    }

    /**
     * Actualiza la lista de mensajes en el recyclerview
     */
    public void updateUI() {
        // Si está cargado el roundadapter, lo vaciamos
        if(this.adapter != null) this.adapter.clear();

        // Registramos el callback que manejará la lista de partidas devueltas
        ServerRepository.MessagesCallback messagesCallback = new ServerRepository.MessagesCallback() {
            @Override
            public void onResponse(List<Message> messages) {
                // Si no hay adapter lo creamos, y si lo hay, las añadimos
                if (adapter == null)
                    adapter = new MessagesAdapter(messages);
                else
                    adapter.addMessages(messages);

                // Añadimos el adapter al recyclerview
                if (messageRecyclerView != null)
                    messageRecyclerView.setAdapter(adapter);

                // Hacemos scrollhasta la última posición
                messageRecyclerView.scrollToPosition(messages.size()-1);

            }
            @Override
            public void onError(String error) {
                // Mostramos el error que nos indican al llamar al callback
                Snackbar.make(messageRecyclerView, error, Snackbar.LENGTH_LONG).show();
            }
        };

        // Obtenemos la lista de mensajes del servidor
        if (this.round)
            serverRepository.getRoundMessages(this.recipient, messagesCallback);
        else
            serverRepository.getUserMessages(this.recipient, messagesCallback);
    }


    /**
     * Manejamos el envío de mensajes al servidor
     * @param v View del botón de enviar
     */
    @OnClick(R.id.btnSendMessage)
    public void send (View v){

        // Registramos el callback que manejará la respuesta del servidor
        RoundRepository.BooleanCallback booleanCallback = new RoundRepository.BooleanCallback() {
            @Override
            public void onResponse(boolean ok) {
                if (ok) {
                    // Si el adapter no está creado, lo creamos
                    if (adapter == null) adapter = new MessagesAdapter();

                    // Añadimos el adapter al recyclerview
                    if (messageRecyclerView != null) messageRecyclerView.setAdapter(adapter);

                    // Añadimos todos los mensajes al adapter
                    adapter.addMessage(new Message(Preferences.getPlayerName(getContext()),
                            editTxtMessage.getText().toString(), true));

                    // Hacemos scrollhasta la última posición y limpiamos en mensaje
                    editTxtMessage.setText("");
                    Jarvis.hideKeyboard(getActivity());
                    messageRecyclerView.scrollToPosition(adapter.getItemCount()-1);
                }
                else
                    // TODO change string
                    // Indicamos al usuario que su mensaje no ha podido ser enviado
                    Snackbar.make(messageRecyclerView.getRootView(), R.string.repository_round_update_error,
                            Snackbar.LENGTH_LONG).show();

            }
        };

        // Enviamos al servidor el mensaje
        if (this.round)
            serverRepository.sendMessageToRound(Preferences.getPlayerUUID(this.getContext()),
                    this.recipient, editTxtMessage.getText().toString(),booleanCallback);
        else
            serverRepository.sendMessageToUser(Preferences.getPlayerUUID(this.getContext()),
                    this.recipient, editTxtMessage.getText().toString(),booleanCallback);
    }

    /**
     * Captura los mensajes que se reciben por Firebase para mostrar los mensajes recibidos
     * @param msg Mensaje que contiene todos los datos necesarios para empezar un chat
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewMessageEvent msg) throws ExcepcionJuego {
        Log.d(DEBUG, "Mensaje recibido en el roundgrafment: " + msg.toString());

        // Si es un mensaje de ronda y es para la nuestra actualizamos la interfaz
        if ((this.round) && (msg.getMsgtype() == NewMessageEvent.roundMessage) &&
                (msg.getSender().equals(this.recipient)))
            updateUI();

        // Si es un mensaje para un usuario, y es la conversación que tenemos abierta lo mostramos
        if ((!this.round) && (msg.getMsgtype() == NewMessageEvent.userMessage) &&
                (msg.getSender().equals(this.recipient)))
            updateUI();
    }
}

