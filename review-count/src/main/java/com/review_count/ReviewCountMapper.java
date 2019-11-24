package main.java.com.review_count;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class ReviewCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private Text asin = new Text();
    private IntWritable count = new IntWritable(1);

    public void map(LongWritable key, Text value, Context context) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject review = (JSONObject) parser.parse(value.toString());
            if (review != null) {
                asin.set(review.get("asin").toString());
                context.write(asin, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
