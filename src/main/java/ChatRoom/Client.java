package ChatRoom;

import java.util.Arrays;

public class Client {
    public static void main(String[] args) throws Exception {
        if(args == null || args.length <2) {
            System.out.println("self and peer(s) are required");
            System.exit(1);
        }
        String selfName = args[0].trim();
        String[] peerNames = Arrays.copyOfRange(args, 1, args.length);
        for (int i = 0; i < peerNames.length; i ++){ peerNames[i] = peerNames[i].trim(); }

//        The files received by a user are stored in directory <folder>\<user's name>\

        String folder = "C:\\Users\\lenovo\\Desktop\\";
        Chat chat = new Chat(selfName, peerNames, folder);
        chat.startChat();
    }
}
