package main.java.com.related_products;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class RelatedProductsDriver {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "RelatedProducts");

        // Set Classes
        job.setJarByClass(RelatedProductsDriver.class);
        job.setMapperClass(RelatedProductsMapper.class);
        job.setReducerClass(RelatedProductsReducer.class);

        // Set Output and Input Parameters
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(TextArrayWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(TextArrayWritable.class);

        // Number of Reducers
        job.setNumReduceTasks(1);

        // Set FileDestination
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
