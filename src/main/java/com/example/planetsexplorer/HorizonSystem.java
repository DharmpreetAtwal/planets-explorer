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
    public static JSONObject getBody(String id, boolean objData, boolean makeEphem) throws Exception {
        String strObjData = "NO";
        String strMakeEphem = "NO";

        if(objData) strObjData = "YES";
        if(makeEphem) strMakeEphem = "YES";

        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='" + id +
                "'&OBJ_DATA='" + strObjData +
                "'&MAKE_EPHEM='" + strMakeEphem + "'";
        String planetStrJSON = executeGet(urlQuery).toString();

        try{
            JSONObject planetJSON = new JSONObject(planetStrJSON);
            String resultStr = (String) planetJSON.get("result");

            JSONObject planetInfo = new JSONObject();
            String siderealPeriod = extractSiderealOrbPeriod(resultStr);
            planetInfo.put("siderealOrbitDays", Float.parseFloat(siderealPeriod));

            String meanRadKM = extractVolMeanRadiusKM(resultStr);
            planetInfo.put("meanRadKM", Float.parseFloat(meanRadKM));

            String siderealDayPeriod = extractSiderealDayPeriod(resultStr);
            if(!siderealDayPeriod.equals("0")) {
                float siderealDayRadSec = Float.parseFloat(siderealDayPeriod);
                float siderealDaySec = (float) ((2*Math.PI) / siderealDayRadSec);
                float siderealDayHr = (siderealDaySec / 60) / 60;
                planetInfo.put("siderealDayHr", siderealDayHr);
            } else { planetInfo.put("siderealDayHr", 0); }

            String obliquityToOrbit = extractObliquityToOrbit(resultStr);
            planetInfo.put("obliquityToOrbitDeg", Float.parseFloat(obliquityToOrbit));

//            System.out.println(planetInfo);
            return planetInfo;
        } catch (JSONException err) {
            System.err.println(err);
            return null;
        }
    }

    private static StringBuilder executeGet(String urlDatabase) throws Exception {
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

        return results;
    }

    private static String extractVolMeanRadiusKM(String result) {
        // This pattern is meant to match 'mean radius, km   =  1.11'
        // or 'Mean Radius (km) =  1.11'
        Pattern radiusPattern = Pattern.compile(Pattern.quote("mean radius") + ",? \\(?km\\)?\\s+=\\s+\\d*\\.?\\d+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = radiusPattern.matcher(result);

        if(matcher.find()) {
//            System.out.println(matcher.group());
            return extractLastNumber(matcher.group());
        } else {
            System.err.println("Could not find 'mean radius'");
            return "0";
        }
    }

    private static String extractSiderealOrbPeriod(String result) {
        // This regex matches Sidereal orb. per.    =  0.2408467 y
        // or Sidereal orb. per., y =   0.61519726
        // or Sidereal orb period  = 1.0000174 y
        Pattern sideOrbPattern = Pattern.compile("Sidereal orb(it)?\\.? per(iod)?\\.?(, y\\s+=\\s+\\d*\\.?\\d+\\s+|\\s+=\\s+\\d*\\.?\\d+\\s+y)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = sideOrbPattern.matcher(result);

        if(matcher.find()) {
//            System.out.println(matcher.group());
            return extractLastNumber(matcher.group());
        } else {
            System.err.println("Could not find 'Sidereal Orb Period'");
            return "0";
        }
    }

    private static String extractSiderealDayPeriod(String result) {
        // Matches rot. rate (rad/s)= 0.00   Rot. Rate (rad/s)= -0.00, rot. rate, rad/s =  0.00
        Pattern sideDayPattern = Pattern.compile("rot. rat(e|,|e,) \\(?rad/s\\)?\\s*=\\s*-?\\d*\\.?\\d+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = sideDayPattern.matcher(result);

        if(matcher.find()) {
//            System.out.println(matcher.group());
            return extractLastNumber(matcher.group());
        } else {
            System.err.println("Could not find sidereal day");
            return "0";
        }
    }

    private static String extractObliquityToOrbit(String result) {
        Pattern obliquityOrbitPattern = Pattern.compile("Obliquity to orbit(, deg|\\[1])?\\s*=\\s*\\d*\\.?[\\d' +/-]*\\.\\d*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = obliquityOrbitPattern.matcher(result);

        if(matcher.find()) {
//            System.out.println(matcher.group());
//            System.out.println(extractLastNumber(matcher.group()));
            return extractLastNumber(matcher.group());
        } else {
            System.err.println("Could not find Obliquity to orbit");
            return "0";
        }
    }

    private static String extractLastNumber(String strEndNumber) {
        Pattern radiusValue = Pattern.compile("-?\\d*\\.?\\d+");
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
