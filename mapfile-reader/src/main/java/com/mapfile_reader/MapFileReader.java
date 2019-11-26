package main.java.com.mapfile_reader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;

public class MapFileReader {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        FileSystem fs;
        Text txtKey = new Text(args[1]);
        Text txtValue = new Text();
        MapFile.Reader reader;
        try {
            fs = FileSystem.get(conf);
            try {
                reader = new MapFile.Reader(fs, args[0], conf);
                reader.get(txtKey, txtValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("The value for Key " + txtKey.toString() + " is " + txtValue.toString());
    }
}