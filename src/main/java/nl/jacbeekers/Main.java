/*
 * MIT License
 *
 * Copyright (c) 2019 Jac. Beekers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package nl.jacbeekers;

import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws ConversionException, Exception {
        // write your code here
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(GenerateXml.class.getName());

        String xsdFile = args[0];
        String xmlFile = args[1];
        String rootElement = null;
        if(args.length > 2) {
            rootElement = args[2];
        }
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
        map2.put("description",null);
        map2.put("is_rating_unrated","2");
        map2.put("quality_ascending_sequence","2");
        map2.put("rating_rank","456");
        data.add(map2);

        GenerateXml xml =null;

        xml = new GenerateXml();
        if (rootElement != null) {
            xml.setRootElement(rootElement);
        }
        xml.setXsdFile(xsdFile);
        xml.setXsdPath("D:\\GitRepos\\convert2xml\\src\\main\\resources\\");
        xml.setTarget(xmlFile);
        xml.setOneFilePerRow("N");
        xml.setSuppressEmptyTags("Y");
        xml.generateXmlFile(data);
        logger.info(xml.getResultCode());
        logger.info(xml.getResultMessage());
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
