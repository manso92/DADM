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
 * RoundFragment es un fragmento que mostrará una lista de mensajes
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
     * Constante que nos representará de parámetro en el bundle
     */
    private static final String ARG_SENDER_ID = "sender";

    /**
     * Adaptador donde colocaremos todas nuestros mensajes
     */
    private MessagesAdapter adapter;

    /**
     * Instancia necesaria para hacer unbinding de los componentes
     */
    private Unbinder unbinder;

    /**
     * Ronda o UUID del usuario del que enviaremos y recibiremos los mensajes
     */
    private String recipient;

    /**
     * Repositorio del servidor del que obtendremos y enviaremos los mensajes
     */
    private ServerRepository serverRepository;

    /**
     * Crea una instancia del Fragmento con todos los parámetros colocados
     * @param to Ronda o UUID de usuario del que obtener y enviarm ensajes
     * @return Instancia del fragmento
     */
    public static MessageFragment newInstance(String to) {
        // Creamos un bundle de argumentos y rellenamos lod datos
        Bundle args = new Bundle();
        args.putString(ARG_SENDER_ID, to);

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
        this.serverRepository = (ServerRepository) RoundRepositoryFactory.createRepository(this.getContext(), true);
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
     * Función que se ejecutará cuando se destruya la view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hacemos unbinding de todas las vistas que Butterknife utilizara antes
        unbinder.unbind();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Dejamos de campturar eventos
        GreenRobotEventBus.getInstance().unregister(this);
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
        // Regcargamos la lista de rondas disponibles
        serverRepository.getRoundMessages(this.recipient, messagesCallback);
    }


    /**
     * Manejamos el envío de mensajes al servidor
     * @param v View del botón de enviar
     */
    @OnClick(R.id.btnSendMessage)
    public void send (View v){
        RoundRepository.BooleanCallback booleanCallback = new RoundRepository.BooleanCallback() {
            @Override
            public void onResponse(boolean ok) {
                if (ok) {
                    adapter.addMessage(new Message(Preferences.getPlayerName(getContext()),
                            editTxtMessage.getText().toString(), true));
                    editTxtMessage.setText("");
                    Jarvis.hideKeyboard(getActivity());
                    messageRecyclerView.scrollToPosition(adapter.getItemCount()-1);
                }
                else
                    // TODO change string
                    Snackbar.make(messageRecyclerView.getRootView(), R.string.repository_round_update_error,
                            Snackbar.LENGTH_LONG).show();

            }
        };
        serverRepository.sendMessageToRound(Preferences.getPlayerUUID(this.getContext()),
                this.recipient, editTxtMessage.getText().toString(),booleanCallback);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewMessageEvent msg) throws ExcepcionJuego {
        Log.d(DEBUG, "Mensaje recibido en el roundgrafment: " + msg.toString());

        if ((msg.getMsgtype() == NewMessageEvent.roundMessage) && (msg.getSender().equals(this.recipient))){
            updateUI();
        }
    }
}

