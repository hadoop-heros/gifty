package main.java.com.mapfile_writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile.Writer;
import org.apache.hadoop.io.Text;

public class MapFileWriter {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        Path inputFile = new Path(args[0]);
        Path outputFile = new Path(args[1]);
        Text txtKey = new Text();
        Text txtValue = new Text();
        try {
            FileSystem fs = FileSystem.get(conf);
            FSDataInputStream inputStream = fs.open(inputFile);
            Writer writer = new Writer(conf, fs, outputFile.toString(), txtKey.getClass(), txtKey.getClass());
            writer.setIndexInterval(1);
            while (inputStream.available() > 0) {
                String strLineInInputFile = inputStream.readLine();
                String[] lstKeyValuePair = strLineInInputFile.split("\\t");
                txtKey.set(lstKeyValuePair[0]);
                txtValue.set(lstKeyValuePair[1]);
                writer.append(txtKey, txtValue);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}