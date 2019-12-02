package main.java.com.product_metadata;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ProductMetadataReducer extends Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterable<Text> values, Context context) {
        Text output = values.iterator().next();
        try {
            context.write(key, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
