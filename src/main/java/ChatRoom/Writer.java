package ChatRoom;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;

import java.net.URI;

public class Writer {

    public static EventStreamWriter<String> getWriter(String url, String scope, String stream) throws Exception {
        URI controllerURI = new URI(url);
        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);
        EventWriterConfig writerConfig = EventWriterConfig.builder().build();
        EventStreamWriter<String> eventWriter = clientFactory.createEventWriter(stream, new JavaSerializer<>(), writerConfig);
        return eventWriter;
    }

    public static EventStreamWriter<byte[]> getBytesWriter(String url, String scope, String stream) throws Exception {
        URI controllerURI = new URI(url);
        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);
        EventWriterConfig writerConfig = EventWriterConfig.builder().build();
        EventStreamWriter<byte[]> eventWriter = clientFactory.createEventWriter(stream, new JavaSerializer<>(), writerConfig);
        return eventWriter;
    }

    public static void createStream(String url, String scope, String stream) throws Exception {
        URI controllerURI = new URI(url);
        StreamManager streamManager = StreamManager.create(controllerURI);
        streamManager.createScope(scope);
        StreamConfiguration config = StreamConfiguration.builder().build();
        streamManager.createStream(scope, stream, config);
        streamManager.close();
    }
}

