package ca.tetchel.shexter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import ca.tetchel.shexter.sms.ShexterService;

/**
 * Receives an Intent when the phone finishes booting which triggers starting the ShexterService.
 * TODO need a way to stop/start the service (from UI)
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver", "Received an intent: " + intent.getAction());
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, ShexterService.class);
            Log.d("BootReceiver", "Starting SMSServer on boot.");

            Toast.makeText(context, "shexter started on boot", Toast.LENGTH_LONG).show();

            context.startService(pushIntent);
        }
    }
}