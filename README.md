## convert2xml
* Command line tool to convert a text file to XML
* Integrated into Informatica Developer 10.2.0 or 10.2.2, it can be used in a Java Transformation to speed up the generation of large XML files.

## Examples
In the resources folder you will Informatica 10.2.2 exports of the Java transformation, a mapping and application that uses it.

## Code snippets
If you just want to use the transformation, it is unlikely you need to change the Java code itself. The code snippets however can be found in the folder src/main/resources/code_snippets. For each Tab of the convert2xml transformation you dragged/dropped in the Developer Client, you will find a corresponding file.

## Usage
### Input
- logLevel
- xsdFile
- xsdDirectory
- targetFileName
- oneFilePerInputRow
- Value1.. Value70

### Output
- resultCode: the OK or Error code
- resultMessage: The output message, e.g. No errors encountered.
- nrFilesCreated: the number of files the Java transformation generated. 
