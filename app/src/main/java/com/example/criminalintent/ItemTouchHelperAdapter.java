package com.example.criminalintent;

public interface ItemTouchHelperAdapter {

    void onItemMove (int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
