
public class Main {

    public static void main(String[] args) {
        // write your code here

        if (args.length == 0) {
            System.out.println("Please enter a file name!");
        } else {
            Parser parser = new Parser(args[0]);
            parser.iterate();
        }
    }
}
