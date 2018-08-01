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
    private String fileName;
    private int returnIndex = 0;
    private String currentFile;
    private String returnAddress;


    public CodeWriter(String outputFile) {
        String fileName = outputFile.split("\\.")[0];

        if (outputFile.contains(".vm")) {
            this.output = Paths.get("/Users/jon/Desktop/nand2tetris/projects/08/FunctionCalls/" + fileName,
                    fileName + ".asm");
        } else {
            fileName = outputFile;
            this.output = Paths.get("/Users/jon/Desktop/nand2tetris/projects/08/FunctionCalls/" + fileName,
                    fileName + ".asm");
        }


        this.lines = new ArrayList<>();

        this.fileName = outputFile.split("\\.")[0];
        writeInit();
    }


//    sys init
    public void writeInit(){
        lines.add("@256");
        lines.add("D=A");
        lines.add("@SP");
        lines.add("M=D");
        writeCall("Sys.init", 0);
    }

    //    Function logic
    public void writeFunctionCalls(Parser.Command command, String arg1, int arg2) {
        lines.add("");
        lines.add(String.format("//%s %s %s", command, arg1, arg2));
        if (command.equals(Parser.Command.FUNCTION)) {
            writeFunction(arg1, arg2);
        } else if (command.equals(Parser.Command.CALL)) {
            writeCall(arg1, arg2);
        }
    }

    public void writeCall(String arg1, int arg2) {
        int argRepositionIndex = arg2 + 5;
        returnIndex++;
//        push return address
        returnAddress = String.format("%s$ret.%d", arg1, returnIndex);
        lines.add("@"+returnAddress);
        lines.add("D=A");
        pushStack();
//        save lcl, arg, this, that
        lines.add("@LCL");
        lines.add("D=M");
        pushStack();
        lines.add("@ARG");
        lines.add("D=M");
        pushStack();
        lines.add("@THIS");
        lines.add("D=M");
        pushStack();
        lines.add("@THAT");
        lines.add("D=M");
        pushStack();
//        reposition arg
        lines.add("@SP");
        lines.add("D=M");
        lines.add("@" + argRepositionIndex);
        lines.add("D=D-A");
        lines.add("@ARG");
        lines.add("M=D");
//        LCL=SP
        lines.add("@SP");
        lines.add("D=M");
        lines.add("@LCL");
        lines.add("M=D");
        writeGoto(arg1);
        lines.add(String.format("(%s)", returnAddress));
    }

    public void writeFunction(String arg1, int arg2) {
        lines.add(String.format("(%s)", arg1));
        for (int i = 0; i < arg2; i++) {
            lines.add("@SP");
            lines.add("A=M");
            lines.add("M=0");
            lines.add("@SP");
            lines.add("M=M+1");
        }
    }


    public void writeReturn() {
        index++;
        lines.add("");
        lines.add(String.format("//return"));
//        set frame
        lines.add("@LCL");
        lines.add("D=M");
        lines.add("@FRAME"+index);
        lines.add("M=D");

//        store frame value in register 13
        lines.add("@13");
        lines.add("M=D");
//        subtract 5 from frame to get return address
        lines.add("@5");
        lines.add("D=A");
        lines.add("@13");
        lines.add("D=M-D");
//        RET = *(FRAME-5)
        lines.add("A=D");
        lines.add("D=M");
        lines.add("@RET"+index);
        lines.add("M=D");
//        *ARG=pop();
        popLastOne();
        lines.add("D=M");
        lines.add("@ARG");
        lines.add("A=M");
        lines.add("M=D");
//        SP=ARG+1;
        lines.add("@ARG");
        lines.add("D=M+1");
        lines.add("@SP");
        lines.add("M=D");
//        restore that,this,arg,lcl
        lines.add("@FRAME"+index);
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
        lines.add("@THAT");
        lines.add("M=D");

        lines.add("@FRAME"+index);
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
        lines.add("@THIS");
        lines.add("M=D");

        lines.add("@FRAME"+index);
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
        lines.add("@ARG");
        lines.add("M=D");

        lines.add("@FRAME"+index);
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
        lines.add("@LCL");
        lines.add("M=D");
        lines.add("@RET"+index);
        lines.add("D=M");
        lines.add("A=M");
        lines.add("0;JMP");
//        writeGoto(returnAddress);

    }
//    Branching Logic

    public void writeLabelGotoIf(Parser.Command command, String label) {
        lines.add("");
        lines.add(String.format("//%s %s", command, label));
        if (command.equals(Parser.Command.LABEL)) {
            writeLabel(label);
        } else if (command.equals(Parser.Command.GOTO)) {
            writeGoto(label);
        } else if (command.equals(Parser.Command.IF)) {
            writeIf(label);
        }
    }

    //    LABEL logic
    public void writeLabel(String label) {
        lines.add(String.format("(%s)", label));
    }

    //    GOTO logic
    public void writeGoto(String label) {
        lines.add("@" + label);
        lines.add("0;JMP");
    }

    //    IF logic
    public void writeIf(String label) {
        System.out.println(label);
        popLastOne();
        lines.add("D=M");
        lines.add("@" + label);
        lines.add("D;JNE");

    }

//    PUSH/POP Logic

    public void writePushPop(Parser.Command command, String segment, int index) {
        lines.add("");
        lines.add(String.format("//%s %s %s", command, segment, index));
        if (command.equals(Parser.Command.PUSH)) {
            handlePush(segment, index);
        } else if (command.equals(Parser.Command.POP)) {
            handlePop(segment, index);
        }
    }

    private void handlePush(String segment, int index) {
        if (segment.equals("constant")) {
            pushConstant(index);
        } else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") ||
                segment.equals("that") || segment.equals("temp") || segment.equals("pointer")) {
            pushLocalArgThisThatTempPointer(segment, index);
        } else if (segment.equals("static")) {
            pushStatic(fileName, index);
        }

    }

    private void handlePop(String segment, int index) {
        if (segment.equals("static")) {
            popStatic(fileName, index);
        } else if (segment.equals("local") || segment.equals("argument") || segment.equals("this") ||
                segment.equals("that") || segment.equals("temp") || segment.equals("pointer")) {
            popLocalArgThisThatTempPointer(segment, index);

        }
    }


//    Pop logic

    private void popStatic(String filename, int index) {
        lines.add("@" + fileName + "." + index);
        pop(index);
    }

    private void popLocalArgThisThatTempPointer(String segment, int index) {
        if (segment.equals("local")) {
            lines.add("@LCL");
            pop(index);
        } else if (segment.equals("argument")) {
            lines.add("@ARG");
            pop(index);
        } else if (segment.equals("this")) {
            lines.add("@THIS");
            pop(index);
        } else if (segment.equals("that")) {
            lines.add("@THAT");
            pop(index);
        } else if (segment.equals("temp")) {
            index = index + 5;
            lines.add("@" + index);
            lines.add("D=A");
            lines.add("@13");
            lines.add("M=D");
//            get value to pop, d = value
            lines.add("@SP");
            lines.add("M=M-1");
            lines.add("A=M");
            lines.add("D=M");
//            access  register 13
            lines.add("@13");
            lines.add("A=M");
            lines.add("M=D");
//            push to stack
            lines.add("@SP");
            lines.add("A=M");
            lines.add("M=D");
        } else if (segment.equals("pointer")) {
            index = index + 3;
            lines.add("@" + index);
            lines.add("D=A");
            lines.add("@13");
            lines.add("M=D");
//            get value to pop, d = value
            lines.add("@SP");
            lines.add("M=M-1");
            lines.add("A=M");
            lines.add("D=M");
//            access  register 13
            lines.add("@13");
            lines.add("A=M");
            lines.add("M=D");
//            push to stack
            lines.add("@SP");
            lines.add("A=M");
            lines.add("M=D");
        }
    }

    private void pop(int index) {
        //access segment + index, d = new address, new address at register 13
        lines.add("D=M");
        lines.add("@" + index);
        lines.add("D=D+A");
        lines.add("@13");
        lines.add("M=D");
//            get value to pop, d = value
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
//            access  register 13
        lines.add("@13");
        lines.add("A=M");
        lines.add("M=D");
//            push to stack
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
    }

//    Push Logic

    private void pushLocalArgThisThatTempPointer(String segment, int index) {
//        get value of base memory location
        if (segment.equals("local")) {
            lines.add("@LCL");
            pushSegment(index);
            pushStack();
        } else if (segment.equals("argument")) {
            lines.add("@ARG");
            pushSegment(index);
            pushStack();
        } else if (segment.equals("this")) {
            lines.add("@THIS");
            pushSegment(index);
            pushStack();
        } else if (segment.equals("that")) {
            lines.add("@THAT");
            pushSegment(index);
            pushStack();
        } else if (segment.equals("temp")) {
            index = index + 5;
            lines.add("@" + index);
            lines.add("D=M");
            pushStack();
        } else if (segment.equals("pointer")) {
            index = index + 3;
            lines.add("@" + index);
            lines.add("D=M");
            pushStack();
        }

    }

    private void pushStatic(String fileName, int index) {
        lines.add("@" + fileName + "." + index);
        pushSegment(index);
        pushStack();
    }


    private void pushConstant(int index) {
        lines.add("@" + index);
        lines.add("D=A");
        pushStack();
    }

    private void pushStack() {
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
        advanceSP();
    }

    private void pushSegment(int index) {
//        A = segment
//        rest of logic
        lines.add("D=M");
        lines.add("@" + index);
        lines.add("D=D+A");
        lines.add("A=D");
        lines.add("D=M");
    }

//Arithmetic/Logical Logic

    public void writeArithmetic(String command) {
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
        } else if (command.equalsIgnoreCase("neg")) {
            neg();
        } else if (command.equalsIgnoreCase("and")) {
            and();
        } else if (command.equalsIgnoreCase("or")) {
            or();
        } else if (command.equalsIgnoreCase("not")) {
            not();
        }
    }

    private void and() {
        lines.add("");
        lines.add("//and");
        popLastTwo();
        lines.add("M=M&D");
        advanceSP();
    }

    private void or() {
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

    private void neg() {
        lines.add("");
        lines.add("//neg");
        popLastOne();
        lines.add("M=-M");
        advanceSP();
    }

    private void not() {
        lines.add("");
        lines.add("//not");
        popLastOne();
        lines.add("M=!M");
        advanceSP();
    }

    private void logicalTwoArg(String command) {
        String jumpVal = "";
        String label = "";
        if (command.equals("eq")) {
            jumpVal = "JEQ";
            label = "EQUAL";
        } else if (command.equals("gt")) {
            jumpVal = "JGT";
            label = "GREATERTHAN";
        } else if (command.equals("lt")) {
            jumpVal = "JLT";
            label = "LESSTHAN";
        }

        index++;
        lines.add("");
        lines.add("//" + command);
        popLastTwo();

//        check if equal
        lines.add("M=M-D");
        lines.add("D=M");
        lines.add("@" + label + index);
        lines.add("D;" + jumpVal);
        lines.add("@NOT" + label + index);
        lines.add("0;JMP");
        lines.add("(" + label + index + ")");
        lines.add("D=-1");
        lines.add("@END" + index);
        lines.add("0;JMP");
        lines.add("(NOT" + label + index + ")");
        lines.add("D=0");
        lines.add("(END" + index + ")");
        lines.add("@SP");
        lines.add("A=M");
        lines.add("M=D");
        lines.add("@SP");
        lines.add("M=M+1");
    }

//    Utility

    private void popLastTwo() {
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
        lines.add("D=M");
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
    }


    private void popLastOne() {
        lines.add("@SP");
        lines.add("M=M-1");
        lines.add("A=M");
    }

    private void advanceSP() {
        lines.add("@SP");
        lines.add("M=M+1");
    }


    public void writeToFile() {
        try {
            Files.write(output, lines);
        } catch (IOException e) {
            System.out.println(e);
            throw new Error("No File named: " + output);
        }
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }
}
