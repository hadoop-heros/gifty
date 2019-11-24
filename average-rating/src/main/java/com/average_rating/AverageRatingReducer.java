package main.java.com.average_rating;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AverageRatingReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterable<IntWritable> values, Context context) {
        int count = 0;
        int total = 0;
        while (values.iterator().hasNext()) {
            total += values.iterator().next().get();
            count++;
        }
        int average = total / count;
        try {
            context.write(key, new IntWritable(average));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
