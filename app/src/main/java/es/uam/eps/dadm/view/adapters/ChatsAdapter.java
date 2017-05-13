package es.uam.eps.dadm.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Preferences;

/**
 * MessagesAdapter manejará la lista de mensajes que se mostrará
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.MessageHolder> {

    /**
     * Lista de mensajes que se representará en la view
     */
    private List<Message> messages;

    /**
     * Contexto desde el que se crea el adapter
     */
    private Context context;

    /**
     * Construye un nuevo adaptador con una lista de mensajes
     * @param messages Lista de mensajes que cargaremos en el adapter
     */
    public ChatsAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
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
     * Crea un holder del mensaje con los datos necesarios
     * @param parent Padre contenedor de la vista
     * @param viewType Layout que cargaremos para representar
     * @return Holder con nuestro mensaje
     */
    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Cogemos el inflater para poder colocar nuestra vista
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_chat, parent, false);

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
        // Rellenamos  los datos con el mensaje que acabamos de obtener
        holder.bindMessage(message);
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
        @BindView(R.id.username)
        TextView username;

        /**
         * Mensaje que se ha enviado
         */
        @BindView(R.id.lastmessage)
        TextView lastmessage;

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
        public void bindMessage(Message message) {
            username.setText(message.getFromName());
            if (message.getMessage().length() > 20) message.setMessage(message.getMessage().substring(0,20));
            if (!message.isSelf()) lastmessage.setText(message.getFromName() + ": " + message.getMessage());
            else lastmessage.setText(Preferences.getPlayerName(context) + ": " + message.getMessage());
        }
    }
}
