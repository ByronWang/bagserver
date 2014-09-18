import java.util.*;
import java.io.*;

public class Test {
    public static void main(String[] args) throws Exception { 
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
           String s = stdin.readLine();        
        if(s.length()%2==0)
            System.out.println(showMsg1()); 
        else
            System.out.println(showMsg2());
        Flight a = new Flight();
        a.setName(s);
        System.out.println(a.output());
        Bomber b = new Bomber();
        b.setName(s);
        System.out.println(b.output());
        s = stdin.readLine();
        StringTokenizer st = new StringTokenizer(s);
        int n = Integer.parseInt(st.nextToken()); 
        System.out.println(compute(n));
    }
    
    public static String showMsg1() { 
        return "You are my sun1"; 
    }
    
    public static String showMsg2() { 
        return "You are my sun2"; 
    }
    
    public static int compute(int n) { 
        if(n>1)
            return n*compute(n-1); 
        else
            return 1;
    }
    
    static class Flight{
        public Flight(){
        }
        
        public String output(){
            return this.name;
        }
        
        public void setName(String name){
            this.name="Flight:"+name;
        }
        
        private String name;
    }
    
    static class Bomber{
        public Bomber(){
        }
        
        public String output(){
            return this.name;
        }
        
        public void setName(String name){
            this.name="Bomber:"+name;
        }
        
        private String name;
    }
}