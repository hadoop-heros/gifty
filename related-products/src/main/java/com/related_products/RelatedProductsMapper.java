package main.java.com.related_products;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;

public class RelatedProductsMapper extends Mapper<LongWritable, Text, Text, TextArrayWritable> {
    public void map(LongWritable key, Text value, Context context) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject metadata = (JSONObject) parser.parse(value.toString());
            if (metadata != null) {
                asin.set(metadata.get("asin").toString());
                JSONObject related = (JSONObject) metadata.get("related");
                if (related != null) {
                    ArrayList<Text> asinList = new ArrayList<>();
                    JSONArray also_bought = (JSONArray) related.get("also_bought");
                    if (also_bought != null) {
                        for (Object o : also_bought) {
                            asinList.add(new Text(o.toString()));
                        }
                    }
                    JSONArray also_viewed = (JSONArray) related.get("also_viewed");
                    if (also_viewed != null) {
                        for (Object o : also_viewed) {
                            asinList.add(new Text(o.toString()));
                        }
                    }
                    Text[] asinArr = new Text[asinList.size()];
                    for (int i = 0; i < asinList.size(); i++) {
                        asinArr[i] = asinList.get(i);
                    }
                    context.write(asin, new TextArrayWritable(asinArr));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Text asin = new Text();
}