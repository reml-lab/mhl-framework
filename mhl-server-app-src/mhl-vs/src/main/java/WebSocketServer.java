/**
 * Created by erikrisinger on 4/16/17.
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSocketServer {

    public static void main(String[] args) throws Exception {
        // args[0]: port number
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);

        System.out.println("listening on port " + port);


        // these won't be args but files/config...
        // args[1]: authorized users

        // args[2]: acceptable hostnames

        // args[3]: secret key

        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MHLWebSocketHandler.class);
            }
        };

        server.setHandler(wsHandler);
        server.start();
        server.join();
    }
}