package ChatRoom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class FileConverter {
    public static byte[] f2bytes(String dir){
        File file = new File(dir);
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.out.println("Failed to open the file.\n");
            return null;
        }
        return fileContent;
    }

    public static void bytes2f(String dir, byte[] fileContent){
        File f = new File(dir);
        try {
            Files.write(f.toPath(), fileContent);
        } catch (IOException e) {
            return;
        }
    }

    /**
     * remove the "upload@" in the line message.
     */
    public static String getFileName(String line){
        return line.substring(line.lastIndexOf("\\")+1);
    }
    public static byte[] str2bytes(String str) {
        return str.getBytes();
    }
    public static String bytes2str(byte[] bytes){
        return new String(bytes);
    }
}

