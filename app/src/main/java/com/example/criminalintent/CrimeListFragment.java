package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment { //фрагмент с RecyclerView для отображения прокручиваемого списка crimes

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;

    private CrimeAdapter mAdapter; //адаптер RecyclerView
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    //private int mAdapterItemChanged; //позиция CrimeHolder в CrimeAdapter для обновления

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);



        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); //обязательный LayoutManager без которого ресайкл не сможет работать

        updateUI(); //метод обновляющий интерфейс

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mCrimeRecyclerView);

        if(savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() { //обновить интерфейс списка при возвращении из другой активности
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem  = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity()); //создаем список crime
        List<Crime> crimes = crimeLab.getCrimes(); //получаем его

        if(mAdapter == null) { //если адаптер не создан
            mAdapter = new CrimeAdapter(crimes); //создаем адаптер с этим списком
            mCrimeRecyclerView.setAdapter(mAdapter); //устанавливаем адаптер к RecyclerView
        } else {
            //mAdapter.notifyItemChanged(mAdapterItemChanged); //обновление конкретного CrimeHolder
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged(); //просто обновить адаптер
        }

        updateSubtitle();
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);
        if(!mSubtitleVisible){
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subtitle);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener { //класс ViewHolder для одного crime на экране

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        private Crime mCrime;
        private String dateFormat = "EEEE, dd MMM, yyyy";


        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) { //обязательный конструктор
            super(inflater.inflate(R.layout.list_item_crime, parent, false)); //передает представление одного crime
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);

        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(DateFormat.format(dateFormat,mCrime.getDate()));
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            //mAdapterItemChanged = this.getAdapterPosition(); //запись позиции CrimeHolder для обновления
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> implements ItemTouchHelperAdapter{ //адаптер для RecyclerView

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){ //передаем список конструктору
            mCrimes = crimes;
        }

        //3 обязательных метода для работы адаптера
        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //создание нового viewHolder
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder crimeHolder, int i) {
            Crime crime = mCrimes.get(i);
            crimeHolder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }


        @Override
        public void onItemMove(int fromPosition, int toPosition) {
        }

        @Override
        public void onItemDismiss(int position) {
            //notifyItemRemoved(position);
            CrimeLab.get(getActivity()).deleteCrime(mCrimes.get(position));
            updateUI();
            if(getActivity().findViewById(R.id.detail_fragment_container) != null) {
                mCallbacks.onCrimeSelected(mCrimes.get(position == 0 ? 0 : position - 1));
            }
        }
    }

}
