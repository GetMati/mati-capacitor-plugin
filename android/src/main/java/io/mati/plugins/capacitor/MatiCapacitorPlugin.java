package io.mati.plugins.capacitor;

import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.matilock.mati_kyc_sdk.MatiButton;
import com.matilock.mati_kyc_sdk.Metadata;
import com.matilock.mati_kyc_sdk.kyc.KYCActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

@NativePlugin
public class MatiCapacitorPlugin extends Plugin {

    private MatiButton matiButton;

    @PluginMethod
    public void setParams(PluginCall call) {
        final String clientId = call.getString("clientId");
        final String flowId = call.getString("flowId");
        final JSONObject metadata = call.getObject("metadata", null);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                matiButton = new MatiButton(getActivity(), null);
                matiButton.setParams(clientId,
                        flowId,
                        "Default flow",
                        convertToMetadata(metadata));
            }
        });
        call.success();
    }

    public AppCompatActivity getActivity() {
        return (AppCompatActivity)bridge.getActivity();
    }

    @PluginMethod
    public void showMatiFlow(PluginCall call) {
        if (matiButton.getVm().getValue() != null) {
            MatiButton.State matiState = matiButton.getVm().getValue();
            MatiButton.SuccessState matiSuccess = (MatiButton.SuccessState) matiState;

            Intent intent = new Intent(bridge.getActivity().getBaseContext(), KYCActivity.class);
            intent.putExtra("ARG_ID_TOKEN", matiSuccess.getIdToken());
            intent.putExtra("ARG_CLIENT_ID", matiSuccess.getClientId());
            intent.putExtra("ARG_VERIFICATION_ID", matiSuccess.getVerificationId());
            intent.putExtra("ARG_ACCESS_TOKEN", matiSuccess.getAccessToken());
            intent.putExtra("ARG_VOICE_TXT", matiSuccess.getVoiceDataTxt());
            intent.putExtra("STATE_LANGUAGE_ID", matiSuccess.getIdToken());
            bridge.getActivity().startActivityForResult(intent, KYCActivity.REQUEST_CODE);
        } else {
            Log.e("Loading error", "Please check yours Mati client ID or internet connection");
        }

        call.success();
    }

    public Metadata convertToMetadata(final JSONObject metadata)
    {
        if (metadata == null)
            return null;

        Metadata.Builder metadataBuilder = new Metadata.Builder();
        Iterator<String> keys = metadata.keys();
        String key;
        while(keys.hasNext()) {
            key = keys.next();
            try {
                metadataBuilder.with(key, metadata.get (key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return metadataBuilder.build();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == KYCActivity.REQUEST_CODE) {
            if(resultCode == KYCActivity.RESULT_OK) {
                bridge.triggerWindowJSEvent("Verification success", String.format("{ 'login success': %s }", ""));
            } else {
                bridge.triggerWindowJSEvent("Verification cancelled");
            }
        }
    }

}



