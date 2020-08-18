package com.joke.easyjoke;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.joke.baseibrary.ioc.CheckNet;
import com.joke.baseibrary.ioc.OnClick;
import com.joke.baseibrary.ioc.ViewById;
import com.joke.baseibrary.ioc.ViewUtils;

public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.tv1)
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        mTv.setText("IOC for me");
    }

    @OnClick(R.id.tv1)
    @CheckNet
    private void onClick() {
        Toast.makeText(this, "IOC inject onclick", Toast.LENGTH_LONG).show();
    }
}
