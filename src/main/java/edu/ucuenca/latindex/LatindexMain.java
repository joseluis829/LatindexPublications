/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.latindex;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author José Luis Cullcay
 */

public class LatindexMain {
    
    private static int lowerLimitAuthor = 4;
    private static int lowerLimitPub = 5;
    private static int upperLimitKey = 10;
    
    private static String uri_provenance = "http://ucuenca.edu.ec/wkhuska/endpoint/4d0ebfe0bc494647139f10dfe308551f";
    
    private static Set<String> listNames = new HashSet<String>();
     
    //private static final KeywordGenerator keywordGenerator = new KeywordGenerator();
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, Exception {

        Distance distance = new Distance();
        
        tokenizeMagazinesJson();
        
        
        if (true)
            return;
        
        //Get authors 0.87 >=14
        String queryPublications = "PREFIX bibo: <http://purl.org/ontology/bibo/>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix uc: <http://ucuenca.edu.ec/ontology#>"
                + "PREFIX dct: <http://purl.org/dc/terms/>"
                + "SELECT DISTINCT ?publication ?issn ?revista ?pubName ?journal WHERE {  "
                + "  GRAPH<http://ucuenca.edu.ec/wkhuska>{     "
                + "    ?author foaf:publications ?publication."//;  "
                //+ "             dct:provenance ?endpoint. "
                + "    ?publication <http://ucuenca.edu.ec/ontology#origin> ?provider. "
                + "    optional{?publication <http://prismstandard.org/namespaces/basic/2.0/issn> ?issn}. "
                + "    optional{?publication bibo:Conference ?revista}. "
                + "    optional{?publication <http://prismstandard.org/namespaces/basic/2.0/publicationName> ?pubName}. "
                + "    optional{?publication <http://purl.org/ontology/bibo/Journal> ?journal}. "
                //+ "    filter (!regex(?provider, \"Scopus\", \"i\"))."
                //+ "    filter (regex(?provider, \"Dspace\", \"i\"))."
                //+ "    filter (!regex(?provider, \"Latindex\", \"i\"))."
                /*+ "     {"
                + "         SELECT * { "
                + "        	GRAPH <http://ucuenca.edu.ec/wkhuska/endpoints> { "
                + "              ?endpoint uc:name \"UCUENCA\"^^xsd:string . "
                + "            } "
                + "         } "
                + "     }"*/
                + "  } "
                + "}";

        //Files
        PrintWriter out = new PrintWriter("LatindexProviderFromJSON_1.csv");
        PrintWriter distanceFile = new PrintWriter("LatindexDistance.csv");

        Repository repo = new SPARQLRepository("http://redi.cedia.org.ec:8080/sparql/select");
        Repository repoUpdate = new SPARQLRepository("http://localhost:8080/sparql/update");

        repo.initialize();
        repoUpdate.initialize();
        RepositoryConnection con = repo.getConnection();
        RepositoryConnection conUpdate = repoUpdate.getConnection();
        try {
            // perform operations on the connection
            TupleQueryResult resulta = con.prepareTupleQuery(QueryLanguage.SPARQL, queryPublications).evaluate();

            int contador = 0;

            //get JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new InputStreamReader(new FileInputStream("origenProvider(All).json"), StandardCharsets.UTF_8));
            JSONArray magazines = (JSONArray) obj;

            while (resulta.hasNext()) { //&& contador <=250) { 
                BindingSet binding = resulta.next();
                String publicationId = String.valueOf(binding.getValue("publication")).trim();
                String issn = String.valueOf(binding.getValue("issn")).trim();
                String revistas[] = new String[4];
                revistas[0] = binding.getValue("revista") != null ? String.valueOf(binding.getValue("revista")) : "";
                revistas[0] = revistas[0] != null && !revistas[0].equals("null") ? revistas[0].replace("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#string>", "").trim() : "";
                revistas[0] = revistas[0].replace("&", " ");
                //Publicacion
                revistas[1] = binding.getBinding("pubName") != null ? binding.getBinding("pubName").getValue().stringValue() : "";
                revistas[1] = revistas[1] != null ? revistas[1].replace("&", " ") : "";
                //Journal
                revistas[2] = binding.getBinding("journal") != null ? binding.getBinding("journal").getValue().stringValue() : "";
                revistas[2] = revistas[2] != null ? revistas[2].replace("&", " ") : "";

                issn = issn != null && !issn.equals("null") ? issn.replace("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#string>", "").trim().replace("X", "") : "";

                //revistas[1] = revistas[1].split("\\.").length > 0 ? revistas[1].split("\\.")[0] : revistas[1];
                revistas[3] = revistas[1].split("\\.").length > 1 ? revistas[1].split("\\.")[1] : "";
                //
                String latindex = "";
                /*if (!issn.equals("")) {
                    latindex = NetClientPost.consultarLatindex(issn, false);
                    out.println(publicationId + "," + revista.replace(",", "-") + "," + issn + "," + latindex.replace(",", ";"));
                    //guardar y colocar como verificado
                } else*/

                for (String revista : revistas) {
                    if (!revista.equals("")) {
                        revista = remove1(revista.toLowerCase());

                        Iterator<JSONObject> iterator = magazines.iterator();
                        while (iterator.hasNext()) {
                            JSONObject magazine = iterator.next();
                            String titulo = (String) ((JSONArray) magazine.get("tit_clave")).get(0);
                            titulo = (titulo.split("\\("))[0].trim().toLowerCase();
                            titulo = remove1(titulo.replace("&", " "));

                            //String issnRev = (String) ((JSONArray) magazine.get("issn")).get(0);
                            double distanciaRev = 0;

                            if (!revista.equals("")) {
                                distanciaRev = distance.cosineSimilarityAndLevenshteinDistance(titulo, revista);
                                if (distanciaRev > 0.9) {//if (distanciaRev >= 0.7 && distanciaRev <= 0.9) {
                                    String insert = " PREFIX dct: <http://purl.org/dc/terms/> "
                                            + " PREFIX bibo: <http://purl.org/ontology/bibo/> "
                                            + " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                                            + " INSERT DATA { "
                                            + " GRAPH <http://ucuenca.edu.ec/wkhuska> "
                                            + " { "
                                            + "     <" + publicationId + "> <http://ucuenca.edu.ec/ontology#origin> \"" + "Latindex" + "\"^^xsd:string. "
                                            + "     <" + publicationId + "> <http://ucuenca.edu.ec/ontology#latindexStatus> \"" + "Verificado" + "\"^^xsd:string. "
                                            + " } "
                                            + "}";

                                    //conUpdate.prepareUpdate(QueryLanguage.SPARQL, insert).execute();

                                }
                                
                                float distanciaRev2 = distance.jaccardSimilarity(titulo, revista);
                                float distanciaRev3 = distance.jaroWinkler(titulo, revista);

                                if ((distanciaRev + distanciaRev2)/2 > 0.7) {
                                    distanceFile.println(publicationId + "," + revista.replace(",", ";") + "," + titulo + "," + distanciaRev + "," + distanciaRev2 + "," + distanciaRev3 + "," + (distanciaRev + distanciaRev2)/2);
                                    System.out.println("Output from Server .... \n Revista/Issn:" + revista + ". Titulo: " + titulo + ". \nDistancia: " + distanciaRev + " Jaccard: " + distanciaRev2 + " Jaro: " + distanciaRev3);
                                    //break;
                                }
                            }
                        }
                    }

                    //out.println(publicationId + "," + revista.replace(",", "-") + "," + issn + "," + latindex.replace(",", ";"));
                }
                contador++;
            }
            System.out.println("Fin del proceso ");
            out.close();
            distanceFile.close();
            //distanceFile2.close();
            con.close();
            conUpdate.close();
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            java.util.logging.Logger.getLogger(LatindexMain.class.getName()).log(Level.SEVERE, null, ex);
            out.close();
            distanceFile.close();
            //distanceFile2.close();
            con.close();
            conUpdate.close();
            System.out.println("Fin del proceso ");
        } finally {
            out.close();
            distanceFile.close();
            //distanceFile2.close();
            con.close();
            conUpdate.close();
            System.out.println("Fin del proceso ");
        }

        return;

    }

    /**
     * Función que elimina acentos y caracteres especiales de una cadena de
     * texto.
     *
     * @param input
     * @return cadena de texto limpia de acentos y caracteres especiales.
     */
    public static String remove1(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuâêîôûñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuuaeiounAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }
    
    public static boolean compareJson(Set<String> revistas) {
        boolean equal = false;
        try {
            Distance distance = new Distance();

            double coefficient = 0.0;

            for (String revista : revistas) {
                for (String titulo : listNames) {
                    coefficient = (distance.cosineSimilarityAndLevenshteinDistance(titulo, revista) + distance.jaccardSimilarity(titulo, revista)) / 2;

                    if (coefficient > 0.90) {
                        System.err.println(revista.replace(",", ";") + "," + titulo.replace(",", ";") + "," + coefficient);
                    }

                }

            }

        } catch (IOException ex) {
            Logger.getLogger(LatindexMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LatindexMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return equal;
    }
    
    
    public static void tokenizeMagazinesJson() {
        Distance distance;

        try {
            distance = new Distance();

            //get JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new InputStreamReader(new FileInputStream("origenProvider(All).json"), StandardCharsets.UTF_8));
            JSONArray magazines = (JSONArray) obj;

            Iterator<JSONObject> iterator = magazines.iterator();
            while (iterator.hasNext()) {
                JSONObject magazine = iterator.next();
                String titulo = (String) ((JSONArray) magazine.get("tit_clave")).get(0);
                listNames = distance.getSetNames(titulo);
                
            }

        } catch (IOException ex) {
            Logger.getLogger(LatindexMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LatindexMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(LatindexMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    

}
