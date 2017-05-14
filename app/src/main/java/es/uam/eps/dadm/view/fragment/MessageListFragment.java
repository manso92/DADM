package es.uam.eps.dadm.view.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.NewChatEvent;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.server.ServerRepository;
import es.uam.eps.dadm.view.activities.ChatActivity;
import es.uam.eps.dadm.view.activities.Jarvis;
import es.uam.eps.dadm.view.adapters.ChatsAdapter;
import es.uam.eps.dadm.view.adapters.Message;
import es.uam.eps.dadm.view.alerts.NewMessageDialogFragment;
import es.uam.eps.dadm.view.listeners.RecyclerItemClickListener;

/**
 * MessageListFragment es un fragmento que mostrará una lista chats disponibles
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class MessageListFragment extends Fragment {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.MessageListFrag";

    /**
     * Recyclerview que contendrá la lista de chats
     */
    @BindView(R.id.recycler_view)
    RecyclerView chatRecyclerView;

    /**
     * Swipe to reload view para recargar la lista
     */
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout refreshLayout;

    /**
     * Fab para añadir un nuevo chat
     */
    @BindView(R.id.fab)
    FloatingActionButton addFoundFab;

    /**
     * Adaptador donde colocaremos todas nuestros mensajes
     */
    private ChatsAdapter adapter;

    /**
     * Instancia necesaria para hacer unbinding de los componentes
     */
    private Unbinder unbinder;

    /**
     * Repositorio del servidor del que obtendremos y enviaremos los mensajes
     */
    private ServerRepository serverRepository;

    /**
     * Prepara el fragmento para su ejecución
     * @param savedInstanceState Pares clave-valor con los datos que serán necesarios para la ejecución del fragmento
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creamos la instancia del repositorio
        this.serverRepository = (ServerRepository) RoundRepositoryFactory.createRepository(this.getContext(), true);
    }

    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        super.onStart();

        // Empezamos a capturar los eventos
        Jarvis.event().register(this);

        // Actualizamos la lista de mensajes
        this.updateUI();
    }


    /**
     * Ejecución al fin del fragmento
     */
    @Override
    public void onStop() {
        super.onStop();

        // Dejamos de campturar eventos
        Jarvis.event().unregister(this);
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
        final View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        this.unbinder = ButterKnife.bind(this, view);

        // Configuramos todo lo necesario del recyclerview
        setupRecyclerView();

        // Añadimos el listener que recargará la lista mostrada
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateUI();
                    }
                }
        );

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
     * Configura el recyvlerview con la lista de partidas que toque
     */
    public void setupRecyclerView(){
        // Cogemos el layout manager y le ajustamos un linearlayout para la lista
        this.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.chatRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Añadimos el listener de la lista
        this.chatRecyclerView.addOnItemTouchListener(new
                RecyclerItemClickListener(getActivity(), new
                RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        // Arrancamos una conversación con el usuario seleccionado
                        if (position < adapter.getItemCount())
                            startActivity(ChatActivity.newIntent(getActivity(),
                                    adapter.getMessage(position).getFromName()));
                    }
                }));
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
                    adapter = new ChatsAdapter(messages,getContext());
                else
                    adapter.addMessages(messages);

                // Añadimos el adapter al recyclerview
                if (chatRecyclerView != null)
                    chatRecyclerView.setAdapter(adapter);

                // Hacemos scrollhasta la última posición
                chatRecyclerView.scrollToPosition(messages.size()-1);

                // Parar la animación del indicador
                if (refreshLayout != null)
                    refreshLayout.setRefreshing(false);

            }
            @Override
            public void onError(String error) {
                // Mostramos el error que nos indican al llamar al callback
                Jarvis.error(ShowMsgEvent.Type.TOAST, error);
            }
        };
        // Regcargamos la lista de rondas disponibles
        serverRepository.getLastMessages(messagesCallback);
    }

    /**
     * Captura el evento click en el botón de empezar chat
     * @param v View del botón que se pulsa
     */
    @OnClick(R.id.fab)
    public void newRound(View v) {
        (new NewMessageDialogFragment()).show(getActivity().getSupportFragmentManager(),"User selection");
    }

    /**
     * Captura los mensajes que se enviarán para crear una nueva ventana de chat
     * @param msg Mensaje que contiene todos los datos necesarios para empezar un chat
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewChatEvent msg) {
        Log.d(DEBUG, "Mensaje recibido en el roundgrafment: " + msg.toString());
        startActivity(ChatActivity.newIntent(this.getActivity(),msg.getUser()));
    }
}

