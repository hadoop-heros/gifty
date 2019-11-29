package com.ag.metadata;


import com.google.gson.Gson;
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

            String ProductID = obj.get("asin").toString();  //ProductID (asin)
            String title = obj.get("title").toString();     //title (title)
            String imgUrl = "";                             ////imageUrl (image: [array of image URLs])

            //some products don't have an image entry
            if(obj.get("image") != null) {
                //image entry has multiple links
                JSONArray imgObj = (JSONArray) obj.get("image");

                //Only want one image to represent product, so take first link
                imgUrl = imgUrl.concat(imgObj.get(0).toString());

            }    //if a product doesnt have an image entry, leave imgUrl string empty

            //Obj to be converted to JSON string
            MetaData metadata = new MetaData(title, imgUrl);

            Gson gson = new Gson();
            String JSONmetadata = gson.toJson(metadata);

            context.write(new Text(ProductID), new Text(JSONmetadata));


        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

//class to be converted to JSON string
class MetaData{

    private String title;
    private String imageURL;

    public MetaData(String title, String imageURL){
        this.title = title;
        this.imageURL = imageURL;
    }
}
