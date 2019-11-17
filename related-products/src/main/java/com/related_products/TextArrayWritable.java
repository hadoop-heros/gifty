package main.java.com.related_products;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Writable;

public class TextArrayWritable extends ArrayWritable {
    public TextArrayWritable() {
        super(Writable.class);
    }

    public TextArrayWritable(Writable[] values) {
        super(Writable.class, values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        for (String s : super.toStrings()) {
            sb.append(s).append(" ");
        }

        sb.append("]");
        return sb.toString();
    }
}
