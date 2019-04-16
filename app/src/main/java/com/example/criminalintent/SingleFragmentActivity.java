package com.example.criminalintent;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    //абстрактный класс для создания субклассов активностей с 1 фрагментом

    protected abstract Fragment createFragment(); //метод для создания экземпляра определенного фрагмента

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        /*
        получаем фрагмент-менеджер, он отвечает за управление фрагментами
        и  добавление их представлений в иерархию представлений активности
        */
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null) {
            fragment = createFragment(); //добавление созданного фрагмента
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

    }
}
