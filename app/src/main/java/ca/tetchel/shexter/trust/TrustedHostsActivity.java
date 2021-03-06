package ca.tetchel.shexter.trust;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import ca.tetchel.shexter.R;
import ca.tetchel.shexter.ShexterNotificationManager;
import ca.tetchel.shexter.eventlogger.EventLogger;
import ca.tetchel.shexter.main.MainActivity;

public class TrustedHostsActivity extends AppCompatActivity {

    private static final String
            TAG = MainActivity.MASTER_TAG + TrustedHostsActivity.class.getSimpleName();

    public static final String
            HOST_ADDR_INTENTKEY = "hostAddr",
            HOSTNAME_INTENTKEY = "hostname";

    private AlertDialog newHostDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_trusted_hosts);

        // These two keys are set when this activity is launched by a New Host notification
        // from the ShexterNotificationManager
        String hostAddr = getIntent().getStringExtra(HOST_ADDR_INTENTKEY);
        String hostname = getIntent().getStringExtra(HOSTNAME_INTENTKEY);
        if (!(hostAddr == null && hostname == null)) {
            Log.d(TAG, "Host Addr: " + hostAddr + ", Hostname: " + hostname);
            onNewHost(hostAddr, hostname);

            // Remove the extras so that onNewHost is only triggered once
            getIntent().removeExtra(HOST_ADDR_INTENTKEY);
            getIntent().removeExtra(HOSTNAME_INTENTKEY);
        } else {
            // If they're not set, the user opened this activity normally.
            Log.d(TAG, "Launched without hostname extras");
        }

        refreshHostsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume");
        refreshHostsList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
        if(newHostDialog != null) {
            newHostDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trustedhosts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_remove_all_trusted) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder
                    .setTitle(getString(R.string.remove_all))
                    .setMessage(getString(R.string.confirm_remove_all))
                    .setPositiveButton(getString(R.string.remove_all),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            TrustedHostsUtilities.deleteAllTrustedHosts(TrustedHostsActivity.this);
                            // Redraw with no hosts
                            recreate();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Do nothing
                        }
                    });
            alertBuilder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshHostsList() {
        Log.d(TAG, "Refresh hosts list");
        final ListView hostsList = findViewById(R.id.trustedHostsLV);

        TrustedHostsListAdapter adapter = new TrustedHostsListAdapter(this,
                TrustedHostsUtilities.getTrustedHostsList(this));

        hostsList.setAdapter(adapter);
    }

    /**
     * Display a dialog to the user asking if they trust a new connection, add the host if they want to,
     * then finish this activity.
     */
    private void onNewHost(final String hostAddr, final String hostname) {
        Log.d(TAG, "OnNewHost");
        if(hostAddr == null || hostname == null) {
            // I don't think this will ever happen.
            Toast.makeText(this, getString(R.string.error_adding_new_host), Toast.LENGTH_LONG).show();

            Log.e(TAG, "Only one of hostaddr or hostname was null, which should not happen: "+
                    "HostAddr: " + hostAddr + ", Hostname: " + hostname);
            return;
        }

        String msg = getString(R.string.connect_request_dialog_msg, hostname, hostAddr);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder .setTitle(getString(R.string.new_connection_request))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TrustedHostsUtilities.addKnownHost(TrustedHostsActivity.this,
                                hostAddr, hostname);
                        onAcceptOrRejectHost(true, hostAddr, hostname);
                    }
                })
                .setNegativeButton(getString(R.string.reject), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAcceptOrRejectHost(false, hostAddr, hostname);
                    }
                });

        newHostDialog = alertBuilder.show();
        Log.d(TAG, "Showed new host dialog");
    }

    private void onAcceptOrRejectHost(boolean accepted, String hostAddr, String hostname) {
        Log.d(TAG, "User " + (accepted ? "accepted" : "rejected") + " incoming connection");
        if(accepted) {
            EventLogger.log(getApplicationContext(), getString(R.string.event_new_trusted_host_title),
                    getString(R.string.event_new_trusted_host_detail, hostname, hostAddr));
        }
        else {
            EventLogger.log(getApplicationContext(), getString(R.string.event_rejected_trusted_host_title),
                    getString(R.string.event_rejected_trusted_host_detail, hostname, hostAddr));
        }

        ShexterNotificationManager.clearNewHostNotif(this);
        // start the main activity, then finish this one, so that if user exits then
        // resumes the app, it doesn't prompt them again.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}