package SIC;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Hashtable;

public class Assembler {
    private final String folderPath;
    private final String fileName;

    public Assembler(String folderPath, String fileName) {
        this.folderPath = folderPath;
        this.fileName = fileName;
    }

    public void pass1(){
        try {
            FileReader fr = new FileReader(folderPath.concat("\\".concat(fileName)));
            BufferedReader read = new BufferedReader(fr);
            FileWriter write = new FileWriter(folderPath.concat("\\pass1.txt"));
            String line, lastMnemonic, lastOperand, location;
            // read first line of code and get starting address.. the line has program name
            line = read.readLine();
            write.write("\t" + line + "\n");
            location = Integer.toHexString(Integer.parseInt(line.split("\t")[2], 16)).toUpperCase();
            // read second line and write the location without any addition
            line = read.readLine();
            lastMnemonic = line.split("\t")[1];
            lastOperand = line.split("\t")[2];
//            for(int i = 1; i <= 6 - location.length(); i++)
//                    write.write("0");
            write.write(location + "\t" + line + "\n");
            while((line = read.readLine()) != null){

                if(line.equals(""))
                    continue;
                if(line.split("\t")[0].equals(".") ){
                    write.write("\t" + line + "\n");
                    continue;
                }
                else if(lastMnemonic.equals("RESW"))
                    location = Integer.toHexString(Integer.parseInt(location, 16) + Integer.parseInt(lastOperand) * 3).toUpperCase();
                else if(lastMnemonic.equals("RESB"))
                    location = Integer.toHexString(Integer.parseInt(location, 16) + Integer.parseInt(lastOperand)).toUpperCase();
                else if(lastMnemonic.equals("BYTE"))
                    location = Integer.toHexString(Integer.parseInt(location, 16) + lastOperand.length() - 3).toUpperCase();
                else
                    location = Integer.toHexString(Integer.parseInt(location, 16) + 3).toUpperCase();
                lastMnemonic = line.split("\t")[1];;
                if(lastMnemonic.equals("RESW") || lastMnemonic.equals("RESB") || lastMnemonic.equals("BYTE"))
                    lastOperand = line.split("\t")[2];
//                for(int i = 1; i <= 6 - location.length(); i++)
//                    write.write("0");
                write.write(location + "\t" + line + "\n");
            }
            read.close();
            write.close();
            symbolTable();
        }
        catch(Exception ex){

        }
    }
    public void pass2(){
        try {
            FileReader fr = new FileReader(folderPath.concat("\\pass1.txt"));
            BufferedReader read = new BufferedReader(fr);
            FileReader fr2 = new FileReader(folderPath.concat("\\Symbol Table.txt"));
            BufferedReader search = new BufferedReader(fr2);
            FileWriter write = new FileWriter(folderPath.concat("\\pass2.txt"));
            Hashtable<String, String> instruction = Opcode();
            String line, lineSearch, endLocation = "";
            line = read.readLine();
            write.write(line + "\n");
            while((line = read.readLine()) != null){
                if(line.equals("") || line.split("\t")[1].equals(".") || line.split("\t")[2].equals("END")){
                    write.write(line + "\n");
                    endLocation = line.split("\t")[0];
                    continue;
                }
                else if(instruction.containsKey(line.split("\t")[2])){
                    if(line.split("\t").length == 3){
                        write.write(line + "\t\t"+ instruction.get(line.split("\t")[2])+"0000\n");
                        continue;
                    }
                    String operand = line.split("\t")[3];
                    boolean indexed = false, found = false;
                    if(operand.toCharArray()[operand.length() -1] == 'X'
                            && operand.toCharArray()[operand.length() -2] == ','){
                        operand = String.copyValueOf(operand.toCharArray(),0,operand.length() - 2);
                        indexed = true;
                    }
                    while((lineSearch = search.readLine()) != null)
                        if(operand.equals(lineSearch.split("\t")[1])){
                            write.write(line + "\t" +
                                    instruction.get(line.split("\t")[2]));
                            if(indexed){
                                operand = lineSearch.split("\t")[0];
                                String c = String.valueOf(operand.toCharArray()[0]);
                                if(Integer.toBinaryString(Integer.parseInt(c, 16)).length() == 3)
                                    c = "1".concat(Integer.toBinaryString(Integer.parseInt(c)));
                                c = Integer.toHexString(Integer.parseInt(c, 2))
                                        .concat(String.copyValueOf(operand.toCharArray(), 1, operand.length() - 1))
                                        .toUpperCase();
                                write.write(c + "\n");
                            }
                            else
                                write.write(lineSearch.split("\t")[0] + "\n");
                            found = true;
                            break;
                        }
                        else
                            found = false;

                    if(!found){
                        write.write(line + "\n");
                        System.err.println(operand + "\tNot deffined");
                    }
                    fr2 = new FileReader(folderPath.concat("\\Symbol Table.txt"));
                    search = new BufferedReader(fr2);
                }
                else if(line.split("\t")[2].equals("WORD")){
                    write.write(line + "\t");

                    for(int i = 1; i <= 6 - Integer.toHexString(Integer.parseInt(line.split("\t")[3])).length(); i++)
                        write.write("0");
                    write.write(Integer.toHexString(Integer.parseInt(line.split("\t")[3])) + "\n");
                }
                else if(line.split("\t")[2].equals("BYTE")){
                    write.write(line + "\t");
                    for(int i=2;i<line.split("\t")[3].length() - 1; i++)
                        write.write(String.format("%x", (int)line.split("\t")[3].toCharArray()[i]));
                    write.write("\n");
                }
                else if(line.split("\t")[2].equals("RESW") || line.split("\t")[2].equals("RESB"))
                    write.write(line + "\tN.O.C \n");
            }
            write.close();
            objectProgram(endLocation);
        }
        catch(Exception ex){

        }
    }
    public void symbolTable(){
        try {
            FileReader fr = new FileReader(folderPath.concat("\\pass1.txt"));
            BufferedReader read = new BufferedReader(fr);
            FileWriter write = new FileWriter(folderPath.concat("\\Symbol Table.txt"));
            String line = read.readLine();
            while((line = read.readLine()) != null)
                if(!line.split("\t")[1].equals(""))
                    write.write(line.split("\t")[0] + "\t" + line.split("\t")[1] + "\n");
            read.close();
            write.close();
        }
        catch(Exception ex){

        }
    }
    public void objectProgram(String endLocation){
        try {
            FileReader fr = new FileReader(folderPath.concat("\\pass2.txt"));
            BufferedReader read = new BufferedReader(fr);
            FileWriter write = new FileWriter(folderPath.concat("\\Opject Program.txt"));
            String line , collection = "", startingAddress = "", lastLocation;
            line = read.readLine();
            int length = 0;
            write.write("H:" + line.split("\t")[1]);
            for(int i = 1; i <= 6 - line.split("\t")[1].length(); i++)
                write.write(" ");
            write.write(",");
            for(int i = 1; i <= 6 - line.split("\t")[3].length(); i++){
                write.write("0");
                startingAddress = startingAddress.concat("0");
            }
            write.write(line.split("\t")[3] + ",");
            for(int i = 1; i <= 6 - Integer.toHexString(Integer.parseInt(endLocation, 16)
                    - Integer.parseInt(line.split("\t")[3], 16)).length(); i++)
                write.write("0");
            write.write(Integer.toHexString(Integer.parseInt(endLocation, 16)
                    - Integer.parseInt(line.split("\t")[3], 16)).toUpperCase() + "\n");

            startingAddress = startingAddress.concat(line.split("\t")[3]);

            line = read.readLine();
            write.write("T:");
            for(int i = 1; i <= 6 - line.split("\t")[0].length(); i++)
                write.write("0");
            write.write(line.split("\t")[0] + ",");
            collection = collection.concat(line.split("\t")[4]).concat(",");
            length += 3;
            lastLocation = line.split("\t")[0];

            boolean readLine = true, end = false;
            while(line != null){
                if(readLine)
                    line = read.readLine();
                if(line.split("\t")[2].equals("END")){
                    line = null;
                    //if(readLine)
                    continue;
                }
                if(line.split("\t")[2].equals("RESW") || line.split("\t")[2].equals("RESB")){
                    lastLocation = line.split("\t")[0];
                    end = true;
                    continue;
                }
                if((length + (Integer.parseInt(line.split("\t")[0], 16) - Integer.parseInt(lastLocation, 16))) <= 30 && !end){
                    if(length == 0){
                        write.write("T:");
                        for(int i = 1; i <= 6 - line.split("\t")[0].length(); i++)
                            write.write("0");
                        write.write(line.split("\t")[0] + ",");
                    }
                    collection = collection.concat(line.split("\t")[4]).concat(",");
                    length += Integer.parseInt(line.split("\t")[0], 16) - Integer.parseInt(lastLocation, 16);
                    lastLocation = line.split("\t")[0];
                    readLine = true;
                }
                else {
                    readLine = false;
                    end = false;
                    if(Integer.toHexString(length).length() == 1)
                        write.write("0");
                    write.write(Integer.toHexString(length).toUpperCase() + "," +
                            String.copyValueOf(collection.toCharArray(), 0, collection.length() - 1) + "\n");
                    collection = "";
                    length = 0;
                }
            }
            if(Integer.toHexString(length).length() == 1)
                write.write("0");
            write.write(Integer.toHexString(length).toUpperCase() + "," +
                    String.copyValueOf(collection.toCharArray(), 0, collection.length() - 1) + "\n");
            write.write("E:" + startingAddress);
            write.close();
        }
        catch(Exception ex){

        }
    }

    private Hashtable<String, String> Opcode(){

        Hashtable<String, String> instruction = new Hashtable<>();

        instruction.put("ADD", "18");
        instruction.put("ADDR", "90");
        instruction.put("COMP", "28");
        instruction.put("COMPR", "A0");
        instruction.put("DIV", "24");
        instruction.put("DIVR", "9C");
        instruction.put("J", "3C");
        instruction.put("JEQ", "30");
        instruction.put("JGT", "34");
        instruction.put("JLT", "38");
        instruction.put("JSUB", "48");
        instruction.put("LDA", "00");
        instruction.put("LDCH", "50");
        instruction.put("LDS", "6C");
        instruction.put("LDT", "74");
        instruction.put("LDL", "08");
        instruction.put("LDX", "04");
        instruction.put("RSUB", "4C");
        instruction.put("STA", "0C");
        instruction.put("STL", "14");
        instruction.put("STCH", "54");
        instruction.put("STS", "7C");
        instruction.put("STT", "84");
        instruction.put("STX", "10");
        instruction.put("SUB", "1C");
        instruction.put("SUBR", "94");
        instruction.put("TD", "E0");
        instruction.put("RD", "D8");
        instruction.put("WR", "DC");
        instruction.put("TIX", "2C");
        instruction.put("TIXR", "BB");
        return instruction;
    }
}