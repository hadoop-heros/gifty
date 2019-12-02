package main.java.com.recommend_me;

import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.MapFile.Reader;
import org.apache.hadoop.io.Text;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Collections;

public class RecommendMe {
    public static void main(String[] args) {

        // TODO: Support multiple id inputs

        Configuration conf = new Configuration();
        FileSystem fs;
        Text txtKey = new Text(args[3]);
        Text txtValue = new Text();
        Recommended recommended = new Recommended();
        Gson gson = new Gson();
        JSONParser parser = new JSONParser();

        try {
            fs = FileSystem.get(conf);
            recommended.asin = txtKey.toString();
            Reader productsReader = new Reader(fs, args[0], conf);
            productsReader.get(txtKey, txtValue);
            productsReader.close();
            productsReader = null;
            String productsArrStr = txtValue.toString();
            String[] productsArr = productsArrStr.substring(productsArrStr.indexOf("[") + 1, productsArrStr.indexOf("]")).split(",");
            Reader scoresReader = new Reader(fs, args[1], conf);
            for (String s : productsArr) {
                Product product = new Product();
                Text txtScore = new Text();
                txtKey.set(s);
                product.asin = txtKey.toString();
                scoresReader.get(txtKey, txtScore);
                if (!txtScore.toString().equals("")) {
                    product.score = Double.parseDouble(txtScore.toString());
                }
                recommended.recommendedProducts.add(product);
            }
            scoresReader.close();
            scoresReader = null;
            Reader metadataReader = new Reader(fs, args[2], conf);
            for (Product p : recommended.recommendedProducts) {
                Text txtMetadata = new Text();
                txtKey.set(p.asin);
                metadataReader.get(txtKey, txtMetadata);
                if (!txtMetadata.toString().equals("")) {
                    JSONObject metadata = (JSONObject) parser.parse(txtMetadata.toString());
                    if (metadata.get("title") != null) {
                        p.title = metadata.get("title").toString();
                    }
                }
            }
            metadataReader.close();
            metadataReader = null;
            Collections.sort(recommended.recommendedProducts);
            String json = gson.toJson(recommended);
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
