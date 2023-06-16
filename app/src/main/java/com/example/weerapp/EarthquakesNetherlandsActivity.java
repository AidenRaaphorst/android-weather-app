package com.example.weerapp;

import androidx.appcompat.app.AppCompatActivity;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class EarthquakesNetherlandsActivity extends AppCompatActivity {
    private static final String XML_EARTHQUAKE_DATA_URL = "https://cdn.knmi.nl/knmi/map/page/seismologie/GQuake_KNMI_RSS.xml";
    private Button backButton;
    private LinearLayout scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquakes_netherlands);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backButton = findViewById(R.id.earthquakesNBackButton);
        scrollView = findViewById(R.id.nScrollView).findViewById(R.id.nSvLayout);

        backButton.setOnClickListener((view) -> onBackPressed());

        // Get XML data
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                XML_EARTHQUAKE_DATA_URL,
                (response) -> {
                    try {
                        addEarthquakesToView(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                (error) -> {
                    System.out.println(error);
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

    private void addEarthquakesToView(String xml) throws Exception {
        Document document = parseXmlFromString(xml);

        scrollView.removeView(findViewById(R.id.loading));

        // Get all earthquakes
        NodeList nList = document.getElementsByTagName("item");
        for(int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                String title = eElement.getElementsByTagName("title").item(0).getTextContent().split(", ")[1];
                String[] description = eElement.getElementsByTagName("description").item(0).getTextContent().split(", ");
                String date = description[0];
                String time = description[1];
                String lat = description[2];
                String lon = description[3];
                String depth = description[4].split(" = ")[1];
                String m = description[5].split(" = ")[1];
                String place = description[6].split(" = ")[1];
                String author = description[7].split(" = ")[1];
                String type = description[8].split(" = ")[1];
                String analysis = description[9].split(" = ")[1];
                String link = eElement.getElementsByTagName("link").item(0).getTextContent();
                String guid = eElement.getElementsByTagName("guid").item(0).getTextContent();

                TextView tv1 = new TextView(this);
                tv1.setText(String.format("%d. %s", i+1, title));
                tv1.setText(String.format("%s\nDatum: %s %s", tv1.getText(), date, time));
                tv1.setText(String.format("%s\nCoördinaten: %s, %s", tv1.getText(), lat, lon));
                tv1.setText(String.format("%s\nDiepte: %s", tv1.getText(), depth));
                tv1.setText(String.format("%s\nMomentmagnitudeschaal: %s", tv1.getText(), m));
                tv1.setText(String.format("%s\nPlaats: %s", tv1.getText(), place));
                tv1.setText(String.format("%s\nAuteur: %s", tv1.getText(), author));
                tv1.setText(String.format("%s\nType: %s", tv1.getText(), type));
                tv1.setText(String.format("%s\nAnalyse: %s", tv1.getText(), analysis));
                tv1.setText(String.format("%s\nGUID: %s", tv1.getText(), guid));
                tv1.setText(String.format("%s\nLink: %s\n", tv1.getText(), link));

                tv1.setTextIsSelectable(true);
                Linkify.addLinks(tv1, Linkify.WEB_URLS);

                scrollView.addView(tv1);
            }
        }
    }
}