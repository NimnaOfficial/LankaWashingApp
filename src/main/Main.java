package main;

import view.OpenPage;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Ensure the UI is created on the Event Dispatch Thread (Best Practice)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Create the Splash Screen
                OpenPage splash = new OpenPage();
                splash.setVisible(true);

                // 2. Start the loading logic in a separate thread
                // This prevents the UI from freezing while "loading"
                // Ensure OpenPage has a public void startLoading() method!
                new Thread(splash::startLoading).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}