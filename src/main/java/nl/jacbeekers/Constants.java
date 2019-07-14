/*
 * MIT License
 *
 * Copyright (c) 2019 JacTools
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

import java.util.HashMap;
import java.util.Map;

public class Constants {
    //generic result codes
    public static final String OK ="OK";
    public static final String NOT_IMPLEMENTED ="GEN-001";
    public static final String UNKNOWN ="GEN-0002";
    //xml errors
    public static final String XMLFILE_EXISTS ="XML-001";
    public static final String XMLFILE_CREATE_FAILED ="XML-002";
    public static final String XML_TRANSFORM_ERROR ="XML-003";
    public static final String XML_PARSER_CONFIG_ERROR ="XML-004";
    public static final String SAX_EXCEPTION ="XML-005";
    public static final String XMLSTREAM_ERROR ="XML-006";
    public static final String XSDNOTFOUND ="XML-007";

    Map results = new HashMap();
    public static Map<String, String> result;
    static {
        result = new HashMap<>();
        result.put(OK,"No errors encountered");
        result.put(UNKNOWN, "Internal error. No result message found. Contact the developer.");
        result.put(NOT_IMPLEMENTED, "Not implemented");
        result.put(XMLFILE_EXISTS, "XML file already exists");
        result.put(XMLFILE_CREATE_FAILED, "XML file could not be created");
        result.put(XML_TRANSFORM_ERROR, "Could not write XML to file");
        result.put(XML_PARSER_CONFIG_ERROR, "Could not write XML to file");
        result.put(SAX_EXCEPTION, "Could not write XML to file");
        result.put(XMLSTREAM_ERROR, "Could not write XML to file");
        result.put(XSDNOTFOUND, "Could not find XSD file");
    }

    //true/false
    public static final String NO ="N";
    public static final String YES ="Y";

    //xml types
    public static final String COMPLEX = "xs:complexType";
    public static final String SIMPLE = "xs:simpleType";

    //defaults
    public static final String DEFAULT_TARGET = "target";
    public static final String DEFAULT_XSD ="dummy.xsd";
    public static final String DEFAULT_XSD_PATH ="./";
    public static final String DEFAULT_ROOT_ELEMENT ="root";
    public static final String DEFAULT_ONE_FILE_PER_ROW = NO;

    public static String getResultMessage(String resultCode){
        return result.getOrDefault(resultCode, result.get(UNKNOWN));
    }
}
