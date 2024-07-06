package org.example.planetsexplorer;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HorizonSystem {
    public static int empherisIndex = 0;
    private static String bodiesNameID = "";

    public static JSONObject getBody(String id) throws Exception {
        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='" + id +
                "'&OBJ_DATA='" + "YES" +
                "'&MAKE_EPHEM='" + "NO" + "'";
        String planetStrJSON = executeGet(urlQuery).toString();

        try{
            JSONObject planetJSON = new JSONObject(planetStrJSON);
            String resultStr = (String) planetJSON.get("result");

            JSONObject planetInfo = new JSONObject();
            String siderealPeriod = extractSiderealOrbPeriod(resultStr);
            planetInfo.put("siderealOrbitDays", Float.parseFloat(siderealPeriod));

            String siderealDayPeriod = extractSiderealDayPeriod(resultStr);
            if(!siderealDayPeriod.equals("0")) {
                float siderealDayRadSec = Float.parseFloat(siderealDayPeriod);
                float siderealDaySec = (float) ((2*Math.PI) / siderealDayRadSec);
                float siderealDayHr = (siderealDaySec / 60) / 60;
                planetInfo.put("siderealDayHr", siderealDayHr);
            } else { planetInfo.put("siderealDayHr", 0); }

            String obliquityToOrbit = extractObliquityToOrbit(resultStr);
            planetInfo.put("obliquityToOrbitDeg", Float.parseFloat(obliquityToOrbit));

            String meanRadKM = extractVolMeanRadiusKM(resultStr);
            planetInfo.put("meanRadKM", Float.parseFloat(meanRadKM));

//            System.out.println(planetInfo);
            return planetInfo;
        } catch (JSONException err) {
            System.err.println(err);
            return null;
        }
    }

    public static ArrayList<JSONObject> getEphemeris(String id, String centerId, String startTime, String stopTime, StepSize stepSize) throws Exception {
        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='" + id +
                "'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='ELEMENTS'&CENTER='"+  centerId +
                "'&CSV_FORMAT='YES'" +
                "&START_TIME='" + startTime +
                "'&STOP_TIME='" + stopTime +
                "'&STEP_SIZE='" + stepSize.toString() + "'";
        String ephemStrJSON = executeGet(urlQuery).toString();

        try {
            JSONObject ephemJSON = new JSONObject(ephemStrJSON);
            String ephemResult = (String) ephemJSON.get("result");

            // A regex that returns everything between the delimeters $$SOE -> $$EOE, with delimeters exlucded
            Pattern csvPattern = Pattern.compile("(?<=\\$\\$SOE)(.*?)(?=\\$\\$EOE)", Pattern.DOTALL);
            Matcher matcher = csvPattern.matcher(ephemResult);

            if(matcher.find()) {
                return extractCSV(matcher.group());
            } else {
                System.err.println("No CSV found");
                return null;
            }
        } catch(JSONException err) {
            System.err.println(err);
            return null;
        }
    }

    public static String idToName(String id) throws Exception {
        if(HorizonSystem.bodiesNameID.isEmpty()) HorizonSystem.initializeNameIDLookup();

        Pattern idNamePattern = Pattern.compile("(?<=" + id +") *[A-Za-z]* ", Pattern.CASE_INSENSITIVE);
        Matcher idNameMatcher = idNamePattern.matcher(HorizonSystem.bodiesNameID);
        if(idNameMatcher.find()) {
            return idNameMatcher.group().replaceAll("\\s+", "");
        } else {
            System.err.println("Invalid id: " + id);
            return null;
        }
    }

    private static void initializeNameIDLookup() throws Exception {
        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND=%27*%27";
        String bodyNameIDJSON = executeGet(urlQuery).toString();

        try {
            JSONObject bodyNameID = new JSONObject(bodyNameIDJSON);
            HorizonSystem.bodiesNameID = (String) bodyNameID.get("result");
        } catch(JSONException err) {
            System.err.println(err);
        }
    }

    private static ArrayList<JSONObject> extractCSV(String strCSV) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new StringReader(strCSV));
        List<String[]> rows = reader.readAll();
        ArrayList<JSONObject> ephemData = new ArrayList<>();

        for (String[] row : rows) {
            String qr = "";
            String ma = "";
            String in = "";

            int i =0;
            for (String cell : row) {
                if(i==3) qr = cell;
                if(i==4) in = cell;
                if(i==9) ma = cell;
                i++;
            }

            if(!qr.isEmpty() && !ma.isEmpty()) {
                JSONObject data = new JSONObject();
                data.put("qr", Float.parseFloat(qr));
                data.put("ma", Float.parseFloat(ma));
                data.put("in", Float.parseFloat(in));
                ephemData.add(data);
            }
        }

        return ephemData;
    }


    private static StringBuilder executeGet(String urlDatabase) throws Exception {
//        System.out.println(urlDatabase);
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