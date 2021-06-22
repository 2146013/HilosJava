package edu.escuelaing.arsw;

import com.sun.xml.internal.ws.api.ha.StickyFeature;

import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer implements Runnable{
    protected ServerSocket serverSocket = null;
    protected Thread thread = null;
    protected ExecutorService foxedPool = Executors.newFixedThreadPool(17);
    private boolean running = true;
    private static HttpServer _instance = new HttpServer();
    private HttpServer(){
        int port = getPort();
    }
    private static HttpServer getInstance(){
        return _instance;
    }
    public static void main(String... args) throws IOException{
        new Thread(HttpServer.getInstance()).start();
        try {
            Thread.sleep(200000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        HttpServer.getInstance().stop();
    }

    private void stop() {
        this.running = false;
        try{
            this.serverSocket.close();
        }catch (IOException e){
            throw new RuntimeException("Error closing server",e);
        }
    }

    public  void startServer() throws IOException {

        int port =35000;
        Socket clientSocket = null;
            try {
                serverSocket = new ServerSocket(port);
                while (running){
                System.out.println("Listo para recibir en puerto ..."+port);
                clientSocket = serverSocket.accept();
                ReqestClient reqestClient = new ReqestClient(clientSocket);
                foxedPool.execute(reqestClient);
                }
                this.foxedPool.shutdown();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
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
            synchronized (this){
                this.thread = Thread.currentThread();
            }
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 36000; //returns default port if heroku-port isn't set(i.e. on localhost)
    }
}

