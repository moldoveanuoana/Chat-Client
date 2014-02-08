/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package srvcln;


import java.net.*; import java.util.*; import java.io.*;

class Cln {  // Clasa Client, cel ce se va conecta la Server
    // Clasa client are doua functionalitati, sa trimita si sa primeasca mesaje
    // Logic nu se pot face ambele simultan pe acelasi fir de executie, astfel
    // in programul de mai jos vom pastra firul principal pentru a trimite mesaje catre server
    // si vom crea un Thread nou pentru a primi mesaje de la server
    
    
  public static void main(String[] args) throws Exception {
    boolean OK  = false;
    try{
        final  Scanner sc = new Scanner(System.in); 
        System.out.print("Username: ");
        final String user = sc.next();
        System.out.print("\nAdresa serverului si portul : ");
        Socket cs = null;
        try{
            cs = new Socket( sc.next(), sc.nextInt() ); // instantiem un socket la adresa si portul citit de la tastatura
             // socket-ul va fi obiectul ce se va conecta la server si prin intermediul caruia vom transmite si primi date
        } 
        catch(Exception e){
            OK = true;
        }
        DataOutputStream os = new DataOutputStream( cs.getOutputStream()); // declaram un obiect de tipul DataOutputStream
        // ce va lucra cu fluxul de iesire al socket-ului
        
        final DataInputStream is = new DataInputStream( cs.getInputStream()); // analog pentru fluxul de intrare
        
        
        Thread T= new Thread(new Runnable(){  // declaram si instantiam Thread-ul ce se va ocupa cu primirea
         // mesajelor de la server
        public void run() { // metoda ce va fi apelata cand thread-ul este pornit
              while (true) {// blocam firul printr-un loop infinit ce primeste mesaje de la server
                  try {
                      String s = is.readUTF();   // primim mesajul de la server
                      if(s.equals("Pa"))
                          System.exit(0);
                      System.out.println(s); // afisam ce am primit
                  } 
       catch (IOException ex) {
                  }
                 
              }
          }
      });
      T.start(); // pornim threadul 
      os.writeUTF(user);//trimitem username-ul serverului
      while (true) { // blocam firul principal cu un loop infinit care citeste de la tastatura mesaje
          // si le trimite prin DataOutputStream in fluxul de iesire al socket-ului catre server
          
          String st = sc.nextLine();
          if(st.compareTo("")!= 0)
              os.writeUTF(st);
      }
    }
    catch(Exception e){
        if(OK)
            System.out.println("Adresa sau portul nu au fost introduse corect!");
        else{
             System.out.println(e.getMessage());
             System.exit(0);
        }
           
    }
    
    
  }
}