package com.example.planetsexplorer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HorizonSystem {
    public static JSONObject executeGet(String urlDatabase) throws Exception {
        StringBuilder results = new StringBuilder();
        URL url = new URL(urlDatabase);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))
        ) {
            for(char letter; (letter = (char)reader.read()) != (char)-1;) {
                results.append(letter);
            }
        }

        try{
            JSONObject json = new JSONObject(results.toString());
            String resStr = (String) json.get("result");

            JSONObject planetInfo = new JSONObject();

            String siderealPeriod = extractSiderealOrbPeriod(resStr);
            if(siderealPeriod != null) { planetInfo.put("siderealPeriod", Float.parseFloat(siderealPeriod)); }
            else { planetInfo.append("siderealPeriod", null); }

            String meanRadKM = extractVolMeanRadiusKM(resStr);
            if(meanRadKM != null) { planetInfo.put("meanRadKM", Float.parseFloat(meanRadKM)); }

            System.out.println(planetInfo);
            return planetInfo;
        } catch (JSONException err) {
            System.err.println(err);
            return null;
        }
    }

    private static String extractVolMeanRadiusKM(String result) {
        // This pattern is meant to match 'mean radius, km   =  1.11' or 'Mean Radius (km) =  1.11'
        Pattern radiusPattern = Pattern.compile(Pattern.quote("mean radius") + ",? \\(?km\\)?\\s+=\\s+\\d*\\.?\\d+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = radiusPattern.matcher(result);

        if(matcher.find()) {
//            System.out.println(matcher.group());
            return extractLastNumber(matcher.group());
        } else {
            System.err.println("Could not find 'mean radius'");
            return null;
        }
    }

    private static String extractSiderealOrbPeriod(String results) {
//        Pattern sideOrbPattern = Pattern.compile("Sidereal orb(it)?\\.? per(iod)?\\.?(, d)?\\s+=\\s+\\d*\\.?\\d+\\s+d", Pattern.CASE_INSENSITIVE);
        Pattern sideOrbPattern = Pattern.compile("Sidereal orb(it)?\\.? per(iod)?\\.?(, y\\s+=\\s+\\d*\\.?\\d+\\s+|\\s+=\\s+\\d*\\.?\\d+\\s+y)", Pattern.CASE_INSENSITIVE);

        Matcher matcher = sideOrbPattern.matcher(results);

        if(matcher.find()) {
//            System.out.println(matcher.group());
            return extractLastNumber(matcher.group());
        } else {
            System.err.println("Could not find 'Sidereal Orb Period'");
            return null;
        }
    }


    private static String extractLastNumber(String strEndNumber) {
        Pattern radiusValue = Pattern.compile("\\d*\\.?\\d+");
        Matcher radiusMatcher = radiusValue.matcher(strEndNumber);

        if(radiusMatcher.find()) {
//            System.out.println(radiusMatcher.group());
            return radiusMatcher.group();
        } else {
            System.err.println("Found 'string', but no ending value");
            return null;
        }
    }
}
