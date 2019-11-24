package main.java.com.related_products;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;

public class TextArrayWritable extends ArrayWritable {

    public TextArrayWritable() {
        super(Text.class);
    }

    public TextArrayWritable(Text[] values) {
        super(Text.class, values);
    }

    @Override
    public Text[] get() {
        return (Text[]) super.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        for (String s : super.toStrings()) {
            sb.append(s).append(",");
        }

        sb.append("]");
        return sb.toString().replaceAll(",]", "]");
    }
}
