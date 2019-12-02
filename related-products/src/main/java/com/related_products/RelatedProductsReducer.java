package main.java.com.related_products;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class RelatedProductsReducer extends Reducer<Text, TextArrayWritable, Text, TextArrayWritable> {
    public void reduce(Text key, Iterable<TextArrayWritable> values, Context context) {
        TextArrayWritable output = values.iterator().next();
        try {
            context.write(key, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}