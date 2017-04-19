/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.latindex;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;
import org.simmetrics.StringMetric;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.JaccardSimilarity;



/**
 *
 * @author bibliodigital
 */
public class Distance {

    // JDBC driver name and database URL
    String JDBC_DRIVER = "org.postgresql.Driver";
    String DB_URL = "jdbc:postgresql://";

    //  Database credentials
    String USER = "usr";
    String PASS = "amd";

    Connection conn = null;
    //Statement stmt = null;

    JsonObject config = null;

    
    
   


    public Distance() throws IOException, ClassNotFoundException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/config.cnf");
        //String readFile = readFile("./config.cnf", Charset.defaultCharset());
        String theString = IOUtils.toString(resourceAsStream, Charset.defaultCharset().toString());
        JsonParser parse = new JsonParser();
        config =  parse.parse(theString).getAsJsonObject();

        DB_URL = DB_URL + config.get("dbServer").getAsString() + "/" + config.get("dbSchema").getAsString();
        USER = config.get("dbUser").getAsString();
        PASS = config.get("dbPassword").getAsString();

    }

    String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public void close() throws SQLException {
        //conn.close();
    }

    /**
     * @param args the command line arguments
     */
    public synchronized double NWD__(List<String> a, List<String> b) throws ClassNotFoundException, SQLException, IOException {
    Class.forName("org.postgresql.Driver");
    conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Map<String, List<String>> map = new HashMap<>();
        List<String> Authors = new ArrayList();
        Authors.add("a1");
        Authors.add("a2");
        List<String> Endpoints = new ArrayList();
        Endpoints.add("e1");
        Endpoints.add("e2");
        Map<String, Double> Result = new HashMap<>();
        double avg = 0;
        double har = 0;
        double thres = 0.65;
        for (int i = 0; i < Authors.size(); i++) {
            for (int j = i + 1; j < Authors.size(); j++) {
                String a1 = Authors.get(i);
                String a2 = Authors.get(j);
                List<String> ka1 = null;
                List<String> ka2 = null;
                if (map.containsKey(a1)) {
                    ka1 = map.get(a1);
                } else {
                    ka1 = a;//consultado2R(a1, Endpoints.get(i));
                    //String t1_ = traductor(Joiner.on(" | ").join(ka1)).toLowerCase();
                    ka1 = traductor(ka1);//new LinkedList<String>(java.util.Arrays.asList(t1_.split("\\s\\|\\s")));
                    ka1 = clean(ka1);
                    ka1 = TopT(ka1, 4);//(int) (2.0 * Math.log(ka1.size())));
                    map.put(a1, ka1);
                }

                if (map.containsKey(a2)) {
                    ka2 = map.get(a2);
                } else {
                    ka2 = b;//consultado2R(a2, Endpoints.get(j));
                    //String t2_ = traductor(Joiner.on(" | ").join(ka2)).toLowerCase();
                    ka2 = traductor(ka2);//new LinkedList<String>(java.util.Arrays.asList(t2_.split("\\s\\|\\s")));
                    ka2 = clean(ka2);
                    ka2 = TopT(ka2, 4);//(int) (2.0 * Math.log(ka2.size())));
                    map.put(a2, ka2);
                }
                //System.out.println(ka1.size() + "," + ka2.size());

                double sum = 0;
                double num = 0;

                double sumMins = 0;
                double min = 1.2;

                for (String t1 : ka1) {
                    min = 1;
                    for (String t2 : ka2) {
                        String tt1 = t1;
                        String tt2 = t2;
                        double v = NGD(tt1, tt2);
                        if (v < min) {
                            min = v;
                        }
                        //System.out.println(tt1 + "," + tt2 + "=" + v);
                    }
                    sum += min;
                    num++;
                }
                double prom = sum / num;

                if (num == 0 && sum == 0) {
                    prom = 2;
                }
                //System.out.println(i + "," + j + "=" + prom);
                Result.put(i + "," + j, prom);

                if (avg == 0) {
                    avg = prom;
                } else {
                    avg = (avg + prom) / 2;
                }
                if (har == 0) {
                    har = prom;
                } else {
                    har = 2 / (1 / har + 1 / prom);
                }

            }
        }

        double r = 0;
        for (Map.Entry<String, Double> cc : Result.entrySet()) {
            r = cc.getValue();
        }

        conn.close();

        return r;
    }

    @Deprecated
    public synchronized double NWD(String uri1, String end1, String uri2, String end2, String quy) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(DB_URL, USER, PASS);

        Map<String, List<String>> map = new HashMap<>();

        List<String> Authors = new ArrayList();
        Authors.add(uri1);
        Authors.add(uri2);
        /*
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ACURIO_DEL_PINO__SANTIAGO");
        Authors.add("http://190.15.141.66:8899/puce/contribuyente/ACURIO_DEL_PINO__SANTIAGO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ACURIO_PAEZ__FAUSTO_DAVID");
        
        
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/CHUCHUCA__VICTOR");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/SAQUICELA__VICTOR");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/SAQUICELA_GALARZA__VICTOR_HUGO");
        Authors.add("http://190.15.141.66:8899/cedia/contribuyente/SAQUICELA__VICTOR");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/SAQUICELA__V");
        Authors.add("http://190.15.141.66:8899/puce/contribuyente/CEVALLOS__VICTOR");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/AREVALO__VICTOR");
        Authors.add("http://190.15.141.66:8899/puce/contribuyente/DEL_PINO__EMILIA");
        Authors.add("http://190.15.141.66:8899/uce/contribuyente/ACURIO_ACURIO__JAIME_NEPTALI");
        Authors.add("http://190.15.141.66:8899/uide/contribuyente/ACURIO_DEL_PINO__SANTIAGO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ESPINOZA__MAURICIO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ESPINOZA_ESPINOZA__JHONNY_MAURICIO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ASTUDILLO_ESPINOZA__CHRISTIAN_MAURICIO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ESPINOZA_MEJIA__JORGE_MAURICIO");
        Authors.add("http://190.15.141.66:8899/cedia/contribuyente/ESPINOZA__MAURICIO");

        Authors.add("http://190.15.141.66:8899/puce/contribuyente/ESPINOZA__OSWALDO");
        Authors.add("http://190.15.141.66:8899/puce/contribuyente/ESPINOZA_VITERI__OSWALDO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ESPINOZA_VEINTIMILLA__ANGEL_OSWALDO");
        Authors.add("http://190.15.141.66:8899/ucuenca/contribuyente/ENCALADA_ESPINOZA__OSWALDO_JAVIER");
         */
        List<String> Endpoints = new ArrayList();
        Endpoints.add(end1);
        Endpoints.add(end2);
        /*      Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.66:8893/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.66:8890/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.66:8893/myservice/query");
        
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.66:8893/myservice/query");
        Endpoints.add("http://190.15.141.66:8891/myservice/query");
        Endpoints.add("http://190.15.141.66:8895/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.66:8890/myservice/query");

        Endpoints.add("http://190.15.141.66:8893/myservice/query");
        Endpoints.add("http://190.15.141.66:8893/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
        Endpoints.add("http://190.15.141.102:8891/myservice/query");
         */
        Map<String, Double> Result = new HashMap<>();

        double avg = 0;
        double har = 0;

        double thres = 0.65;

        for (int i = 0; i < Authors.size(); i++) {
            for (int j = i + 1; j < Authors.size(); j++) {

                String a1 = Authors.get(i);
                String a2 = Authors.get(j);
                List<String> ka1 = null;
                List<String> ka2 = null;
                if (map.containsKey(a1)) {
                    ka1 = map.get(a1);
                } else {
                    ka1 = consultado2R(a1, Endpoints.get(i));
                    //String t1_ = traductor(Joiner.on(" | ").join(ka1)).toLowerCase();
                    ka1 = traductor(ka1);//new LinkedList<String>(java.util.Arrays.asList(t1_.split("\\s\\|\\s")));
                    ka1 = clean(ka1);
                    ka1 = TopT(ka1, (int) (2.0 * Math.log(ka1.size())));
                    map.put(a1, ka1);
                }

                if (map.containsKey(a2)) {
                    ka2 = map.get(a2);
                } else {
                    ka2 = consultado2R(a2, Endpoints.get(j));
                    //String t2_ = traductor(Joiner.on(" | ").join(ka2)).toLowerCase();
                    ka2 = traductor(ka2);//new LinkedList<String>(java.util.Arrays.asList(t2_.split("\\s\\|\\s")));
                    ka2 = clean(ka2);
                    ka2 = TopT(ka2, (int) (2.0 * Math.log(ka2.size())));
                    map.put(a2, ka2);
                }
                //System.out.println(ka1.size() + "," + ka2.size());

                double sum = 0;
                double num = 0;

                double sum2 = 0;

                for (String t1 : ka1) {
                    for (String t2 : ka2) {
                        num++;
                        String tt1 = t1;
                        String tt2 = t2;
                        double v = NGD(tt1, tt2);
                        sum += v;

                        //System.out.println(tt1 + "," + tt2 + "=" + v);
                    }
                }
                double prom = sum / num;

                if (num == 0 && sum == 0) {
                    prom = 2;
                }
                //System.out.println(i + "," + j + "=" + prom);
                Result.put(i + "," + j, prom);

                if (avg == 0) {
                    avg = prom;
                } else {
                    avg = (avg + prom) / 2;
                }
                if (har == 0) {
                    har = prom;
                } else {
                    har = 2 / (1 / har + 1 / prom);
                }

            }
        }

        double r = 0;
        for (Map.Entry<String, Double> cc : Result.entrySet()) {
            r = cc.getValue();
        }

        conn.close();

        return r;
    }

    public List<String> TopT(List<String> m, int n) throws IOException, SQLException {

        n = (n <= 0) ? 1 : n;
        if (m.size() == 1) {
            m.add(m.get(0));
        }
        Map<String, Double> Mapa = new HashMap();
        for (int i = 0; i < m.size(); i++) {
            for (int j = i + 1; j < m.size(); j++) {
                double v = NGD(m.get(i), m.get(j));
                //System.out.print(i+"/"+m.size()+"\t");

                if (Mapa.containsKey(m.get(i))) {
                    Mapa.put(m.get(i), Mapa.get(m.get(i)) + v);
                } else {
                    Mapa.put(m.get(i), v);
                }

                if (Mapa.containsKey(m.get(j))) {
                    Mapa.put(m.get(j), Mapa.get(m.get(j)) + v);
                } else {
                    Mapa.put(m.get(j), v);
                }
            }
        }
        Map<String, Double> sortByValue = sortByValue(Mapa);
        List<String> ls = new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList(sortByValue.keySet());
        ArrayList<Double> arrayList2 = new ArrayList(sortByValue.values());
        for (int i = 0; i < n; i++) {
            if (i < sortByValue.size()) {
                ls.add(arrayList.get(i));
                // System.out.println(arrayList.get(i)+"__"+arrayList2.get(i));
            }
        }
        return ls;
    }

    public List<String> consultado2R(String ent, String end) {

        List<String> consultado2 = null;
        boolean c = true;
        do {
            try {
                consultado2 = consultado2(ent, end);
                c = false;
            } catch (Exception w) {
                System.out.println(w + "Retry");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Distance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } while (c);

        return consultado2;
    }

    public List<String> consultado2(String ent, String end) {
        List<String> lista = new ArrayList();
        /*
         String consulta2 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                          + "PREFIX esdbpr: <http://es.dbpedia.org/resource/> "+ 
                           " PREFIX owl: <http://dbpedia.org/ontology/> "+
        "SELECT ?person WHERE{ "+
        "?person  rdf:type              owl:Scientist . "+
        "?person  owl:country  esdbpr:Ecuador.  }"; */

        String entidad = ent;
        String endpoint = end;

        String consulta = config.get("contextQuery").getAsString().replaceAll("\\|\\?\\|", entidad);

        Query query = QueryFactory.create(consulta);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

        // QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/fct/service", query);
        //System.out.println("Ejecutando consulta");
        //ResultSet resultado = qexec.execSelect();
        //System.out.println("Fin consulta");
        try {
            ResultSet rs = qexec.execSelect();

            while (rs.hasNext()) {
                QuerySolution soln = rs.nextSolution();
                //System.out.println(soln.getLiteral("d").getString());
                lista.add(soln.getLiteral("d").getString());
                // System.out.println ( "Val "+soln.getResource("d").getLocalName());

            }

            return lista;
// ResultSet results = qexec.execSelect();
//QuerySolution solucion = results.nextSolution();
//ResultSetFormatter.out(System.out, results);
        } catch (Exception e) {
            System.out.println("Verificar consulta, no existen datos para mostrar");
        } finally {
            qexec.close();

        }
        return lista;
    }

    private double NGD(String a, String b) throws IOException, SQLException {

        a = a.trim();
        b = b.trim();

        if (a.compareToIgnoreCase(b) == 0) {
            return 0;
        }

        //double n0 = getResultsCount(""+a+"");
        //double n1 = getResultsCount(""+b+"");
        //String c = ""+a+" "+b+"";
        double n0 = getResultsCount("\"" + a + "\"~10");
        double n1 = getResultsCount("\"" + b + "\"~10");
        String c = "\"" + a + " " + b + "\"~50";

        double n2 = getResultsCount(c);
        //double m = 5026040.0 * 590;

        double m = 5029469;

        double distance = 0;

        int Measure = 0;

        double l1 = Math.max(Math.log10(n0), Math.log10(n1)) - Math.log10(n2);
        double l2 = Math.log10(m) - Math.min(Math.log10(n0), Math.log10(n1));

        if (Measure == 0) {
            distance = l1 / l2;
        }

        if (Measure == 1) {
            distance = 1 - (Math.log10(n2) / Math.log10(n0 + n1 - n2));
        }

        if (n0 == 0 || n1 == 0 || n2 == 0) {
            distance = 1;
        }

        //System.out.println("n0="+n0);
        //System.out.println("n1="+n1);
        //System.out.println("n2="+n2);
        //System.out.println(a + "," + b + "=" + distance2);
        return distance;
    }

    public String traductorBing(String palabras) {

        if (false) {
            Translate.setClientId("fedquest");
            Translate.setClientSecret("ohCuvdnTlx8Sac4r7gfqyHy0xOJJpKK9duFC4tn9Sho=");
        } else {
            Translate.setClientId("karyabad");
            Translate.setClientSecret("viz4JYZAD8samvwuoV6gJ5MczDig8cBHyP0NnY1gRF0=");
        }

        String translatedText;
        try {
            translatedText = Translate.execute(palabras, Language.ENGLISH);

            return translatedText;
        } catch (Exception ex) {

            try {
                String[] ls = palabras.split("\\s\\|\\s");
                int chunk = ls.length / 2; // chunk size to divide
                String pal = "";
                for (int i = 0; i < ls.length; i += chunk) {
                    String[] pr = java.util.Arrays.copyOfRange(ls, i, i + chunk);
                    pr = clean2(pr);
                    String u = Joiner.on(" | ").join(pr);
                    u = traductorBing(u);
                    pal += u + " ";

                }

                return pal;

            } catch (Exception exx) {
                exx.printStackTrace(new PrintStream(System.out));
            }

        }

        return palabras;
    }

    private double getResultsCount(String query) throws IOException, SQLException {

        double c = 0;
        c = getResultsCount1(query);
        return c;
    }

    private double getResultsCount1(final String query) throws IOException, SQLException {

        String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srsearch=" + URLEncoder.encode(query, "UTF-8");
        double v = -1;
        do {
            try {
                String s = Http(url);
                JsonParser parser = new JsonParser();
                JsonObject parse = parser.parse(s).getAsJsonObject();
                v = parse.get("query").getAsJsonObject().get("searchinfo").getAsJsonObject().get("totalhits").getAsNumber().doubleValue();
            } catch (Exception e) {
                System.out.println(e + query + "Retry .... ");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Distance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //System.out.println("In1"+url);
        } while (v == -1);
        return v;
    }

    public List<String> clean(List<String> ls) {
        List<String> al = ls;
        Set<String> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);

        JsonArray asArray = config.get("stopwords").getAsJsonArray();

        for (JsonElement s : asArray) {
            al.remove(s.getAsString());
        }

        return al;
    }

    public <K, V extends Comparable<? super V>> Map<K, V>
            sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list
                = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public String[] clean2(final String[] v) {
        List<String> list = new ArrayList<String>(java.util.Arrays.asList(v));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new String[list.size()]);
    }

    public synchronized String Http(String s) throws SQLException, IOException {

        Statement stmt = conn.createStatement();
        String sql;
        sql = "SELECT * FROM cache where cache.key='" + getMD5(s) + "'";
        java.sql.ResultSet rs = stmt.executeQuery(sql);
        String resp = "";
        if (rs.next()) {
            resp = rs.getString("value");
            rs.close();
            stmt.close();
        } else {
            rs.close();
            stmt.close();
            final URL url = new URL(s);
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
            connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            final Scanner reader = new Scanner(connection.getInputStream(), "UTF-8");
            while (reader.hasNextLine()) {
                final String line = reader.nextLine();
                resp += line + "\n";
            }
            reader.close();

            try {
                JsonParser parser = new JsonParser();
                parser.parse(resp);
                PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO cache (key, value) values (?, ?)");
                stmt2.setString(1, getMD5(s));
                stmt2.setString(2, resp);
                stmt2.executeUpdate();
                stmt2.close();
            } catch (Exception e) {
                System.out.printf("Error al insertar en la DB: " +  e);
            }

        }

        return resp;
    }

    public synchronized String Http2(String s, Map<String, String> mp) throws SQLException, IOException {
        String md = s + mp.toString();
        Statement stmt = conn.createStatement();
        String sql;
        sql = "SELECT * FROM cache where cache.key='" + getMD5(md) + "'";
        java.sql.ResultSet rs = stmt.executeQuery(sql);
        String resp = "";
        if (rs.next()) {
            resp = rs.getString("value");
            rs.close();
            stmt.close();
        } else {
            rs.close();
            stmt.close();

            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(s);

            //Add any parameter if u want to send it with Post req.
            for (Entry<String, String> mcc : mp.entrySet()) {
                method.addParameter(mcc.getKey(), mcc.getValue());
            }

            int statusCode = client.executeMethod(method);

            if (statusCode != -1) {
                InputStream in = method.getResponseBodyAsStream();
                final Scanner reader = new Scanner(in, "UTF-8");
                while (reader.hasNextLine()) {
                    final String line = reader.nextLine();
                    resp += line + "\n";
                }
                reader.close();

                try {
                    JsonParser parser = new JsonParser();
                    parser.parse(resp);
                    PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO cache (key, value) values (?, ?)");
                    stmt2.setString(1, getMD5(md));
                    stmt2.setString(2, resp);
                    stmt2.executeUpdate();
                    stmt2.close();

                } catch (Exception e) {
                      System.out.printf("Error al insertar en la DB: " +  e);
                }

            }

        }

        return resp;
    }

    public String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String traductorYandex(String palabras) throws UnsupportedEncodingException, SQLException, IOException {
        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
        //String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20160321T160516Z.43cfb95e23a69315.6c0a2ae19f56388c134615f4740fbb1d400f15d3&lang=en&text=" + URLEncoder.encode(palabras, "UTF-8");
        Map<String, String> mp = new HashMap<>();
        mp.put("key", "trnsl.1.1.20160321T160516Z.43cfb95e23a69315.6c0a2ae19f56388c134615f4740fbb1d400f15d3");
        mp.put("lang", "en");
        mp.put("text", palabras);
        mp.put("options", "1");
        String rs = "";
        boolean c = true;
        int cont = 0;
        do {
            try {
                cont ++;
                String Http = Http2(url, mp);
                rs = Http;
                String res = Http;
                JsonParser parser = new JsonParser();
                JsonObject parse = parser.parse(res).getAsJsonObject();
                JsonArray asArray = parse.get("text").getAsJsonArray();
                res = asArray.get(0).getAsString();
                palabras = res;
                c = false;
            } catch (Exception e) {
                System.out.println(palabras + rs + "Retry");
                e.printStackTrace(new PrintStream(System.out));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Distance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //System.out.println("In2");
        } while (c && cont < 10);
        return palabras;

    }

    public List<String> traductor(List<String> join) throws SQLException, IOException {

        List<String> ls = new ArrayList();
        for (String w : join) {
            String translated = "";
            translated = traductorYandex(w.trim());
            if (translated.equals(w.trim())) {
                translated = traductorBing(w.trim()).trim().toLowerCase();
            }
            ls.add(translated.trim().toLowerCase());
        }
        return ls;
    }
    
    public double cosineSimilarityAndLevenshteinDistance(String param1, String param2) {

        String a = param1;
        String b = param2;

        StringMetric metric
                = with(new CosineSimilarity<String>())
                .simplify(Simplifiers.toLowerCase())
                .simplify(Simplifiers.removeNonWord()).simplifierCache()
                .tokenize(Tokenizers.qGram(3)).tokenizerCache().build();
        float compare = metric.compare(a, b);

        StringMetric metric2
                = with(new Levenshtein())
                .simplify(Simplifiers.removeDiacritics())
                .simplify(Simplifiers.toLowerCase()).build();

        float compare2 = metric2.compare(a, b);

        float similarity = (float) ((compare + compare2) / 2.0);

        return similarity;
    }
    
    public float jaccardSimilarity(String param1, String param2) {
        StringMetric metric2
                = with(new JaccardSimilarity<String>())
                .simplify(Simplifiers.removeDiacritics())
                .simplify(Simplifiers.toLowerCase())
                .tokenize(Tokenizers.qGram(2)).tokenizerCache().build();

        return metric2.compare(param1, param2);
    }
    

}
