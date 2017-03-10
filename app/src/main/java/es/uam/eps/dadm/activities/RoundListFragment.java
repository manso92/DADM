package es.uam.eps.dadm.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;

public class RoundListFragment extends Fragment {
    private static final int SIZE = 3;
    private RecyclerView roundRecyclerView;
    private RoundAdapter roundAdapter;

    private Callbacks callbacks;
    public interface Callbacks {
        void onRoundSelected(Round round);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round_list, container, false);
        roundRecyclerView = (RecyclerView) view.findViewById(R.id.round_recycler_view);
        RecyclerView.LayoutManager linearLayoutManager = new
                LinearLayoutManager(getActivity());
        roundRecyclerView.setLayoutManager(linearLayoutManager);
        roundRecyclerView.setItemAnimator(new DefaultItemAnimator());

        roundRecyclerView.addOnItemTouchListener(new
                RecyclerItemClickListener(getActivity(), new
                RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Round round =
                                RoundRepository.get(getContext()).getRounds().get(position);
                        callbacks.onRoundSelected(round);
                    }
                }));

        updateUI();
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    public void updateUI() {
        RoundRepository repository = RoundRepository.get(this.getActivity());
        List<Round> rounds = repository.getRounds();
        if (roundAdapter == null) {
            roundAdapter = new RoundAdapter(rounds);
            roundRecyclerView.setAdapter(roundAdapter);
        } else {
            roundAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_round:
                Round round = new Round(RoundRepository.SIZE);
                RoundRepository.get(getActivity()).addRound(round);
                updateUI();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.RoundHolder>{
        private List<Round> rounds;
        public class RoundHolder extends RecyclerView.ViewHolder {
            private TextView idTextView;
            private TextView boardTextView;
            private TextView dateTextView;
            private Round round;
            public RoundHolder(View itemView) {
                super(itemView);
                idTextView = (TextView) itemView.findViewById(R.id.list_item_id);
                boardTextView = (TextView) itemView.findViewById(R.id.list_item_board);
                dateTextView = (TextView) itemView.findViewById(R.id.list_item_date);
            }
            public void bindRound(Round round){
                this.round = round;
                idTextView.setText(round.getTitle());
                boardTextView.setText(round.getBoard().toString());
                dateTextView.setText(String.valueOf(round.getDate()).substring(0,19));
            }

        }
        public RoundAdapter(List<Round> rounds){
            this.rounds = rounds;
        }
        @Override
        public RoundAdapter.RoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.list_item_round, parent, false);
            return new RoundHolder(view);
        }
        @Override
        public void onBindViewHolder(RoundAdapter.RoundHolder holder, int position) {
            Round round = rounds.get(position);
            holder.bindRound(round);
        }
        @Override
        public int getItemCount() {
            return rounds.size();
        }
    }
}
