package elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import utils.GlobalLogger;
import watcher.LogEntry;

import java.io.IOException;
import org.apache.logging.log4j.Level;

public class ElasticClient {

    private final RestClient restClient;
    private final ElasticsearchTransport elasticsearchTransport;
    private final ElasticsearchClient elasticsearchClient;

    /**
     * This constructor initializes an instance of the ElasticClient class, which serves as a wrapper for interacting
     * with Elasticsearch. It sets up the necessary Elasticsearch client components to establish a connection to the
     * Elasticsearch cluster specified in the ElasticConstants.SERVER_URL.
     */
    public ElasticClient() {
        // Create the low-level client
        restClient = RestClient
                .builder(HttpHost.create(ElasticConstants.SERVER_URL))
                .build();

        // Create the transport with jackson mapper
        elasticsearchTransport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // Create the api client
        elasticsearchClient = new ElasticsearchClient(elasticsearchTransport);
    }

    /**
     * This method is responsible for sending a log entry to Elasticsearch for indexing. It takes a LogEntry object
     * representing a log event as a parameter. If the indexing operation is successful, the method returns an
     * IndexResponse object. Or else, it returns null.
     *
     * @param logEntry A LogEntry object containing the log data to be indexed.
     * @return An IndexResponse object representing the result of the indexing operation.
     */
    public IndexResponse send(LogEntry logEntry) {
        try {
            return elasticsearchClient.index(i -> i
                    .index(ElasticConstants.LOG_TABLE_NAME)
                    .id(logEntry.getId().toString())
                    .document(logEntry));
        } catch (IOException e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to index entry to ElasticSearch:", e);
        }

        return null;
    }

    /**
     * This method is responsible for gracefully shutting down and releasing resources associated with the Elasticsearch
     * client. It should be called when your application is shutting down or when you no longer need the Elasticsearch
     * client instance.
     */
    public void close() {
        try {
            if (elasticsearchTransport != null) {
                elasticsearchTransport.close();
            }

            if (restClient != null) {
                restClient.close();
            }
        } catch (IOException e) {
            GlobalLogger.getLoggerInstance().log(Level.FATAL, "An error occurred trying to close ElasticSearch channels:", e);
        }
    }
}
