package com.ag.metadata;


import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MetaDataReducer extends Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{

        try{
            String metadata = "";
            TextArrayWritable output = new TextArrayWritable();
            for(Text val : values){
                metadata = metadata.concat(val.toString());
                metadata = metadata.concat(" ");
            }
            context.write(key, new Text(metadata));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
