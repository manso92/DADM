package com.manso92.damas.view.adapters;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.manso92.damas.R;

import java.util.ArrayList;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * MessagesAdapter manejará la lista de mensajes que se mostrará
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageHolder> {

    /**
     * Lista de mensajes que se representará en la view
     */
    private List<Message> messages;

    /**
     * Construye un nuevo adaptador
     */
    public MessagesAdapter() {
        this.messages = new ArrayList<>();
    }

    /**
     * Construye un nuevo adaptador con una lista de mensajes
     * @param messages Lista de mensajes que cargaremos en el adapter
     */
    public MessagesAdapter(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Cambia la lista de mensajes que hay en la view
     * @param messages Lista de mensajes
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    /**
     * Añade un mensaje a la lista
     * @param message Mensaje que se añade a la lista que hay
     */
    public void addMessage(Message message) {
        if (this.messages == null) this.messages = new ArrayList<>();
        this.messages.add(message);
        this.notifyDataSetChanged();
    }

    /**
     * Añade más mensajes a la lista
     * @param messages Mensajes que se añaden a la lista que hay
     */
    public void addMessages(List<Message> messages) {
        if (this.messages == null) this.messages = messages;
        else this.messages.addAll(messages);
        this.notifyDataSetChanged();
    }

    /**
     * Limpia la lista de mensajes que tiene el adapter
     */
    public void clear() {
        this.messages = new ArrayList<Message>();
    }

    /**
     * Obtiene un mensaje de la lista
     * @param position Posición del mensaje en la lista
     * @return Mensaje que se buscaba
     */
    public Message getMessage(int position) {
        return this.messages.get(position);
    }

    /**
     * Indica el tipo de view que se debe cargar
     * @param position Posición de la lista del elemento del que queremos obtener el tipo
     * @return El id del layout que hay que cargar
     */
    @Override
    public int getItemViewType(int position) {
        // Comprobamso si el mensaje es nuestro o de otro
        return this.messages.get(position).isSelf() ?
                R.layout.list_item_message_right :
                R.layout.list_item_message_left;
    }

    /**
     * Crea un holder del mensaje con los datos necesarios
     * @param parent Padre contenedor de la vista
     * @param viewType Layout que cargaremos para representar
     * @return Holder con nuestro mensaje
     */
    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Cogemos el inflater para poder colocar nuestra vista
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(viewType, parent, false);

        // Retornamos el objeto que manejará la vista
        return new MessageHolder(view);
    }

    /**
     * Hace binding de los datos necesarios en el holder
     * @param holder Contenedor de la vista que mostrará
     * @param position Posición que ocupa el mensaje en la lista de datos
     */
    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        // Obtenemos la info del mensaje que vamos a cargar
        Message message = messages.get(position);

        boolean user;
        if (position > 0)
            user = message.isSelf() != messages.get(position-1).isSelf();
        else
            user = true;

        // Rellenamos  los datos con el mensaje que acabamos de obtener
        holder.bindMessage(message, user);
    }

    /**
     * Indica la cantidad de mensajes que tiene el contenedor
     * @return Tamaño de la lista de mensajes
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Holder que se encargará de colocar en la interfaz cada uno de los elementos del item
     *
     * @author Pablo Manso
     * @version 13/05/2017
     */
    public class MessageHolder extends RecyclerView.ViewHolder {

        /**
         * Etiqueta que indica quién ha mandado el mensaje
         */
        @BindView(R.id.lblMsgFrom)
        TextView lblMsgFrom;

        /**
         * Mensaje que se ha enviado
         */
        @BindView(R.id.txtMsg)
        TextView txtMsg;

        /**
         * Crea la vista y hace binding de los componentes
         * @param itemView Item en el que cargaremos los datos
         */
        public MessageHolder(View itemView) {
            // Llamamos a la clase padre para la construcción y hacemos binding de componentes
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /**
         * Coloca los datos de un mensaje en la vista
         * @param message Mensaje que se colocará en la vista
         */
        public void bindMessage(Message message, boolean user) {
            if (user)
                lblMsgFrom.setText(message.getFromName());
            else
                lblMsgFrom.setVisibility(View.GONE);
            txtMsg.setText(message.getMessage());
        }
    }
}
