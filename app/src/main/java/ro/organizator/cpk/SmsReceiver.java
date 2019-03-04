package ro.organizator.cpk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    public static final String SMS_EXTRA_NAME = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dialOpenGate = settings.getBoolean("dial_open_gate", false);
        boolean smsOpenGate = settings.getBoolean("sms_open_gate", false);
        if(!dialOpenGate && !smsOpenGate) {
            return;
        }

        // Get the SMS map from Intent
        Bundle extras = intent.getExtras();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String smsPassword = preferences.getString("sms_password", "");
        String openGateNumber = "tel:" + preferences.getString("open_gate_number", "");
        String smsConfirmationMessage = preferences.getString("confirmation_message", "");
        String smsConfirmationDelayString = preferences.getString("confirmation_delay", "");
        Long smsConfirmationDelay = smsConfirmationDelayString.isEmpty() ? 0 : Long.parseLong(smsConfirmationDelayString);

        StringBuilder messages = new StringBuilder();

        if (extras != null) {
            // Get received SMS array
            Object[] smsExtras = (Object[]) extras.get(SMS_EXTRA_NAME);
            if(null == smsExtras ) {
                Toast.makeText(context, "Null SMS Extra\n", Toast.LENGTH_LONG).show();
                return;
            }

            for (Object smsExtra : smsExtras) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra);

                String body = sms.getMessageBody();
                String address = sms.getOriginatingAddress();

                messages.append("SMS from ").append(address).append(" :\n");
                messages.append(body).append("\n");

                if (body.equalsIgnoreCase(smsPassword)) {
                    String permissions = settings.getString("permissions", "");
                    PhoneParser phoneParser = new PhoneParser();
                    if (phoneParser.isAllowed(address, permissions)) {
                        if(!smsConfirmationMessage.isEmpty()) {
                            SmsManager smsManager = SmsManager.getDefault();
                            try {
                                smsManager.sendTextMessage("tel:" + address, null, smsConfirmationMessage, null, null);
                            } catch (SecurityException se) {
                                Toast.makeText(context, "Security Exception: \n" + se.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            if(smsConfirmationDelay > 0) {
                                try {
                                    Thread.sleep(smsConfirmationDelay);
                                } catch (InterruptedException ie) {
                                    Toast.makeText(context, "Security Exception: \n" + ie.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        if (smsOpenGate) {
                            SmsManager smsManager = SmsManager.getDefault();
                            try {
                                smsManager.sendTextMessage(openGateNumber, null, smsPassword, null, null);
                                Toast.makeText(context, "Open gate request sent to " + openGateNumber, Toast.LENGTH_LONG).show();
                            } catch (SecurityException se) {
                                Toast.makeText(context, "Security Exception: \n" + se.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Intent mIntent = new Intent(Intent.ACTION_CALL);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mIntent.setData(Uri.parse(openGateNumber));
                            try {
                                context.startActivity(mIntent);
                            } catch (SecurityException se) {
                                Toast.makeText(context, "Security Exception: \n" + se.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // Display SMS message
                        Toast.makeText(context, messages.toString() + "Rejected\n", Toast.LENGTH_SHORT).show();
                    }
                }

            }

        }

    }
}
