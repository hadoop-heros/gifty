package main.java.com.recommend_me;

import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.MapFile.Reader;
import org.apache.hadoop.io.Text;

import java.util.Collections;

public class RecommendMe {
    public static void main(String[] args) {

        Configuration conf = new Configuration();
        FileSystem fs;
        Text txtKey = new Text(args[2]);
        Text txtValue = new Text();
        Reader productsReader;
        Reader scoresReader;
        Recommended recommended = new Recommended();
        Gson gson = new Gson();

        // Get the array from Related Products MapFile
        try {
            fs = FileSystem.get(conf);
            productsReader = new Reader(fs, args[0], conf);
            scoresReader = new Reader(fs, args[1], conf);
            productsReader.get(txtKey, txtValue);
            String productsArrStr = txtValue.toString();
            String[] productsArr = productsArrStr.substring(productsArrStr.indexOf("[") + 1, productsArrStr.indexOf("]")).split(",");
            System.out.println("productsArr.length():");
            System.out.println(productsArr.length);
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
            Collections.sort(recommended.recommendedProducts);
            String json = gson.toJson(recommended);
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
