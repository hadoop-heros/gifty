package main.java.com.related_products;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;

public class IntArrayWritable extends ArrayWritable {
    public IntArrayWritable() {
        super(IntWritable.class);
    }

    public IntArrayWritable(IntWritable[] values) {
        super(IntWritable.class, values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        for (String s : super.toStrings())
        {
            sb.append(s).append(" ");
        }

        sb.append("]");
        return sb.toString();
    }
}
