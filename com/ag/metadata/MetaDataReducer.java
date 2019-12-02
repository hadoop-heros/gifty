package com.ag.metadata;


import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MetaDataReducer extends Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{

        //nothing to reduce
        Text metadata = values.iterator().next();
        try{
            context.write(key, metadata);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
