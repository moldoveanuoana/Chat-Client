/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package srvcln;

// importurile necesare
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.logging.Level;
import java.util.logging.Logger;
class Srv {  // Clasa Server, locul la care se vor conecta clientii

  static int i=0;   // numarul de conexiuni de la deschiderea serverului

  public static void main(String[] arg) throws IOException {

    /*   In ArrayList-ul "sockete" vom stoca toate DataOutputStreamurile ce se conecteaza
     * la serverul nostru.
     *   DataOutputStream-ul este clasa ce se ocupa de fluxul de iesire al clientului
     * care s-a conecatat la server. In DataOutputStream-ul fiecarui socket ( client )
     * serverul va scrie mesajul ce doreste sa il transmita.
      */
        boolean OK = false;
        ArrayList<DataOutputStream> sockete = new ArrayList<DataOutputStream>();
        ArrayList<String> listOfUsers = new ArrayList<String>();
        // Obiectele de tip ServerSocket si Socket sunt cele care instantiaza conexiunea dintre Server si Client
        try{
            ServerSocket ss = null;
            Socket cs = null;
            Scanner sc = new Scanner(System.in);
            System.out.print("Portul : ");
            try{
                ss = new ServerSocket( sc.nextInt() );  // instantiam ServerSocket-ul la portul citit de la tastatura
                System.out.println("Serverul a pornit");
            }
            catch(Exception e){
                OK = true;
            }
           while (true) { // Blocam firul principal al serverului cu un lool care asteapta
        // conexiuni de la clienti si pe fiecare iteratie face urmatoarele:
           cs = ss.accept(); // serverul asteaptea urmatoare conexiune de la un socket, iar cand aceasta se intampla
      // stocheaza valoarea in obiectul cs, de tip socket, pe server
           sockete.add(new DataOutputStream(cs.getOutputStream())); // dupa conexiune adaugam DataOutputStream-ul
      // al socket-ului abea conectat, in lista noastra din server
           System.out.println("\nClient nou. ");
           new Conexiune(cs,++i,sockete, listOfUsers); // deschidem un thread nou pentru clientul conectat unde dam ca parametrul
      // socket-ul abea conectat, indicele sau si lista cu ceilalti clienti conectati
           }
        }
        catch(Exception e){
            if(OK)
                System.out.println("Portul introdus nu este valid!");
            else
                System.out.println("A aparut o eroare in retinerea clientului in Server!");
        }
  }

}

class Conexiune extends Thread { // Clasa Conexiune extinde Thread si presupune un proces ce va rula in paralel
    // cu cel principal al serverului, si se va ocupa de primirea si trimiterea mesajelor ce tin
    // de UN SINGUR client ( astfel mai jos regasim variabilele ce ar tine de socket-ul unui client )
    // si lista cu toate DataOutputStreamurile clientilor conectat - > pentru a le putea transmite mesajele mai departe
  int identitate; Socket cs = null; DataInputStream is = null;  DataOutputStream os =null  ;
  boolean isOk = false;
  String user = "";
   ArrayList<DataOutputStream> sockete;
   ArrayList<String> listOfUsers;


  public Conexiune(Socket client, int i,ArrayList<DataOutputStream> sockete,ArrayList<String> listOfUsers) // constructorul clasei in care primim parametrii
           throws IOException {    // pe care i-am trimis mai sus
      cs = client;
      identitate = i;  // atribui socket-ul primit de la server celui local threadu-ului, si identitatea
      is = new DataInputStream(cs.getInputStream());  // fluxul de intrare
      os = new DataOutputStream(cs.getOutputStream());// cel de iesire

      this.sockete = sockete;  // si lista de clienti conectati la server
      this.listOfUsers = listOfUsers;
      user = is.readUTF();
      isOk = test(user);
      start(); // pornim threadu-ul
  }


  private boolean test(String use){
     if(listOfUsers.contains(use))
         return false;
     listOfUsers.add(use);
     return true;
  }

  public void run() { // metoda ce se apleaza la pornirea threadului
      try {
          boolean tr = true;
          while(isOk == false){
                    os.writeUTF("Numele introdus este deja folosit! Introduceti un alt nume!");
                    user = is.readUTF();
                    if(user.length() == 0){
                       user = is.readUTF();
                    }
                    isOk = test(user);
                }
          
          while (tr) { // blocam firul threadului cu un loop ce primeste mesaje de la clien
                String message = is.readUTF(); // citim un mesaj prin fluxul de intrare
                System.out.println(user + ": " + message);  // il afisam
                if( message.compareTo("/LIST") == 0){
                    os.writeUTF("Persoanele online sunt:");
                    for(int i = 0; i<listOfUsers.size(); i++)
                        os.writeUTF(listOfUsers.get(i));
                }
                else
                    if(message.startsWith("/BCAST ") && message.length()>7){
                        if(listOfUsers.size() == 1)
                            os.writeUTF("Doar tu esti online!");
                        else{
                            for(int i=0; i<listOfUsers.size(); i++) 
                                if(listOfUsers.get(i).compareTo(user)!= 0)
                                    sockete.get(i).writeUTF(user + ": " + message.substring(7));
                            
                            os.writeUTF("Mesajul a fost trimis!");
                           }
                    }
                    else
                        if( message.startsWith("/MSG ") && message.length() >7){
                           String [] split=  message.split(" ",3);
                           try{
                            if(split[2].length() >=1)
                                if(listOfUsers.indexOf(split[1]) != -1){
                                    sockete.get(listOfUsers.indexOf(split[1])).writeUTF(user + ": " + split[2]);
                                    os.writeUTF("Mesajul a fost trimis!");
                                }
                                else
                                    os.writeUTF("Nu exista userul dat!");
                           }
                           catch(Exception e){
                               os.writeUTF("Comanda /MSG nu a fost introdusa corespunzator");
                           }
                        }
                        else
                            if(message.startsWith("/NICK ")&& message.length() > 6){
                                String [] newName = message.split(" ", 2);
                                if(listOfUsers.indexOf(newName[1]) != -1)
                                    os.writeUTF("Numele dat este deja folosit! Alegeti-va alt nume");
                                else{
                                    listOfUsers.set(listOfUsers.indexOf(user), newName[1]);
                                    user = newName[1];
                                    os.writeUTF("Numele a fost schimbat cu succes!");
                                }
                            }
                            else
                                if( message.compareTo("/QUIT") == 0){
                                    for(int i = 0; i<listOfUsers.size(); i++)
                                        if(i!= listOfUsers.indexOf(user))
                                            sockete.get(i).writeUTF("Clientul "+ user + " s-a deconectat!");
                                    os.writeUTF("Ai fost delogat cu succes!");
                                    os.writeUTF("Pa");
                                    listOfUsers.remove(listOfUsers.indexOf(user));
                                    sockete.remove(listOfUsers.indexOf(user));
                                    os.close();
                                    cs.close();
                                    tr = false;

                                }
                                else
                                    os.writeUTF("Nu a fost introdusa nici o comanda sau comanda a fost introdusa incorect!");
           
          }
          if(tr == false)
              interrupt();
    }
      catch(IOException ex){
           int index = -1,i;
          for( i = 0; i< listOfUsers.size(); i++)
              if(user.equals(listOfUsers.get(i))){
                  index = i;
                  break;
              }
          if(index != -1 ){
              listOfUsers.remove(index);
              sockete.remove(index);
              System.out.println("S-a pierdut conexiunea cu userul: " + user);
              interrupt();
          }
          else
               System.out.println(ex.getMessage());
      }
    catch(Exception e) {

              System.out.println(e.getMessage());

    }
  }
}
