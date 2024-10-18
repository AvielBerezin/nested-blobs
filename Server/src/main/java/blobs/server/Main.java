package blobs.server;

import blobs.server.network.WebSocketServerForNetworkListener;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/main.js", new MainJsHandler());
        server.setExecutor(null);
        server.start();
        WebSocketServerForNetworkListener webSocketServer = new WebSocketServerForNetworkListener(new InetSocketAddress(81), new BlobsOverTheNetwork());
        webSocketServer.run();
    }

    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pageFile = Main.class.getClassLoader().getResource("Client/index.html").getFile();
            File file = new File(pageFile);
            InputStream is = new FileInputStream(file);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            is.transferTo(os);
            os.close();
        }
    }

    static class MainJsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pageFile = Main.class.getClassLoader().getResource("Client/main.js").getFile();
            File file = new File(pageFile);
            InputStream is = new FileInputStream(file);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            is.transferTo(os);
            os.close();
        }
    }
}
