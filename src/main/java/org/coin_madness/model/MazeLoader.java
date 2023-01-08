package org.coin_madness;

import org.coin_madness.model.Field;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MazeLoader {

    public Field[][] load(String filename, String separator) throws IOException {
        ArrayList<String[]> layout = readMap(filename, separator);
        int height = layout.size();
        int width = layout.get(0).length;
        int id = 0;

        Field[][] fields = new Field[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Field field = new Field(id, x, y);

                if(Objects.equals(layout.get(y)[x], "1")) {
                    field.setWall(true);
                }

                fields[y][x] = field;
                id++;
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
