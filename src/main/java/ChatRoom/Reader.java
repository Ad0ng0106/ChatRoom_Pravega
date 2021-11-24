package ChatRoom;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.impl.JavaSerializer;

import java.net.URI;

public class Reader {

    public static ReaderGroupManager createReaderGroup(String url, String scope, String stream, String groupName) throws Exception {
        URI controllerURI = new URI(url);
        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, clientConfig);
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder().stream(scope + "/" + stream).build();
        readerGroupManager.createReaderGroup(groupName, readerGroupConfig);
        return readerGroupManager;
    }

    public static EventStreamReader<String> createReader(String url, String scope, String readerName, String groupName)
            throws Exception {
        URI controllerURI = new URI(url);
        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);
        ReaderConfig readerConfig = ReaderConfig.builder().build();
        EventStreamReader<String> reader = clientFactory.createReader(readerName, groupName, new JavaSerializer<>(), readerConfig);
        return reader;
    }


    public static EventStreamReader<byte[]> createBytesReader(String url, String scope, String readerName, String groupName) throws Exception {
        URI controllerURI = new URI(url);
        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);
        ReaderConfig readerConfig = ReaderConfig.builder().build();
        EventStreamReader<byte[]> reader = clientFactory.createReader(readerName, groupName, new JavaSerializer<>(), readerConfig);
        return reader;
    }

    public static void readHistoryData(EventStreamReader<String> reader, String selfName) {
        while (true) {
            String event = reader.readNextEvent(1000).getEvent();
            if (event == null) {
                break;
            }
            if (event.startsWith(selfName + ": ")){
                System.out.print("You(" + selfName + ")");
                System.out.println(event.substring(selfName.length()));
            }
            else {
                System.out.println(event);
            }
        }
    }
}
