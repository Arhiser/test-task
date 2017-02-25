package charge.arhis.testapp.screens;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import charge.arhis.testapp.R;
import charge.arhis.testapp.view.TachometerIndicatorView;

public class MainActivity extends AppCompatActivity {

    private TachometerIndicatorView indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setTitle(R.string.title);
        setContentView(R.layout.activity_main);
        indicator = (TachometerIndicatorView)findViewById(R.id.indicator);
    }
}
