package jmessenger.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Launcher {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private static void downloadJMessenger(URL url) throws IOException {
        InputStream in = url.openStream();
        Files.copy(in, Paths.get("client/JMessenger.jar"), StandardCopyOption.REPLACE_EXISTING);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws InterruptedException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }
        try {
            // check for new updates
            JSONObject json = readJsonFromUrl("https://api.github.com/repos/njchensl/JMessenger/releases/latest");
            String version = json.get("tag_name").toString();
            // check the current version
            String myVersion = "";
            try {
                File f = new File("client/info");
                Scanner sc = new Scanner(f);
                myVersion = sc.nextLine();
                sc.close();
            } catch (IOException ignored) {
                // treat it as not up to date
            }
            // check if up to date
            if (!version.equals(myVersion)) {
                // not up to date
                System.out.println("not up to date");
                JSONArray assets = json.getJSONArray("assets");
                JFrame frame = new JFrame("Please wait");
                frame.setPreferredSize(new Dimension(300, 300));
                frame.add(new JLabel("Updating JMessenger") {{
                    setFont(getFont().deriveFont(20f));
                    setHorizontalTextPosition(CENTER);
                    setVerticalTextPosition(CENTER);
                }});
                frame.pack();
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setVisible(true);
                // find the link to download only JMessenger.jar
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject release = assets.getJSONObject(i);
                    String link = release.get("browser_download_url").toString();
                    // if the link contains the download for only JMessenger.jar
                    if (link.contains("JMessenger.jar")) {
                        // download it
                        URL url = new URL(link);
                        downloadJMessenger(url);
                    }
                }
                // write the new version to the file
                File f = new File("client/info");
                if (!f.exists()) {
                    f.createNewFile();
                }
                BufferedWriter out = new BufferedWriter(new PrintWriter(new FileOutputStream(f)));
                out.write(version);
                out.flush();
                out.close();
                // close the frame
                frame.dispose();
            } else {
                System.out.println("up to date");
            }
        } catch (IOException | NoSuchElementException e) {
            JOptionPane.showMessageDialog(null, "Unable to check for new updates", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            String[] options = new String[]{"Restart in Safe Mode", "Restart", "No", "Check Runtime Version and Restart"};
            String arg = "";
            while (true) {
                Process p = Runtime.getRuntime().exec("java -jar client/JMessenger.jar " + arg);
                int exit = p.waitFor();
                if (exit == 0) {
                    System.exit(0);
                }
                int result = JOptionPane.showOptionDialog(null, "The program has terminated abnormally. Would you like to start over?\nExit code: " + exit, "Message", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (result == 0) {
                    // safe mode
                    arg = "--safemode";
                } else if (result == 1) {
                    // restart
                    arg = "";
                } else if (result == 3) {
                    // check runtime version
                    ProcessBuilder   ps=new ProcessBuilder("java.exe","-version");
                    ps.redirectErrorStream(true);
                    Process pr = ps.start();
                    BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line;
                    StringBuilder info = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                        info.append(line).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, info, "Java Version Check", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // return
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to start the messenger", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
    }
}
