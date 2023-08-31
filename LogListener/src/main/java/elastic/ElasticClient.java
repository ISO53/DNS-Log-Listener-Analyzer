package elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import utils.NetworkInfo;

import java.io.IOException;

public class ElasticClient {

    private static final String SERVER_URL = "http://localhost:9200";
    private static final String API_KEY = "pass";

    public static void main(String[] args) {

        // Create the low-level client
        RestClient restClient = RestClient
                .builder(HttpHost.create(SERVER_URL))
                .build();

        // Create the transport with jackson mapper
        ElasticsearchTransport elasticsearchTransport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // Create the api client
        ElasticsearchClient esClient = new ElasticsearchClient(elasticsearchTransport);

        NetworkInfo networkInfo = new NetworkInfo("89.207.14.192");

        IndexResponse response = null;
        try {
            response = esClient.index(i -> i
                    .index("trials")
                    .id(networkInfo.getIp())
                    .document(networkInfo));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Indexed with version " + (response != null ? response.version() : "ERROR"));

        try {
            GetResponse<NetworkInfo> getResponse = esClient.get(g -> g
                    .index("trials")
                    .id("89.207.14.192"),
                    NetworkInfo.class);

            if (getResponse.found()) {
                NetworkInfo info = getResponse.source();
                System.out.println(info.getHostname());
            } else {
                System.out.println("Product not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
