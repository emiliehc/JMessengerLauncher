package jmessenger.client;

import javax.swing.*;
import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] options = new String[]{"Restart in Safe Mode", "Restart", "No"};
        String arg = "";
        while (true) {
            Process p = Runtime.getRuntime().exec("java -jar client/JMessenger.jar " + arg);
            int exit = p.waitFor();
            if (exit == 0) {
                break;
            }
            int result = JOptionPane.showOptionDialog(null, "The program has terminated abnormally. Would you like to start over?\nExit code: " + exit, "Message", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (result == 0) {
                // safe mode
                arg = "--safemode";
            } else if (result == 1) {
                // restart
                arg = "";
            } else {
                // return
                break;
            }
        }
    }
}
