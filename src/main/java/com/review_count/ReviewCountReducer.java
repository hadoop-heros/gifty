package com.review_count;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

public class ReviewCountReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text t_key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter)
            throws IOException {
        Text key = t_key;
        int frequencyForProduct = 0;
        while (values.hasNext()) {
            // replace the type of value with the actual type of our value
            IntWritable value = (IntWritable) values.next();
            frequencyForProduct += value.get();
        }
        output.collect(key, new IntWritable(frequencyForProduct));
    }
}
