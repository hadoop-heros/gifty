package main.java.com.average_rating;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.text.DecimalFormat;

public class AverageRatingReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    private DecimalFormat df = new DecimalFormat("0.00");

    public void reduce(Text key, Iterable<DoubleWritable> values, Context context) {
        int count = 0;
        double total = 0;
        while (values.iterator().hasNext()) {
            total += values.iterator().next().get();
            count++;
        }
        double average = Double.parseDouble(df.format((total / count)));
        try {
            context.write(key, new DoubleWritable(average));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
