package main.java.com.related_products;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;

public class RelatedProductsMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable related_id = new IntWritable(0);

    public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) {
        String valueString = value.toString();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(valueString);
            JSONObject jsonObject = (JSONObject) obj;
            String productId = jsonObject.get("asin").toString();
            List<String> relatedProducts = new ArrayList<>();
            JSONArray alsoViewed = (JSONArray) jsonObject.get("also_viewed");
            if (alsoViewed != null) {
                for (Object o : alsoViewed) {
                    relatedProducts.add(o.toString());
                }
            }
            output.collect(new Text(productId), related_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
