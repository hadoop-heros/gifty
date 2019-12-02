package main.java.com.product_metadata;

import com.google.gson.Gson;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProductMetadataMapper extends Mapper<LongWritable, Text, Text, Text> {
    private Text asin = new Text();
    private Text productJson = new Text();

    public void map(LongWritable key, Text value, Context context) {
        JSONParser parser = new JSONParser();
        Gson gson = new Gson();
        try {
            JSONObject metadata = (JSONObject) parser.parse(value.toString());
            if (metadata != null) {
                Product product = new Product();
                asin.set(metadata.get("asin").toString());
                product.asin = asin.toString();
                Object title = metadata.get("title");
                if (title != null) {
                    product.title = title.toString();
                }
                Object imgUrl = metadata.get("imUrl");
                if (imgUrl != null) {
                    product.imgUrl = imgUrl.toString();
                }
                productJson.set(gson.toJson(product));
                context.write(asin, productJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
