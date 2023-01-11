package org.coin_madness.helpers;

import org.coin_madness.model.Field;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MazeLoader {

    public Field[][] load(String filename, String separator) throws IOException {
        ArrayList<String[]> layout = readMap(filename, separator);
        int height = layout.size();
        int width = layout.get(0).length;

        Field[][] fields = new Field[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Field field = new Field(x, y);

                if(layout.get(x)[y].contains("1")) {
                    field.setWall(true);
                }

                fields[x][y] = field;
            }
        }
        return fields;
    }

    private ArrayList<String[]> readMap(String filename, String separator) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<String[]> mapLayout = new ArrayList<>();
        String line = br.readLine();

        while(line != null) {
            String[] row = line.split(separator);
            mapLayout.add(row);
            line = br.readLine();
        }
        return mapLayout;
    }

}
