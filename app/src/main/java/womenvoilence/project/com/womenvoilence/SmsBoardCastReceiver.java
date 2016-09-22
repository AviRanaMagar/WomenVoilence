package womenvoilence.project.com.womenvoilence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsBoardCastReceiver extends BroadcastReceiver {
    public SmsBoardCastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "SENT_SMS":
                String mobileNo = intent.getExtras().getString("mobileNo");
                Log.e("sms_sent_mobile_no", mobileNo);
                Toast.makeText(context, "Sms to " + mobileNo + " sent succssfully!", Toast.LENGTH_SHORT).show();
                break;
            case "android.provider.Telephony.SMS_RECEIVED":
                String[] smsDetail = readMessage(context, intent);
                String smsSender = smsDetail[0];
                String smsBody = smsDetail[1];

                if ((smsBody.indexOf("[") > 0) && (smsBody.indexOf("]") > 0)) {
                    String latAndLong = smsBody.substring(smsBody.indexOf("[") + 1, smsBody.indexOf("]"));
                    int indexOfComma = latAndLong.indexOf(",");
                    String latitude = latAndLong.substring(0, indexOfComma);
                    String longitude = latAndLong.substring(indexOfComma + 1, latAndLong.length());

                    String data = "google.navigation:q=" + latitude + "," + longitude + "";
                    Uri gmmIntentUri = Uri.parse(data);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);

                }

                break;
        }
    }

    public String[] readMessage(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessage = "";
        String smsBody = "";
        String smsSender = "";
        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                strMessage += "SMS From: " + messages[i].getOriginatingAddress();
                smsSender = messages[i].getOriginatingAddress();
                strMessage += " : ";
                strMessage += messages[i].getMessageBody();
                smsBody = messages[i].getMessageBody();
                strMessage += "\n";
            }

            Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
            Log.e("Message SMS:", strMessage);
        }
        String messageDetail[] = new String[2];
        messageDetail[0] = smsSender;
        messageDetail[1] = smsBody;
        return messageDetail;
    }
}