/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.latindex;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author José Luis Cullcay
 */

public class LatindexMain {
    
    private static int lowerLimitAuthor = 4;
    private static int lowerLimitPub = 5;
    private static int upperLimitKey = 10;
    
    private static String uri_provenance = "http://ucuenca.edu.ec/wkhuska/endpoint/4d0ebfe0bc494647139f10dfe308551f";
     
    //private static final KeywordGenerator keywordGenerator = new KeywordGenerator();
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, Exception {

        List<String> lista1 = new ArrayList<>();//keywordGenerator.getKeywords("Facultad de medicina, Investigación científica, Universidad de Guayaquil","");
        lista1.add("medicine");
        lista1.add("doctor");
        
        List<String> lista2 = new ArrayList<>();//keywordGenerator.getKeywords("Enhanced in vitro antitumor activity of a titanocene complex encapsulated into Polycaprolactone (PCL) electrospun fibers" + "Astereaceae, Baccharis latifolia, Limonene, Trichophyton mentagrophytes, Trichophyton rubrum", "");//"Astereaceae, Baccharis latifolia, Limonene, Trichophyton mentagrophytes, Trichophyton rubrum"
        lista2.add("cure");
        lista2.add("cancer");
        //Remove elements when there are more than 13
        for (int i = lista2.size() - 1; i > 13; i--) {
            lista2.remove(i);
        }
        Distance di = new Distance();
        Double NWD1 = di.NWD__(lista1, lista2);
        System.out.println(lista1);
        System.out.println(lista2);
        System.out.println("Distance: " + NWD1.toString());
        System.out.println("Jaccard Distance: " + di.jaccardSimilarity("Victor Hugo Saquicela Galarza", "V. Saquicela"));
        System.out.println("Jaccard Distance: " + di.jaccardSimilarity("Victor Hugo Saquicela Galarza", "Víctor Saquicela"));
        
        return;
        
        
}
    
    

}
