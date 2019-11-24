package main.java.com.review_count;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ReviewCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    public void reduce(Text key, Iterable<IntWritable> values, Context context) {
        int count = 0;
        while (values.iterator().hasNext()) {
            values.iterator().next();
            count++;
        }
        try {
            context.write(key, new IntWritable(count));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
