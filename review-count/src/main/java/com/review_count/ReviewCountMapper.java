package main.java.com.review_count;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

public class ReviewCountMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);

    public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
            throws IOException {
        String valueString = value.toString();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(valueString);
            JSONObject jsonObject = (JSONObject)obj;
            String productId = jsonObject.get("asin").toString();
            output.collect(new Text(productId), one);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
