package blobs.server;

import blobs.client.generate.concrete.CreateClient;
import blobs.server.network.WebSocketServerForNetworkListener;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/god", new ResourceHttpHandler("Client/god.html"));
        server.createContext("/", new ResourceHttpHandler("Client/index.html"));
        server.createContext("/main.js", new GeneratedHttpHandler());
        server.setExecutor(null);
        server.start();
        WebSocketServerForNetworkListener webSocketServer = new WebSocketServerForNetworkListener(new InetSocketAddress(81), new BlobsOverTheNetwork());
        webSocketServer.run();
    }

    static class ResourceHttpHandler implements HttpHandler {
        private final String resource;

        public ResourceHttpHandler(String resource) {
            this.resource = resource;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pageFile = Main.class.getClassLoader().getResource(resource).getFile();
            File file = new File(pageFile);
            InputStream is = new FileInputStream(file);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            is.transferTo(os);
            os.close();
        }
    }

    static class GeneratedHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = CreateClient.createClient().inputStream(0);
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            is.transferTo(os);
            os.close();
        }
    }
}
