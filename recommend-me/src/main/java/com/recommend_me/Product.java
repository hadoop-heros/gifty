package main.java.com.recommend_me;

public class Product implements Comparable<Product> {
    String asin;
    Double score;
    String title;

    public Product() {
        asin = "";
        score = 0.0;
        title = "";
    }

    @Override
    public int compareTo(Product product) {
        return (this.score < product.score ? 1 :
                (this.score == product.score ? 0 : -1));
    }
}
