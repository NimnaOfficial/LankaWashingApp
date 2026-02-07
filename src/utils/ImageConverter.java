package utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ImageConverter {
    public static void main(String[] args) {
        try {
            // 1. Print where Java is looking for files
            String currentDir = System.getProperty("user.dir");
            System.out.println("📂 Current Working Directory: " + currentDir);

            // 2. Try to find the file in the project root
            // Make sure you pasted img22.jpeg into the folder printed above!
            File file = new File("src/images/img22.jpeg");

            // Check if it exists before trying to read
            if (!file.exists()) {
                System.err.println("❌ Error: File not found at: " + file.getAbsolutePath());
                System.err.println("👉 FIX: Please copy 'img22.jpeg' into the folder shown above.");
                return;
            }

            // 3. Convert to Base64
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            System.out.println("\n✅ SUCCESS! Copy the string below (it's very long):");
            System.out.println("--------------------------------------------------");
            System.out.println(encodedString);
            System.out.println("--------------------------------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}