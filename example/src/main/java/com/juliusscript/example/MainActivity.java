package com.juliusscript.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //access rxannotated methods
        RxApi.getInstance().getIntegerRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Toast.makeText(MainActivity.this, "Integer value is: " + integer, Toast.LENGTH_SHORT).show();
                    }
                });

        RxApiTwo apiTwo = new RxApiTwo();
        apiTwo.getValueRx("noot")
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) {
                        Log.i(MainActivity.class.getSimpleName(), result);
                    }
                });
    }
}
