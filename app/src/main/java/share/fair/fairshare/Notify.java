package share.fair.fairshare;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Notify {

public static void test(Context context){
    String url = "https://gcm-http.googleapis.com/gcm/send";

    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
              Log.w("custom", "the response is: " + response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.w("custom", "can't get response");

                }
            }
    ) {
        @Override
        public byte[] getBody() throws AuthFailureError {
            String testString = " {\n" +
                    "    \"to\": \"fcwoVd13u78:APA91bFf1P754jZsB5RPibTp5cZ_q0dQpnOra7UXT3DwD1XxyILBdJF6--9lb5uFFXsmtWn8wJiigd1HIm73JTkeIWbrbDNdU5BGNCe0X2FJ6scXiPDid-VRBKeLySt_70lf_nRBQflj\",\n" +
                    "    \"data\": {\n" +
                    "      \"message\": \"This is a GCM Topic Message!\",\n" +
                    "     }\n" +
                    "  }";
            Log.w("custom", testString);
            return testString.getBytes();
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String,String> params = new HashMap<String, String>();
            params.put("Authorization", "key=AIzaSyDXPdDTOWD3NQeGTbPhLR4ZQBCwVwomxhs");
            params.put("Content-Type", "application/json");
            return params;
        }
    };
    Volley.newRequestQueue(context).add(postRequest);
}

}



