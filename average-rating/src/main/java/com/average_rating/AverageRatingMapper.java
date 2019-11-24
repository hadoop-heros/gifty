package main.java.com.average_rating;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AverageRatingMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private Text asin = new Text();
    private IntWritable rating = new IntWritable(0);

    public void map(LongWritable key, Text value, Context context) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject review = (JSONObject) parser.parse(value.toString());
            if (review != null) {
                asin.set(review.get("asin").toString());
                rating.set((int) Double.parseDouble(review.get("overall").toString()));
                context.write(asin, rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
