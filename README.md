## convert2xml
* Command line tool to convert a text file to XML
* Integrated into Informatica Developer 10.2.0 or 10.2.2, it can be used in a Java Transformation to speed up the generation of large XML files.

## Examples
In the resources folder you will Informatica 10.2.2 exports of the Java transformation, a mapping and application that uses it.

## Code snippets
If you just want to use the transformation, it is unlikely you need to change the Java code itself. The code snippets however can be found in the folder src/main/resources/code_snippets. For each Tab of the convert2xml transformation you dragged/dropped in the Developer Client, you will find a corresponding file.

## Usage
### Input
- logLevel <br />
  Optional or Required: Required <br />
  Purpose: Determines the amount and details of log information. <br />
  Possible values (from detailed to only on Errors): DEBUG, INFO, WARN, ERROR <br />
- xsdFile <br />
  Optional or Required: Required <br />
  Purpose: name of the XSD file to be used. Specify only the file name, not the path. <br />
  Possible values: Any valid file name up to 1000 characters <br />
- xsdDirectory <br />
  Optional or Required: Required <br />
  Purpose: The location of the XSD file. Can be a relative or absolute path. Must end with a / (forward slash) <br />
  Possible values: Any valid path name that ends on a forward slash (also on Windows) <br />
- targetFileName <br />
  Optional or Required: Required <br />
  Purpose: The location and file name to be used as target file name. Must NOT include an extension. <br />
  Possible values: Any valid path and file name, excluding its extension. The Java code will add the extension .xml <br />
- oneFilePerInputRow <br />
  Optional or Required: Required <br />
  Purpose: Whether each input row should generate its own XML file (Y) or not (N). If 'N' then all input rows will be collected in one single XML file <br />
  Possible values: Y|N <br />
- Value1.. Value70 <br />
  Optional or Required: Optional <br />
  Purpose: The input data that must be converted into XML elements. Value1 will be used as input for the first XML element (as determined by the provided XSD), Value2 is the input for the second XML element, and so forth. <br />
  Possible values: Any character string up to 100 characters (Value1 can be up to 1000 characters) <br />

### Output
- resultCode: the OK or Error code <br />
- resultMessage: The output message, e.g. No errors encountered. <br />
- nrFilesCreated: the number of files the Java transformation generated. <br /> 
