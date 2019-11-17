package main.java.com.related_products;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

public class RelatedProductsDriver {
    public static void main(String[] args) {
        JobClient my_client = new JobClient();
        // Create a configuration object for the job
        JobConf job = new JobConf(RelatedProductsDriver.class);

        // Set a name of the Job
        job.setJobName("RelatedProducts");

        // Set Output and Input Parameters
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // Specify data type of output key and value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntArrayWritable.class);

        // Specify names of Mapper and Reducer Class
        job.setMapperClass(RelatedProductsMapper.class);
        job.setReducerClass(RelatedProductsReducer.class);

        // Specify formats of the data type of Input and Output
        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        // Set input and output directories using command line arguments,
        // arg[0] = name of input directory on HDFS
        // arg[1] = name of output directory to be created to store output file
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        my_client.setConf(job);
        try {
            // Run the job
            JobClient.runJob(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
