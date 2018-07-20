import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CodeWriter {
    private List<String> lines;
    private Path output;
    private int index = 0;

    public CodeWriter(String outputFile) {
        this.output = Paths.get("/Users/jon/Desktop/nand2tetris/projects/07/StackArithmetic/" + outputFile.split("\\.")[0],
                outputFile.split("\\.")[0] + ".asm");
        System.out.println(output);
        this.lines = new ArrayList<>();
    }


    public void writeArithMetic(String command) {
        if (command.equalsIgnoreCase("add")) {
            add();
        } else if (command.equalsIgnoreCase("sub")) {
            sub();
        } else if (command.equalsIgnoreCase("eq")) {
            logicalTwoArg("eq");
        } else if (command.equalsIgnoreCase("lt")) {
            logicalTwoArg("lt");
        } else if (command.equalsIgnoreCase("gt")) {
            logicalTwoArg("gt");
        }else if (command.equalsIgnoreCase("neg")) {
            neg();
        }else if (command.equalsIgnoreCase("and")) {
            and();
        }else if (command.equalsIgnoreCase("or")) {
            or();
        }else if (command.equalsIgnoreCase("not")) {
            not();
        }
    }

    private void and(){
        lines.add("");
        lines.add("//and");
        popLastTwo();
        lines.add("M=M&D");
        advanceSP();
    }

    private void or(){
        lines.add("");
        lines.add("//or");
        popLastTwo();
        lines.add("M=M|D");
        advanceSP();
    }

    private void add() {
        lines.add("");
        lines.add("//add");
        popLastTwo();
        lines.add("M=M+D");
        advanceSP();
    }

    private void sub() {
        lines.add("");
        lines.add("//sub");
        popLastTwo();
        lines.add("M=M-D");
        advanceSP();
    }

    private void logicalTwoArg(String command) {
        String jumpVal = "";
        String label = "";
        if (command.equals("eq")){
            jumpVal = "JEQ";
            label = "EQUAL";
        } else if (command.equals("gt")){
            jumpVal = "JGT";
            label = "GREATERTHAN";
        } else if (command.equals("lt")){
            jumpVal = "JLT";
            label = "LESSTHAN";
        }

        index++;
        lines.add("");
        lines.add("//"+command);
        popLastTwo();
//        check if equal
        lines.add("M=M-D");
        lines.add("D=M");
        lines.add("@"+label+ index);
        lines.add("D;"+jumpVal);
        lines.add("@NOT"+label + index);
        lines.add("0;JMP");
        lines.add("("+label + index + ")");
        lines.add("D=-1");
        lines.add("@END" + index);
        lines.add("0;JMP");
        lines.add("(NOT"+label + index + ")");
        lines.add("D=0");
        lines.add("(END" + index + ")");
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
        lines.add("@SP");
        lines.add("M=M+1");
    }

    private void neg(){
        lines.add("");
        lines.add("//neg");
        popLastOne();
        lines.add("M=-M");
        advanceSP();
    }

    private void not(){
        lines.add("");
        lines.add("//not");
        popLastOne();
        lines.add("M=!M");
        advanceSP();
    }

    private void popLastTwo() {
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
    }

    private void popLastOne(){
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
    }

    private void advanceSP() {
        lines.add("@SP");
        lines.add("M=M+1");
    }

    public void writePushPop(Parser.Command command, String segment, int index) {
        lines.add("");
        lines.add(String.format("//%s %s %s", command, segment, index));

        if (command.equals(Parser.Command.PUSH)) {
            lines.add("@" + index);
            lines.add("D=A");
            lines.add("@SP");
            lines.add("A=M");
            lines.add("M=D");
            lines.add("@SP");
            lines.add("M=M+1");

        }
    }

    public void writeToFile() {
        try {
            Files.write(output, lines);
        } catch (IOException e) {
            System.out.println("line 25, CodeWriter");
            System.out.println(e.getStackTrace());
        }
    }
}
