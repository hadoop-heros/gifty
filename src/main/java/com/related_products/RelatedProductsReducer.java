package com.related_products;

import com.IntArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class RelatedProductsReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntArrayWritable> {
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntArrayWritable> outputCollector, Reporter reporter) throws IOException {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        while (values.hasNext()) {
            IntWritable value = values.next();
            temp.add(value.get());
        }
        IntWritable[] relatedProducts = new IntWritable[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            relatedProducts[i] = new IntWritable(temp.get(i));
        }

        outputCollector.collect(key, new IntArrayWritable(relatedProducts));
    }
}
