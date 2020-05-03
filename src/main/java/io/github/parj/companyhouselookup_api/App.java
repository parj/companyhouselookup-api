package io.github.parj.companyhouselookup_api;

import io.github.parj.companyhouselookup.company.GetCompany;
import io.github.parj.companyhouselookup.company.Search;
import io.muserver.*;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

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
                    }))
                .withHttpPort(8888)
                .start();
        logger.info("Started server at " + server.uri());

        if (System.getenv("API_COMPANYHOUSE_KEY") == null || System.getenv("API_COMPANYHOUSE_KEY").isEmpty() )
            logger.error("Company house lookup API has not been set. To set - environment variable API_COMPANYHOUSE_KEY must be set");
    }
}
