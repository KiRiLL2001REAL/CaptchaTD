package cw.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.ini4j.Wini;

public class HelpConfiguration {
    String title;
    ArrayList<String> strings;
    ArrayList<Integer> stringsIndices;
    ArrayList<String> images;
    ArrayList<Integer> imagesIndices;

    public HelpConfiguration(
            String title,
            ArrayList<String> strings,
            ArrayList<String> images,
            ArrayList<Integer> stringsIndices,
            ArrayList<Integer> imagesIndices
    ) {
        this.title = title;
        this.strings = strings;
        this.stringsIndices = stringsIndices;
        this.images = images;
        this.imagesIndices = imagesIndices;
    }

    public HelpConfiguration(
            String iniFile
    ) {
        try (InputStream is = getClass().getResourceAsStream(iniFile)) {
            String[] strIndices;

            Wini ini = new Wini(is);
            // reading title
            this.title = ini.get("title", "name", String.class);
            // reading strings indices
            strIndices = ini.get("content", "stringsIndices", String.class).split(",");
            this.stringsIndices = new ArrayList<>();
            for (String strIndex : strIndices)
                this.stringsIndices.add(Integer.parseInt(strIndex.trim()));
            // reading images indices
            strIndices = ini.get("content", "imagesIndices", String.class).split(",");
            this.imagesIndices = new ArrayList<>();
            for (String strIndex : strIndices)
                this.imagesIndices.add(Integer.parseInt(strIndex.trim()));

            // reading elements
            this.strings = new ArrayList<>();
            this.images = new ArrayList<>();
            int countElements = this.imagesIndices.size() + this.stringsIndices.size();
            for (int i = 0; i < countElements; i++) {
                String content = ini.get("content", "element" + i, String.class);
                if (this.stringsIndices.contains(i)) {
                    content = content.replace("\\n", "\n");
                    content = content.replace("\\t", "\t");
                    this.strings.add(content);
                }
                else if (this.imagesIndices.contains(i))
                    this.images.add(content);
                else
                    throw new IOException("Incorrect element enumeration: id " + i + " is not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public ArrayList<Integer> getStringsIndices() {
        return stringsIndices;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public ArrayList<Integer> getImagesIndices() {
        return imagesIndices;
    }
}
