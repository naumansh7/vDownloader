package com.nauman.vdownloader.Tasks;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nauman.vdownloader.R;
import com.nauman.vdownloader.constants.iConstants;
import com.nauman.vdownloader.utils.JSONParser;
import com.nauman.vdownloader.utils.iUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.nauman.vdownloader.constants.iConstants.DISABLE_DOWNLOADING;
import static com.nauman.vdownloader.constants.iConstants.DOWNLOADING_MSG;
import static com.nauman.vdownloader.constants.iConstants.URL_NOT_SUPPORTED;
import static com.nauman.vdownloader.constants.iConstants.WEB_DISABLE;


/**
 * Created by Nauman Shafqat on 4/14/2017.
 */

public class GenerateLink implements iConstants {

    public static Context context;
    public static ProgressDialog processDialog;
    public static Dialog dialog;
    public static int errorMsg=1;
    public static void Start(Context myContext , String url , String title){
        errorMsg=1;
            for (int i = 0; i < DISABLE_DOWNLOADING.length; i++) {

            if (url.contains(DISABLE_DOWNLOADING[i])) {
                errorMsg=0;
            }
        }
        context=myContext;

        if(errorMsg==1) {
            processDialog = new ProgressDialog(myContext);
            processDialog.setMessage(DOWNLOADING_MSG);
            processDialog.show();

            new GetUrls().execute(String.format(API_URL, url));
        }else{
            iUtils.ShowToast(context,WEB_DISABLE);

        }

    }

    public static class GetUrls extends AsyncTask<String, Void, JSONObject> {
        JSONParser FJson = new JSONParser();
        @Override
        protected JSONObject doInBackground(String... urls) {
            return FJson.getOJSONFromUrl(urls[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            Log.e("ERROR", result.toString());
            processDialog.dismiss();
            String error = "";
            try {
                error = result.getString("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (error.contains("not-supported") || error.contains("no_media_found") || error.contains("miss")) {
                iUtils.ShowToast(context, URL_NOT_SUPPORTED);
            } else {
                try {
                    GenerateUI(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public static void GenerateUI(JSONObject result) throws JSONException {

        //String thumbnail = result.getString("thumbnail");
        final String title = result.getString("title");
        final  JSONArray urls = result.getJSONArray("urls");

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.download_dialog);
        ListView linksList = (ListView)dialog.findViewById(R.id.ListView);

        String[] listItems = new String[urls.length()];

        String label="";
        for(int i = 0; i < urls.length(); i++){
            JSONObject list = urls.getJSONObject(i);

            label=list.getString("label");
            if(label.contains("(audio - no video) webm")){
                label=label.replace("(audio - no video) webm","mp3");
            }
            listItems[i] = label;

        }
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, listItems);
        linksList.setAdapter(adapter);

        linksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String ext = "";
                try {
                    final JSONObject data = urls.getJSONObject(position);

                    if(data.getString("label").contains(" mp4")){
                        ext=".mp4";
                    }else if(data.getString("label").contains(" mp3")){
                        ext=".mp3";
                    }else if(data.getString("label").contains(" 360p - webm")){
                        ext=".webm";
                    }else if(data.getString("label").contains(" webm")){
                        ext=".mp3";
                    }else if(data.getString("label").contains(" m4a")){
                        ext=".m4a";
                    }else if(data.getString("label").contains(" 3gp")){
                        ext=".3gp";
                    }else if(data.getString("label").contains(" flv")){
                        ext=".flv";
                    }else{
                        ext=".mp4";
                    }

                    DownloadFile.Downloading(context,data.getString("id"),title,ext);
                        dialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }

}
