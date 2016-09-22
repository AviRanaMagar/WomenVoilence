package womenvoilence.project.com.womenvoilence;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private final String LOG_TAG = "WOMEN_VOILENCE_LOG_TAG";
    private TextView locationTV;
    private EditText mobileNoET, messageET;
    Button getLocationbtn, pickContact;
    ProgressDialog progress;

    private void setupProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setTitle("Location");
        progress.setMessage("Wait while getting location...");
    }

    private void checkGPSStatus() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e(LOG_TAG, "Gps disabled");
            showDialogBox();
        } else {
            Log.e(LOG_TAG, "Gps Enabled");
        }
    }

    private void showDialogBox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkGPSStatus();
        setupProgressDialog();


        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this).addApi(LocationServices.API).addConnectionCallbacks(MainActivity.this).addOnConnectionFailedListener(MainActivity.this).build();

        mobileNoET = (EditText) findViewById(R.id.mobileNo);
        messageET = (EditText) findViewById(R.id.message);

        locationTV = (TextView) findViewById(R.id.location);
        pickContact = (Button) findViewById(R.id.pickContact);
        pickContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);


            }
        });

        getLocationbtn = (Button) findViewById(R.id.sendMessage);
        getLocationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mobileNoET.getText().toString().isEmpty()) {
                    if (!messageET.getText().toString().isEmpty()) {
                        if (!locationTV.getText().toString().isEmpty()) {
                            sendSms(mobileNoET.getText().toString(), messageET.getText().toString(), locationTV.getText().toString());
                        } else {
                            Toast.makeText(MainActivity.this, "Location not Provided!", Toast.LENGTH_SHORT).show();
                            checkGPSStatus();
                            return;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Message is empty!", Toast.LENGTH_SHORT).show();

                        return;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Mobile No is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }


                Log.e(LOG_TAG, "Button clicked!");
            }
        });

    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.e(LOG_TAG, mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "last location");
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "connection Failed");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(LOG_TAG, location.getLatitude() + "," + location.getLongitude());
        locationTV.setText(location.getLatitude() + "," + location.getLongitude());
//        mGoogleApiClient.disconnect();
    }

    public void setSharedPreference() {
        SharedPreferences sharedPreferences = getPreferences(0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("a", 67);
        editor.commit();
    }

    public void logSharedPrefences() {
        SharedPreferences sharedPreferences = getPreferences(0);
        Log.i("value", sharedPreferences.getInt("a", 0) + "");
    }

    public static final String SENT_SMS_BOARDCAST_KEY = "SENT_SMS";

    public void sendSms(String mobileNo, String message, String location) {
        Intent sentIn = new Intent(SENT_SMS_BOARDCAST_KEY);
        sentIn.putExtra("mobileNo", mobileNo);
        PendingIntent sentPIn = PendingIntent.getBroadcast(getBaseContext(), 0, sentIn, PendingIntent.FLAG_UPDATE_CURRENT);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(mobileNo, null, message + "[" + location + "]", sentPIn, null);

    }

    static final int PICK_CONTACT = 1;
    public String cNumber;

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {


                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            cNumber = phones.getString(phones.getColumnIndex("data1"));
                            System.out.println("number is:" + cNumber);
                            mobileNoET.setText(cNumber);
                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));


                    }
                }
                break;
        }
    }


}
