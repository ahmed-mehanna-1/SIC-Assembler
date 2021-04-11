import SIC.Assembler;

public class Main {

    public static void main(String[] args) {

        Assembler assembler = new Assembler("FOLDER-PATH", "FILE-NAME");
        assembler.pass1();
        assembler.pass2();

    }

}