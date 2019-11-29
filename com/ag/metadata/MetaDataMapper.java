package com.ag.metadata;


import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

public class MetaDataMapper extends Mapper<Object, Text, Text, Text> {

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException
    {
        JSONParser parser = new JSONParser();
        String valueString = value.toString();
        try {
           JSONObject obj = (JSONObject) parser.parse(valueString);
           String ProductID = obj.get("asin").toString();
           String title = obj.get("title").toString();


            context.write(new Text(ProductID), new Text(title));

           if(obj.get("image") != null) {
               JSONArray imgObj = (JSONArray) obj.get("image");
               String imgUrl = "";

               imgUrl = imgUrl.concat(imgObj.get(0).toString());
               context.write(new Text(ProductID), new Text(imgUrl));
           }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
