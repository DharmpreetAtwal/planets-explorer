package org.example.planetsexplorer;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.planetsexplorer.celestial.Moon;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static helper class that handles the back-end HTTP requests to NASA's system data and
 * ephemeris computation service, Horizon System. Read about the system and it's API here:
 * <a href="https://ssd.jpl.nasa.gov/horizons/">...</a>
 *
 * <p> This class can can execute HTTP requests which return a JSONObject that contains
 * data about a celestial's physical parameters, or it's ephemeris path.
 *
 * <p>  The database is incomplete. Some celestial objects don't have a recorded radius or
 * obliquity
 *
 * @author Dharmpreet Atwal
 */
public final class HorizonSystem {
    /**
     * Don't let this class be instantiated
     */
    private HorizonSystem() {}

    /**
     * A global counter that controls which part of the ephemeris data is displayed
     */
    public static int ephemerisIndex = 0;

    /**
     * A constant that determines the scale of a distances or radius
     */
    public static final int pixelKmScale = 100;

    /**
     * A lookup table that stores the mapping between a celestial's database id and name
     */
    private static final HashMap<String, String> idNameMap = new HashMap<>(100);

    /**
     * A reverse lookup table of idNameMap
     * @see HorizonSystem#idNameMap
     */
    private static final HashMap<String, String> nameIdMap = new HashMap<>(100);

    /**
     * A lookup table that stores the mapping between a celestial's database id and IAU designation
     */
    private static final HashMap<String, String> idDesignationMap = new HashMap<>(100);

    /**
     * A reverse lookup table of idDesignationMap
     * @see HorizonSystem#idDesignationMap
     */
    private static final HashMap<String, String> designationIdMap = new HashMap<>(100);

    /**
     * A lookup table that stores the mapping between a celestial's database id and alias
     */
    private static final HashMap<String, String> idAliasMap = new HashMap<>(100);

    /**
     * Performs a GET request to the HorizonSystem database.
     *
     * @param urlDatabase The URL for the HTTP request
     * @return A {@link StringBuilder} representation of a {@link JSONObject}
     * @throws IOException if there was an error connecting to the database or executing the GET request
     */
    private static StringBuilder executeGet(String urlDatabase) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlDatabase);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            for(char letter; (letter = (char)reader.read()) != (char)-1;)
                result.append(letter);
        }

        return result;
    }

    /**
     * Queries the database to get the object data of a celestial. Converts the StringBuilder
     * representation into a JSONObject, parses the result attribute, and extracts the
     * relevant information.
     *
     * <p> The database is incomplete and may not contain the needed information. In this case, a
     * default value is assigned.
     *
     * @param id The database id of the celestial
     * @return A {@link JSONObject} containing {@code siderealOrbitDays}, {@code siderealDayHr},
     * {@code obliquityToOrbitDeg} {@code meanRadKM}
     */
    public static JSONObject getBody(String id) {
        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='" + id +
                "'&OBJ_DATA='" + "YES" +
                "'&MAKE_EPHEM='" + "NO" + "'";
        String planetStrJSON;
        try {
            planetStrJSON = executeGet(urlQuery).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try{
            JSONObject planetJSON = new JSONObject(planetStrJSON);
            String resultStr = (String) planetJSON.get("result");

            JSONObject planetInfo = new JSONObject();
            String siderealPeriod = extractSiderealOrbPeriod(resultStr, id);
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

            String meanRadKM = extractVolMeanRadiusKM(resultStr, id);
            planetInfo.put("meanRadKM", Float.parseFloat(meanRadKM));

            return planetInfo;
        } catch (JSONException err) {
            System.err.println(err);
            return null;
        }
    }

    /**
     * Queries the database to get the ephemeris position of the target celestial relative to a center
     * celestial.
     *
     * @param id The database id of the target celestial
     * @param centerId The database id of the celestial from which the position of the target is calculated
     * @param startTime The date-timestamp start of the ephemeris range in format: "YYYY-MM-DD HH:MM"
     * @param stopTime The date-timestamp stop of the ephemeris range in format: "YYYY-MM-DD HH:MM"
     * @param stepSize The time-based increment in between each sequential point in the ephemeris data
     * @return An {@code ArrayList<JSONObject>} where each JSONObject contains the x, y, z componenets of the
     * displacement vector, and the vx, vy, vz components of the velocity
     * @throws Exception if the returned data doesn't contain any of the required components
     */
    public static ArrayList<JSONObject> getEphemeris(String id, String centerId, String startTime, String stopTime, StepSize stepSize) throws Exception {
        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='" + id +
                "'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='VECTORS'&VEC_TABLE='2'&CENTER='@"+  centerId +
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
                return extractVectorsCSV(matcher.group());
            } else {
                System.err.println("No CSV found");
                return null;
            }
        } catch(JSONException err) {
            System.err.println(err);
            return null;
        }
    }

    /**
     * Queries the database to get the id, name, designation, and alias of every recorded body in the
     * database. Stores these values in static lookup tables.
     *
     * <p> After initializing the lookup tables, this method calls {@code Moon.initializeMoonInfo()},
     * which initializes the Moon specific lookup tables.
     *
     * @see HorizonSystem#idNameMap
     * @see HorizonSystem#idDesignationMap
     * @see HorizonSystem#idAliasMap
     */
    public static void initializeNameIDLookup() {
        String urlQuery = "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND=%27*%27";
        String bodyNameIDJSON = null;
        try {
            bodyNameIDJSON = executeGet(urlQuery).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            JSONObject bodyNameID = new JSONObject(bodyNameIDJSON);
            BufferedReader reader =new BufferedReader(new StringReader((String) bodyNameID.get("result")));
            String line;

            while((line = reader.readLine()) != null) {
                if(line.length() > 20) {
                    Pattern idPattern = Pattern.compile("^[\\d\\s-]+$");
                    String id = line.substring(0, 11);
                    Matcher idMatcher = idPattern.matcher(id);
                    if(idMatcher.find()) {
                        id = removeSpaces(id);
                        String name = removeSpaces(line.substring(11, 46));
                        String designation = removeSpaces(line.substring(46, 59));
                        String alias = removeSpaces(line.substring(59, 78));

                        idNameMap.put(id, name);
                        nameIdMap.put(name, id);
                        idDesignationMap.put(id, designation);
                        designationIdMap.put(designation, id);
                        idAliasMap.put(id, alias);
                    }
                }
            }
        } catch(JSONException | IOException err) {
            System.err.println(err);
        }

        Moon.initializeMoonInfo();
    }

    /**
     * A private helper method that removes unnecessary spaces. Helps to ensure consistent spaces in strings
     * used as keys in the lookup tables.
     *
     * @param str The string to remove unnecessary from
     * @return The string with the extra spaces removed.
     */
    private static String removeSpaces(String str) {
        String result = str.replaceAll("\\s\\s+", "");
        result = result.replaceAll("^\\s+", "");
        result = result.replaceAll("\\s+$", "");
        return result;
    }

    /**
     * A helper method that extracts the displacement and velocity components stored in the String
     * representation of a CSV file.
     * @param strCSV The String representation of a CSV file.
     * @return An {@code ArrayList<JSONObject>} where each JSONObject contains the x, y, z components of the
     * displacement vector, and the vx, vy, vz components of the velocity
     */
    private static ArrayList<JSONObject> extractVectorsCSV(String strCSV) {
        CSVReader reader = new CSVReader(new StringReader(strCSV));
        List<String[]> rows = null;

        try {
            rows = reader.readAll();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        ArrayList<JSONObject> ephemData = new ArrayList<>();

        for(String[] row: rows) {
            String x = "";
            String y = "";
            String z = "";

            String vx = "";
            String vy = "";
            String vz = "";

            int i=0;
            for (String cell : row) {
                if(i==2) x = cell;
                if(i==3) y = cell;
                if(i==4) z = cell;
                if(i==5) vx= cell;
                if(i==6) vy= cell;
                if(i==7) vz= cell;
                i++;
            }

            if(!x.isEmpty() && !y.isEmpty() && !z.isEmpty() && !vx.isEmpty() && !vy.isEmpty() && !vz.isEmpty()) {
                JSONObject data = new JSONObject();
                data.put("x", Float.parseFloat(x));
                data.put("y", Float.parseFloat(y));
                data.put("z", Float.parseFloat(z));
                data.put("vx", Float.parseFloat(vx));
                data.put("vy", Float.parseFloat(vy));
                data.put("vz", Float.parseFloat(vz));

                ephemData.add(data);
            }
        }

        return ephemData;
    }

    /**
     * A method that queries the database for the oldest point in time for the ephemeris position of
     * a given spacecraft. The returned timestamp has 5 min extra onto the actual start time.
     * @param dbID The database id of the spacecraft
     * @return A date-timestamp of format "YYYY-MM-DD HH:MM"
     */
    public static String getSpacecraftStartTimestamp(String dbID) {
        String timestamp = "";
        try {
            StringBuilder resultJSON = executeGet(
                    "https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND=%27" +
                    dbID +"%27&OBJ_DATA=%27NO%27&MAKE_EPHEM=%27YES%27&CENTER=%27@399%27&START_TIME=%271000-01-01%27");
            JSONObject startTimeJSON = new JSONObject(resultJSON.toString());
            String result = startTimeJSON.getString("result");

            Pattern timestampPattern = Pattern.compile("(?<=prior to A.D. )\\d{4}-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)-\\d{2} \\d{2}:\\d{2}:\\d{2}");
            Matcher timestampPatternMatcher = timestampPattern.matcher(result);

            if(timestampPatternMatcher.find()){
                timestamp = timestampPatternMatcher.group();

                String time = timestamp.substring(12);
                time = addMinutes(time, 5);

                timestamp = timestamp.substring(0, 12) + time;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return timestamp;
    }

    /**
     * A method that queries the database for the furthest point in time for the ephemeris position of
     * a given spacecraft. The returned timestamp has 5 min less than actual stop time.
     * @param dbID The database id of the spacecraft
     * @param startTime The oldest point in time for ephemeris data of the given spacecraft
     * @return A date-timestamp of format "YYYY-MM-DD HH:MM"
     */
    public static String getSpacecraftStopTimestamp(String dbID, String startTime) {
        String timestamp = "";
        try {
            StringBuilder resultJSON = executeGet("https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND=%27" + dbID +"%27&OBJ_DATA=%27NO%27&MAKE_EPHEM=%27YES%27&CENTER=%27@399%27&START_TIME=%27" + startTime + "%27&STOP_TIME=%279999-01-01%27");
            JSONObject startTimeJSON = new JSONObject(resultJSON.toString());
            String result = startTimeJSON.getString("result");

            Pattern timestampPattern = Pattern.compile("(?<=after A.D. )\\d{4}-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)-\\d{2} \\d{2}:\\d{2}:\\d{2}");
            Matcher timestampPatternMatcher = timestampPattern.matcher(result);

            if(timestampPatternMatcher.find()){
                timestamp = timestampPatternMatcher.group();

                String time = timestamp.substring(12);
                time = addMinutes(time, -5);

                timestamp = timestamp.substring(0, 12) + time;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return timestamp;
    }

    /**
     * A helper method that adds minutes onto the String representation of a timestamp
     * @param timeString The timestamp to be altered.
     * @param minutes The amount of minutes to be added
     * @return A new timestamp with the minutes added on of format HH:mm:ss
     */
    private static String addMinutes(String timeString, int minutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.parse(timeString, formatter);
        LocalTime newTime = localTime.plusMinutes(minutes);
        return newTime.format(formatter);
    }

    private static String extractVolMeanRadiusKM(String result, String id) {
        // This pattern is meant to match 'mean radius, km   =  1.11'
        // or 'Mean Radius (km) =  1.11'
        Pattern radiusPattern = Pattern.compile("(mean )?radius,? \\(?km\\)?\\s+=\\s+\\d*\\.?\\d+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = radiusPattern.matcher(result);

        if(matcher.find()) {
//            System.out.println(matcher.group());
            return extractLastNumber(matcher.group());
        } else {
            String radius = Moon.idRadiusMap.get(id);
            if(radius == null) {
                System.err.println("Could not find 'mean radius'");
                return "1";
            }
            return radius;
        }
    }

    private static String extractSiderealOrbPeriod(String result, String id) {
        // This regex matches Sidereal orb. per.    =  0.2408467 y
        // or Sidereal orb. per., y =   0.61519726
        // or Sidereal orb period  = 1.0000174 y
        Pattern sideOrbPattern = Pattern.compile("Sidereal orb(it)?\\.? per(iod)?\\.?(, y\\s+=\\s+\\d*\\.?\\d+\\s+|\\s+=\\s+\\d*\\.?\\d+\\s+y)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = sideOrbPattern.matcher(result);

        if(matcher.find()) {
            return extractLastNumber(matcher.group());
        } else {
            Pattern sideOrbPatternDays = Pattern.compile("orbit(al)? period\\s*[=~]\\s+\\d*\\.?\\d+\\s* d", Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = sideOrbPatternDays.matcher(result);

            if(matcher1.find()) {
                float lastNumber = Float.parseFloat(Objects.requireNonNull(extractLastNumber(matcher1.group())));
                lastNumber = lastNumber / 365.25f;
                return String.valueOf(lastNumber);
            } else if(Moon.idOrbitDaysMap.containsKey(id)) {
                float sidereal = Float.parseFloat(Moon.idOrbitDaysMap.get(id));
                sidereal = sidereal / 365.25f;

                if(sidereal == 0) System.err.println("Could not find 'Sidereal Orb Period' " + id);
                return String.valueOf(sidereal);
            }

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

    public static String idToName(String id) {
        return idNameMap.get(id);
    }

    public static Set<String> getIdNameMapKeySet() {
        return idNameMap.keySet();
    }

    public static String nameToID(String name) {
        return nameIdMap.get(name);
    }

    public static String designationToId(String designation) {
        return designationIdMap.get(designation);
    }
}