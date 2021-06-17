package edu.escuelaing.arsw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ReqestClient implements Runnable{
    private Socket socketClient;
    public ReqestClient(Socket socketClient){
        this.socketClient = socketClient;
    }
    public String createResponse(String path){
        String type = "text/html";
        if(path.endsWith(".css")){
            type = "text/css";
        } else if(path.endsWith(".js") ){
            type = "text/javascript";
        }
        else if(path.endsWith(".jpeg")){
            type = "image/jpeg";
        }else if(path.endsWith(".png")){
            type = "image/png";
        }
        //para leer archivos
        Path file = Paths.get("./www"+path);
        Charset charset = Charset.forName("UTF-8");
        String outmsg ="";
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                outmsg = outmsg + line;
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: "+type+"\r\n"
                + "\r\n"+ outmsg;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
            BufferedReader in = null;
            in = new BufferedReader(
                    new InputStreamReader(socketClient.getInputStream()));
            String inputLine, outputLine;
            String method="";
            String path = "";
            String version = "";
            List<String> headers = new ArrayList<String>();
            while ((inputLine = in.readLine()) != null) {
                if(method.isEmpty()){
                    String[] requestStrings = inputLine.split(" ");
                    method = requestStrings[0];
                    path = requestStrings[1];
                    version = requestStrings[2];
                    System.out.println("reques: "+method +" "+ path + " "+ version);
                } else{
                    System.out.println("header: "+inputLine);
                    headers.add(inputLine);
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            String responseMessage = createResponse(path);
            out.println(responseMessage);
            out.close();
            in.close();
            socketClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
