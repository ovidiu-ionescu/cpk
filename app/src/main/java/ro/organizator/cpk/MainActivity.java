package ro.organizator.cpk;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        askPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            // Open the General Preferences fragment directly, it's the only fragment we have
            intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
            intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void askPermission() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                ) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.CALL_PHONE,
            }, 10);
        }
    }

    public void sms(View view) {
        Destination buttonTag = (Destination) view.getTag();
        sms(buttonTag);
    }

    /**
     * Sends a password SMS to a destination
     * @param destination
     */
    private void sms(Destination destination) {
        String phoneNumber = destination.getPhoneNumber();
        SmsManager smsManager = SmsManager.getDefault();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    10);

            // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            //You already have permission
            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String smsPassword = preferences.getString("sms_password", "");
                smsManager.sendTextMessage("tel:" + phoneNumber, null, smsPassword, null, null);
                Toast.makeText(this, "Open gate request sent to " + destination.getName(), Toast.LENGTH_SHORT ).show();
//                Snackbar.make(findViewById(R.id.buttonPanel), "Open gate request sent to " + destination.getName(), Snackbar.LENGTH_LONG).show(); //.setAction("Action", null).show();

            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * SMS all numbers in the destination
     * @param view
     */
    public void smsAll(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String destinationPreferences = preferences.getString("destinations", "");
        final List<Destination> destinations = new PhoneParser().calculateDestinations(destinationPreferences);
        for(Destination destination: destinations) {
            sms(destination);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phone_number, null, open_sesame, null, null);
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        dialButtons();
    }

    /**
     * Add all necessary buttons on the main screen
     */
    private void dialButtons() {
        // add the dial buttons
        LinearLayout layout = (LinearLayout) findViewById(R.id.buttonPanel);

        if(layout.getChildCount() > 0) {
            layout.removeAllViews();
        }

        /*
         * The only way I found on making a styled button is to inflate some xml resource.
         * Specifying the style id in constructor does not work even if the JavaDoc suggests it should.
         */
        final LayoutInflater inflater = LayoutInflater.from(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String destinationPreferences = preferences.getString("destinations", "");
        final List<Destination> destinations = new PhoneParser().calculateDestinations(destinationPreferences);
        if(destinations.isEmpty()) {
            // if no destinations defined complain to the user to define some
            TextView noDestinations = (TextView) inflater.inflate(R.layout.no_destinations, layout, false);
            layout.addView(noDestinations);
        } else {
            if(UserModeKt.isSuperUser()) {
                // render a button for each destination
                for (Destination destination : destinations) {
                    Button button = (Button) inflater.inflate(R.layout.dial_button, layout, false);
                    button.setText("SMS " + destination.getName());
                    button.setTag(destination);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sms(view);
                        }
                    });

                    //add button to the layout
                    layout.addView(button);
                }
            }

            // add one more button for all destinations
            Button button = (Button) inflater.inflate(R.layout.dial_button, layout, false);
            button.setText("Open Gate");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    smsAll(view);
                }
            });
            layout.addView(button);
        }

    }
}
