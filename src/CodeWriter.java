import java.io.File;
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
        } else if (command.equalsIgnoreCase("eq")) {
            eq();
        } else if (command.equalsIgnoreCase("lt")) {
            lt();
        } else if (command.equalsIgnoreCase("gt")) {
            gt();
        }
    }

    private void add() {
        lines.add("");
        lines.add("//add");
        popLastTwo();
        lines.add("M=M+D");
        advanceSP();
    }

    private void eq() {
        index++;
        System.out.println(index);
        lines.add("");
        lines.add("//eq");
        popLastTwo();
//        check if equal
        lines.add("M=D-M");
        lines.add("D=M");
        lines.add("@EQUAL" + index);
        lines.add("D;JEQ");
        lines.add("@NOTEQUAL" + index);
        lines.add("0;JMP");
        lines.add("(EQUAL" + index + ")");
        lines.add("D=-1");
        lines.add("@END" + index);
        lines.add("0;JMP");
        lines.add("(NOTEQUAL" + index + ")");
        lines.add("D=0");
        lines.add("(END" + index + ")");
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
        lines.add("@SP");
        lines.add("M=M+1");
    }

    private void lt() {
        index++;
        System.out.println(index);
        lines.add("");
        lines.add("//lt");
        popLastTwo();
//        check if lt
        lines.add("M=M-D");
        lines.add("D=M");
        lines.add("@LESSTHAN" + index);
        lines.add("D;JLT");
        lines.add("@NOTLESSTHAN" + index);
        lines.add("0;JMP");
        lines.add("(LESSTHAN" + index + ")");
        lines.add("D=-1");
        lines.add("@END" + index);
        lines.add("0;JMP");
        lines.add("(NOTLESSTHAN" + index + ")");
        lines.add("D=0");
        lines.add("(END" + index + ")");
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
        lines.add("@SP");
        lines.add("M=M+1");
    }

    private void gt() {
        index++;
        lines.add("");
        lines.add("//gt");
        popLastTwo();
//        check if lt
        lines.add("M=M-D");
        lines.add("D=M");
        lines.add("@GREATERTHAN" + index);
        lines.add("D;JGT");
        lines.add("@NOTGREATERTHAN" + index);
        lines.add("0;JMP");
        lines.add("(GREATERTHAN" + index + ")");
        lines.add("D=-1");
        lines.add("@END" + index);
        lines.add("0;JMP");
        lines.add("(NOTGREATERTHAN" + index + ")");
        lines.add("D=0");
        lines.add("(END" + index + ")");
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
        lines.add("@SP");
        lines.add("M=M+1");
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
