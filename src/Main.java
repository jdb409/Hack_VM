
public class Main {

    public static void main(String[] args) throws Throwable {
        // write your code here

        if (args.length == 0) {
            throw new Throwable("Please enter a file name!");
        }

        Parser parser = new Parser(args[0]);
        CodeWriter codeWriter = new CodeWriter(args[0]);

        while (parser.getLines().hasNext()) {
            parser.advance();
            if (parser.getCommandType() == Parser.Command.ARITHMETIC){
                codeWriter.writeArithMetic(parser.getCurrent());
            } else {
                codeWriter.writePushPop(parser.getCommandType(), parser.arg1(), parser.arg2());
            }
        }

        codeWriter.writeToFile();
    }
}
