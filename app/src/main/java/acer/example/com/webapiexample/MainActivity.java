package acer.example.com.webapiexample;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
//to import a volley library we included it in gradle file build.gradle
public class MainActivity extends AppCompatActivity
{
    String apiKey = "da3258d6795c1682c86dc616bc69f381";
    ProgressBar pb;
    TextView tvWeatherDetails;
    EditText etCity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = findViewById(R.id.pb1);
        pb.setVisibility(View.GONE);
        //getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.button_style));
        getSupportActionBar().setIcon(R.drawable.ic_launcher_background);

    }

    public void btn1_Click(View v)
    {
        etCity = findViewById(R.id.etCity);
        String city = etCity.getText().toString();
        callApi("http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey);
    }

    private void callApi(String url)
    {
        if(!connectionAvailable())
        {
            Toast.makeText(this, "CallApi->Connection not avilable", Toast.LENGTH_SHORT).show();
            return;
        }

        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                displayResult(response);
                pb.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Response.Listener fired", Toast.LENGTH_SHORT).show();
            }
        };
        Response.ErrorListener failureListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                pb.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Response.ErrorListener fired", Toast.LENGTH_SHORT).show();
            }
        };

        //jsonRequest parametre in below method is null for GET method, but if POST method is used then we have to make
        //an json object containing all the things to be posted and then add up that json object into null wala parametre
        //alternatively we can store those values in an hashmap(key,value) and then convert the hashmap into json object.
        JsonObjectRequest apiReq = new JsonObjectRequest(Request.Method.GET,url,null,successListener,failureListener);
        //There is a compulsion to create a request queue and then add the JsonObjectRequest object into it.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(apiReq);
        pb.setVisibility(View.VISIBLE);
    }

    public boolean connectionAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo ()!=null); //We had to add 3 permissions in manifest file.
    }

    //API returns a JSON Object so to access the results that API gives us, we access it by the JSON Oject
    private void displayResult(JSONObject jObj)
    {
        try
        {
            String output = "City :" + jObj.getString("name");

            JSONObject coordobj = jObj.getJSONObject("coord");
            output += "\nLongitude ="+coordobj.getString("lon");
            output += "\nLatitude ="+coordobj.getString("lat");

            JSONObject mainObj = jObj.getJSONObject("main");
            output += "\nHumidity ="+mainObj.getString("humidity");
            //decimal wala object was created to display the subtracted answer in .2f form
            java.text.DecimalFormat decimal = new java.text.DecimalFormat("#.##");
            output += "\nMax Temp ="+decimal.format(mainObj.getLong("temp_max")-272.15);
            output += "\nMin Temp ="+decimal.format(mainObj.getLong("temp_min")-272.15);
            output += "\nCurrent Temp ="+decimal.format(mainObj.getLong("temp")-272.15);
            tvWeatherDetails = (TextView) findViewById(R.id.tvWeatherDetails);
            tvWeatherDetails.setText(output);
        }
        catch(JSONException e)
        {
            Toast.makeText(this, "Unexpcted Shit in displayResult()", Toast.LENGTH_SHORT).show();
        }
    }

    LocationManager lm;
    LocationListener locationListener;

    public void btn2_Click(View v)
    {
        try
        {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location)
                {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    String url = String.valueOf("http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid="+apiKey);
                    callApi(url);
                    lm.removeUpdates(locationListener);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle)
                {

                }

                @Override
                public void onProviderEnabled(String s)
                {

                }

                @Override
                public void onProviderDisabled(String s)
                {
                    Toast.makeText(MainActivity.this, s+" Enable locatin from settings for app", Toast.LENGTH_SHORT).show();
                }
            };
            lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }
        catch (SecurityException e)
        {

        }
    }
}
