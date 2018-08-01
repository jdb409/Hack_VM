import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Throwable {
        // write your code here

        if (args.length == 0) {
            throw new Throwable("Please enter a file name!");
        }

        File file = new File("examples/" + args[0]);
        if (file.isFile()) {
            Parser parser = new Parser(file.getName());
            CodeWriter codeWriter = new CodeWriter(file.getName());
            writeFile(parser, codeWriter);
        } else {
//            directory logic
            CodeWriter codeWriter = new CodeWriter(file.getName());
            List<File> files = Arrays.asList(file.listFiles());
            for (File vmFile : files) {
                String fileExtension = vmFile.getName().split("\\.")[1];
                if (fileExtension.equals("vm")) {
                    codeWriter.setCurrentFile(vmFile.getName().split("\\.")[0]);
                    Parser parser = new Parser(file.getName(), vmFile.getName());

                    writeFile(parser, codeWriter);
                }
            }
        }

    }

    public static void writeFile(Parser parser, CodeWriter codeWriter) {
        while (parser.getLines().hasNext()) {
            parser.advance();
            if (parser.getCommandType() == Parser.Command.ARITHMETIC) {
                codeWriter.writeArithmetic(parser.getCurrent());
            } else if (parser.getCommandType() == Parser.Command.PUSH || parser.getCommandType() == Parser.Command.POP) {
                codeWriter.writePushPop(parser.getCommandType(), parser.arg1(), parser.arg2());
            } else if (parser.getCommandType() == Parser.Command.LABEL || parser.getCommandType() == Parser.Command.GOTO || parser.getCommandType() == Parser.Command.IF) {
                codeWriter.writeLabelGotoIf(parser.getCommandType(), parser.arg1());
            } else if (parser.getCommandType() == Parser.Command.FUNCTION || parser.getCommandType() == Parser.Command.CALL) {
                codeWriter.writeFunctionCalls(parser.getCommandType(), parser.arg1(), parser.arg2());
            } else if (parser.getCommandType() == Parser.Command.RETURN) {
                codeWriter.writeReturn();
            }
        }

        codeWriter.writeToFile();
    }

}
