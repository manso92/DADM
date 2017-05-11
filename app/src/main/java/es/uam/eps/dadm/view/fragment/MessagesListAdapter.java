package es.uam.eps.dadm.view.fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;

import java.util.ArrayList;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.MessageHolder> {

    private List<Message> messages;
    public MessagesListAdapter(List<Message> messages) {
        this.messages = messages;
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }
    public void addMessages(List<Message> rounds) {
        if (this.messages == null) this.messages = rounds;
        else this.messages.addAll(rounds);
        this.notifyDataSetChanged();
    }
    public void clear() {
        this.messages = new ArrayList<Message>();
    }


    public Message getRound(int position) {
        return this.messages.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return this.messages.get(position).isSelf() ? 1 : 0;
    }


    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Cogemos el inflater para poder colocar nuestra vista
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        switch (viewType) {
            case 0:
                view = layoutInflater.inflate(R.layout.list_item_message_left, parent, false);
                break;
            case 1:
                view = layoutInflater.inflate(R.layout.list_item_message_right, parent, false);
                break;
        }
        // Retornamos el objeto que manejará la vista
        return new MessageHolder(view);

    }
    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        // Obtenemos la info de la partida que vamos a cargar
        Message round = messages.get(position);
        // Rellenamos  los datos con la ronda que acabamos de obtener
        holder.bindMessage(round);
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }

































    public class MessageHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.lblMsgFrom)
        TextView lblMsgFrom;
        @BindView(R.id.txtMsg)
        TextView txtMsg;

        public MessageHolder(View itemView) {
            // Llamamos a la clase padre para la construcción y hacemos binding de componentes
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(Message message) {
            txtMsg.setText(message.getMessage());
            lblMsgFrom.setText(message.getFromName());
        }
    }
}
