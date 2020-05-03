package io.github.parj.companyhouselookup_api;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;

import io.github.parj.companyhouselookup.company.GetCompany;
import io.github.parj.companyhouselookup.company.Search;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
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
                    System.out.println(query);
                })
                .withHttpPort(8888)
                .start();
        logger.info("Started server at " + server.uri());
    }
}
