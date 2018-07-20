import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Parser {
    private Iterator<String> lines = null;
    private String current;

    public enum Command {
        ARITHMETIC, PUSH, POP, LABEL, GOTO, IF, FUNCTION, RETURN, CALL
    }

    public Parser(String fileName) {
        this.lines = readFile(Paths.get("files", fileName));
    }


    public void iterate() {
        while (lines.hasNext()) {
            advance();
            System.out.println(getCommandType());
            System.out.println(arg1());
            if (arg2() != null) {
                System.out.println(arg2());
            }
        }

    }

    public void advance() {
        while (hasMoreCommands()) {
            String line = lines.next();
            if (!line.startsWith("//") && !line.equalsIgnoreCase("")) {
                current = line;
                break;
            }
        }
    }



    public Command getCommandType() {
        String command = current.split(" ")[0];
        List<String> arithmetic = Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
        if (command.equalsIgnoreCase("push")) {
            return Command.PUSH;
        } else if (command.equalsIgnoreCase("pop")) {
            return Command.POP;
        } else if (arithmetic.contains(command)) {
            return Command.ARITHMETIC;
        }
        return null;
    }

    public String arg1() {
        if (getCommandType() == Command.ARITHMETIC) {
            return current;
        } else if (getCommandType() == Command.RETURN) {
            return null;
        } else {
            return (current.split(" ")[1]);

        }
    }

    public Integer arg2() {
        if (getCommandType() == Command.POP || getCommandType() == Command.PUSH ||
                getCommandType() == Command.FUNCTION || getCommandType() == Command.CALL){
            return Integer.parseInt(current.split(" ")[2]);
        }
        return null;
    }

    private boolean hasMoreCommands() {
        return this.lines.hasNext();
    }


    private Iterator<String> readFile(Path fileName) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(fileName);
        } catch (IOException e) {
            System.out.println(e);
        }
        return lines.iterator();
    }

    public Iterator<String> getLines() {
        return lines;
    }

    public void setLines(Iterator<String> lines) {
        this.lines = lines;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }
}
