package main.java.com.product_scores;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProductScoresMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    private Text asin = new Text();
    private DoubleWritable rating = new DoubleWritable(0);

    public void map(LongWritable key, Text value, Context context) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject review = (JSONObject) parser.parse(value.toString());
            if (review != null) {
                asin.set(review.get("asin").toString());
                rating.set(Double.parseDouble(review.get("overall").toString()));
                context.write(asin, rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}