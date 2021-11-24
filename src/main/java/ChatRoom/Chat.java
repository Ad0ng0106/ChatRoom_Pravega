package ChatRoom;

import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;

import java.util.Arrays;
import java.util.Scanner;

/**
 * The chat room is implemented with two threads, one for receiving messages and the other for sending messages.
 * Both private chat and group chat use two streams. One for sending and receiving messages and the other for sending and receiving files.
 * Messages are written into stream and read from stream directly by the string stream writer and string stream reader.
 * Files are converted to byte[] and sent and received with byte[] stream writer and byte[] stream reader.
 */
public class Chat {
    String selfName;
    String[] peerNames;
    String controller;
    String scope;
    String fileDir;

    EventStreamWriter<String> writer;
    EventStreamReader<String> reader;
    ReaderGroupManager readerGroupManager;

    EventStreamWriter<byte[]> fileWriter;
    EventStreamReader<byte[]> fileReader;
    ReaderGroupManager fileReaderGroupManager;

    boolean isEnd = false;

    class Sender implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (true) {

                String line = scanner.nextLine();


//                If a file is to be uploaded, the byte[] writer writes the file itself and then writes a converted string indicating the file name.
                if (line.startsWith("upload@")){
                    byte[] bytes = FileConverter.f2bytes(line.substring(7));
                    if (bytes != null) {
                        fileWriter.writeEvent(bytes);
                        String fileName = FileConverter.getFileName(line);
                        writer.writeEvent(selfName + " uploaded " + fileName);
                        fileWriter.writeEvent(FileConverter.str2bytes(selfName + ": " + fileName));
                    }
                }

//                send message
                else{
                    String message = selfName + ": " + line;
                    writer.writeEvent(message);

//                    Chat ends if "bye" is written
                    if (message.equals(selfName + ": bye")) {
                        writer.close();
                        fileWriter.close();
                        scanner.close();
                        isEnd = true;
                        return;
                    }
                }
                System.out.print("You(" + selfName + "): ");
            }
        }
    }

    class Reiceiver implements Runnable {
        @Override
        public void run() {

            Thread.currentThread();
            Reader.readHistoryData(reader, selfName);
            System.out.print("You(" + selfName + "): ");

            while (!isEnd) {
                String event = reader.readNextEvent(1000).getEvent();
                if (event != null && !event.startsWith(selfName+": ")) {
                    System.out.println();
                    System.out.println(event);
                    System.out.print("You(" + selfName + "): ");
                }

//                If a file is detected, the byte[] reader first collects the file itself and then collects the byte[] file name.
                byte[] fileEvent = fileReader.readNextEvent(1000).getEvent();
                if (fileEvent != null){
                    byte[] fileNameBytes = null;
                    while (fileNameBytes == null){ fileNameBytes = fileReader.readNextEvent(1000).getEvent(); }
                    String fileName = FileConverter.bytes2str(fileNameBytes);
                    String sender = fileName.substring(0,fileName.indexOf(":"));
                    fileName = fileName.substring(fileName.indexOf(" ")+1);
                    FileConverter.bytes2f(fileDir + fileName, fileEvent);
                    if (!sender.equals(selfName)) {
                        System.out.println();
                        System.out.println("Received file " + fileName + " from " + sender);
                        System.out.print("You(" + selfName + "): ");
                    }
                }

            }
            closeChatReader();
            System.out.println("Chat ends.");
        }
    }

    public Chat(String selfName, String[] peerNames, String folder) throws Exception{
        this.selfName = selfName;
        this.peerNames = peerNames;
        controller = "tcp://127.0.0.1:9090";
        scope = "ChatRoom";
        fileDir = folder + selfName + "\\";
        String stream = getStreamName(peerNames);

        Writer.createStream(controller, scope, stream);
        writer = Writer.getWriter(controller, scope, stream);
        readerGroupManager = Reader.createReaderGroup(controller, scope, stream, selfName);
        reader = Reader.createReader(controller, scope, selfName, selfName);

        Writer.createStream(controller, scope, stream + "file");
        fileWriter = Writer.getBytesWriter(controller, scope, stream + "file");
        fileReaderGroupManager = Reader.createReaderGroup(controller, scope, stream + "file", selfName + "file");
        fileReader = Reader.createBytesReader(controller, scope, selfName + "file", selfName + "file");
    }

    public void startChat(){

        System.out.print("Chat members: you(" + selfName + ")");
        for (String name: peerNames){ System.out.print(", " + name); }
        System.out.println();
        System.out.println("================================================");

        Reiceiver receiver = new Reiceiver();
        Thread readThread = new Thread(receiver);
        readThread.start();

        Sender sender = new Sender();
        Thread writeThread = new Thread(sender);
        writeThread.start();
    }

    public void closeChatReader(){
        reader.close();
        readerGroupManager.deleteReaderGroup(selfName);
        readerGroupManager.close();

        fileReader.close();
        fileReaderGroupManager.deleteReaderGroup(selfName+"file");
        fileReaderGroupManager.close();

    }


    /**
     * Name the string by sorting the name of all the chat members and put them together as a whole string.
     */
    private String getStreamName(String[] names){
        String[] namesNew = new String[names.length + 1];
        for (int i = 0; i < names.length; i ++){ namesNew[i] = names[i]; }
        namesNew[names.length] = selfName;
        Arrays.sort(namesNew);
        StringBuffer sb = new StringBuffer();
        for (String name : namesNew){ sb.append(name); }
        return sb.toString();
    }

}
