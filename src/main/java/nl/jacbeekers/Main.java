package nl.jacbeekers;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws ConversionException, Exception {
        // write your code here
        String xsdFile = args[0];
        String rootElement = args[1];
        String xmlFile = args[2];
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

        HashMap<String,String> map = new HashMap<String, String>();
        map.put("rating_type","123");
        map.put("rating","1");
        map.put("description","test1");
        map.put("is_rating_unrated","1");
        map.put("quality_ascending_sequence","1");
        map.put("rating_rank","123");
        data.add(map);

        HashMap<String,String> map2 = new HashMap<String, String>();
        map2.put("rating_type","123");
        map2.put("rating","2");
        map2.put("description","test2");
        map2.put("is_rating_unrated","2");
        map2.put("quality_ascending_sequence","2");
        map2.put("rating_rank","456");
        data.add(map2);

        GenerateXml xml =null;

        xml = new GenerateXml();
        xml.setRootElement(rootElement);
        xml.setXsdFile(xsdFile);
        xml.setTarget(xmlFile);
        xml.setOneFilePerRow("N");
        xml.generateXmlFile(data);
/*
        xml = new generateXml();
        xml.setRootElement(rootElement);
        xml.setXsdFile(xsdFile);
        xml.setTarget(xmlFile);
        xml.setOneFilePerRow("N");
        xml.generateXmlFile("D:\\GitRepos\\convert2xml\\src\\main\\resources\\inputforxmldemo.txt");
*/
    }
}
