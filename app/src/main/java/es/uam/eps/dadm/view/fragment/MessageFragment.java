package es.uam.eps.dadm.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.view.listeners.RecyclerItemClickListener;

/**
 * @author Pablo Manso
 * @version 11/05/2017
 */

public class MessageFragment extends Fragment {
    @BindView(R.id.messageRecyclerView)
    RecyclerView messageRecyclerView;

    private MessagesListAdapter adapter;
    private List<Message> listMessages;

    /**
     * Instancia necesaria de Butterknife para realizar el unbinding
     */
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargamos el layout del fragmento y hacemos binding de las vistas
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        this.unbinder = ButterKnife.bind(this, view);

        listMessages = new ArrayList<Message>();

        listMessages.add(new Message("Pablo", "Hola", true));
        listMessages.add(new Message("Pablo", "Hola", false));
        listMessages.add(new Message("Pablo", "Hola", true));
        listMessages.add(new Message("Pablo", "Hola", false));
        listMessages.add(new Message("Pablo", "Hola", false));
        listMessages.add(new Message("Pablo", "Hola", true));
        listMessages.add(new Message("Pablo", "Hola", true));
        listMessages.add(new Message("Pablo", "Hola", false));
        listMessages.add(new Message("Pablo", "Hola", true));
        listMessages.add(new Message("Pablo", "Hola", false));
        listMessages.add(new Message("Pablo", "Hola", false));
        listMessages.add(new Message("Pablo", "Hola", true));

        // Cogemos el layout manager y le ajustamos un linearlayout para la lista
        this.messageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.messageRecyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new MessagesListAdapter(listMessages);
        messageRecyclerView.setAdapter(adapter);
        messageRecyclerView.scrollToPosition(listMessages.size()-1);

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
}
