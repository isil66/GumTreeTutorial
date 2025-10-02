public class Main {
   

    static void moveMe()   { 
            System.out.println("moveMe_top");
    }

    static void georgeWashington() { 
            System.out.println("renameMe"); 
    }

    public static int mathAdd(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        georgeWashington();
        mathAdd();
        moveMe();
    }
}

