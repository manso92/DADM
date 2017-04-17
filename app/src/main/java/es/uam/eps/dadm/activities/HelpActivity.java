package es.uam.eps.dadm.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import es.uam.eps.dadm.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setTitle(R.string.settings_see_help);

        TextView foo = (TextView)findViewById(R.id.helpTextView);
        foo.setText(Html.fromHtml(getString(R.string.help_help_text)));
    }
}
