package com.example.weerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class EarthquakesWorldwideActivity extends AppCompatActivity {
    private final String XML_WORLDWIDE_EARTHQUAKES_DATA = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=100";
    private Button backButton;
    private LinearLayout scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquakes_worldwide);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backButton = findViewById(R.id.earthquakesWWBackButton);
        scrollView = findViewById(R.id.wwScrollView).findViewById(R.id.wwSvLayout);

        backButton.setOnClickListener((view) -> onBackPressed());

        Calendar cal = new GregorianCalendar();
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        String urlString = String.format("%s&starttime=%s&endtime=%s", XML_WORLDWIDE_EARTHQUAKES_DATA, startDate, endDate);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                urlString,
                (responses) -> {

                    for (String response : responses.split("\n")) {
                        try {
                            addJsonEarthquakesToView(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                (error) -> {
                    error.printStackTrace();
                }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private static Document parseXmlFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(new InputSource(new StringReader(xml)));
        document.getDocumentElement().normalize();

        return document;
    }

    private void addXmlEarthquakesToView(String xml) throws Exception {
        Document document = parseXmlFromString(xml);

        // Get all earthquakes
        NodeList nList = document.getElementsByTagName("event");
        System.out.println(nList.getLength());
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                String title = eElement.getElementsByTagName("description").item(1).getTextContent();

                Element info = (Element) eElement.getElementsByTagName("origin").item(0);
                String dateTime = info.getElementsByTagName("time").item(0).getTextContent();
                String lat = info.getElementsByTagName("latitude").item(0).getTextContent();
                String lon = info.getElementsByTagName("longitude").item(0).getTextContent();
                String depth = info.getElementsByTagName("depth").item(0).getTextContent();

                Element quality = (Element) info.getElementsByTagName("quality").item(0);
                String usedPhaseCount = quality.getChildNodes().item(0).getTextContent();
                String usedStationCount = quality.getChildNodes().item(1).getTextContent();
                String standardError = quality.getChildNodes().item(2).getTextContent();
                String azimuthalGap = quality.getChildNodes().item(3).getTextContent();
                String minimumDistance = quality.getChildNodes().item(4).getTextContent();

                Element magInfo = (Element) eElement.getElementsByTagName("magnitude").item(0);
                String mag = magInfo.getElementsByTagName("mag").item(0).getTextContent();
                String magType = magInfo.getElementsByTagName("type").item(0).getTextContent();

                TextView tv = new TextView(this);
                tv.setText(String.format("%d. %s", i + 1, title));
                tv.setText(String.format("%s\nDatum: %s", tv.getText(), dateTime));
                tv.setText(String.format("%s\nCoördinaten: %s, %s", tv.getText(), lat, lon));
                tv.setText(String.format("%s\nDiepte: %s", tv.getText(), depth));
                tv.setText(String.format("%s\nMomentmagnitudeschaal: %s %s", tv.getText(), mag, magType));
                tv.setText(String.format("%s\nUsed phase count: %s", tv.getText(), usedPhaseCount));
                tv.setText(String.format("%s\nUsed station count: %s", tv.getText(), usedStationCount));
                tv.setText(String.format("%s\nStandard error: %s", tv.getText(), standardError));
                tv.setText(String.format("%s\nAzimuthal gap: %s", tv.getText(), azimuthalGap));
                tv.setText(String.format("%s\nMinimum distance: %s", tv.getText(), minimumDistance));

                tv.setTextIsSelectable(true);

                scrollView.addView(tv);
            }
        }
    }

    private void addJsonEarthquakesToView(String response) throws Exception {
        JSONObject jsonResponse = new JSONObject(response);
        System.out.println(jsonResponse);

        scrollView.removeView(findViewById(R.id.loading));

        JSONObject properties = jsonResponse.getJSONObject("properties");
        String place = properties.getString("place");
        String time = properties.getString("time");
        String url = properties.getString("url");
        String status = properties.getString("status");
        String nst = properties.getString("nst");
        String dmin = properties.getString("dmin");
        String rms = properties.getString("rms");
        String gap = properties.getString("gap");
        String mag = properties.getString("mag");
        String magType = properties.getString("magType");

        JSONArray coordinates = jsonResponse.getJSONObject("geometry").getJSONArray("coordinates");
        String coords1 = coordinates.getString(0);
        String coords2 = coordinates.getString(1);
        String coords3 = coordinates.getString(2);

        TextView tv = new TextView(this);
        tv.setText(String.format("%s", place));
        tv.setText(String.format("%s\nDatum (Unix timestamp): %s", tv.getText(), time));
        tv.setText(String.format("%s\nNst: %s", tv.getText(), nst));
        tv.setText(String.format("%s\nDmin: %s", tv.getText(), dmin));
        tv.setText(String.format("%s\nRms: %s", tv.getText(), rms));
        tv.setText(String.format("%s\nGap: %s", tv.getText(), gap));
        tv.setText(String.format("%s\nMagnitude: %s %s", tv.getText(), mag, magType));
        tv.setText(String.format("%s\nCoordinates: %s %s %s", tv.getText(), coords1, coords2, coords3));
        tv.setText(String.format("%s\nStatus: %s", tv.getText(), status));
        tv.setText(String.format("%s\nLink: %s\n", tv.getText(), url));

        tv.setTextIsSelectable(true);
        Linkify.addLinks(tv, Linkify.WEB_URLS);

        scrollView.addView(tv);
    }
}