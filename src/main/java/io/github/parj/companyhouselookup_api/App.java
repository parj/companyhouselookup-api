package io.github.parj.companyhouselookup_api;

import io.github.parj.companyhouselookup.Constants;
import io.github.parj.companyhouselookup.Utility;
import io.github.parj.companyhouselookup.company.GetCompany;
import io.github.parj.companyhouselookup.company.Search;
import io.muserver.*;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;

public class App<lastLivenessCheck> {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    static long lastLivenessCheck = System.currentTimeMillis();
    static boolean runOnce = false;

    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
                .addHandler(ContextHandlerBuilder.context("lookup")
                    .addHandler(Method.GET, "/company/get/{companyNumber}", (request, response, pathParams) -> {
                        String companyNumber = pathParams.get("companyNumber");
                        GetCompany getCompany = new GetCompany();
                        JsonNode node = getCompany.get(companyNumber);
                        response.contentType("application/json");
                        response.write(node.toString());
                    })
                    .addHandler(Method.GET, "/company/search", (request, response, pathParams) -> {
                        String query = request.query().get("q");
                        Search search = new Search();
                        JsonNode node = search.search(query);
                        response.contentType("application/json");
                        response.write(node.toString());
                    })
                    .addHandler(Method.GET, "/health", (request, response, pathParams) -> {
                        response.contentType("application/json");
                        response.write("{Status: UP}");
                    })
                    .addHandler(Method.GET, "/health/details", (request, response, pathParams) -> {
                        MuStats stats = request.server().stats();
                        long completedRequests = stats.completedRequests();
                        long bytesRead = stats.bytesRead();
                        long bytesSent = stats.bytesSent();
                        response.contentType("application/json");
                        JSONObject json = new JSONObject();
                        json.put("Status", "UP");
                        json.put("CompletedRequests", String.valueOf(completedRequests));
                        json.put("BytesRead", String.valueOf(bytesRead));
                        json.put("BytesSent", String.valueOf(bytesSent));
                        response.write(json.toString());
                    })
                    .addHandler(Method.GET, "/health/liveness", (request, response, pathParams) -> {
                        //To prevent spamming the api connectivity
                        if (checkIfOkayToRun()) {
                            response.contentType("application/json");

                            if (isCompanyHouseLookupReacheable())
                                response.write("{Status: OK, CompanyHouseReachable: TRUE}");
                            else {
                                response.status(Integer.parseInt(Response.Status.GATEWAY_TIMEOUT.toString()));
                                response.write("{Status: \"NOT OK\", CompanyHouseReachable: FALSE}");
                            }
                        } else {
                            response.write("{Status: OK, CompanyHouseReachable: \"NOT CHECKED\" }");
                        }
                    })
                )
                .withHttpPort(8888)
                .start();
        logger.info("Started server at " + server.uri());

        if (System.getenv("API_COMPANYHOUSE_KEY") == null || System.getenv("API_COMPANYHOUSE_KEY").isEmpty() )
            logger.error("Company house lookup API has not been set. To set - environment variable API_COMPANYHOUSE_KEY must be set");
    }

    public static boolean checkIfOkayToRun() {
        if (!runOnce || System.currentTimeMillis() - lastLivenessCheck > 120000 ) {
            runOnce = true;
            lastLivenessCheck = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static boolean isCompanyHouseLookupReacheable() throws IOException, URISyntaxException {
        int port = 443;
        boolean isAlive = false;
        String host = new URI(Constants.COMPANYHOUSE_URL).getHost();
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        Socket socket = new Socket();

        // Timeout required - it's in milliseconds
        int timeout = 30000;
        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;
            logger.debug("Connection to " + host + ":" + port + " was successful");
        } catch (SocketTimeoutException exception) {
            logger.error("SocketTimeoutException " + host + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            logger.error(
                    "IOException - Unable to connect to " + host + ":" + port + ". " + exception.getMessage());
        }

        return isAlive;
    }

}
