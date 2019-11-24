# Gifty

## How it works
Gifty uses Hadoop's MapReduce to extract recommended products using an Amazon data set that contains over 100 million records.

Dataset: http://jmcauley.ucsd.edu/data/amazon/

## Compile project files into .jar files

Using IntelliJ IDE, build the Artifacts using `Build > Artifacts`

The artifacts will be built into jar files and stored in the following directory:

```$xslt
/hadoop/shared/artifacts
```

## Run a new Map Reduce Job

1. Navigate to the directory where Hadoop is installed:
```$xslt
cd /hadoop
```

2. Format the filesystem:
```$xslt
bin/hdfs namenode -format
```

3. Start NameNode daemon and DataNode daemon:
```$xslt
sbin/start-dfs.sh
```

4. Browse the web interface for the NameNode; by default it is available at:
```$xslt
http://localhost:9870/
```

5. Make the HDFS directories required to execute MapReduce jobs:
```$xslt
bin/hdfs dfs -mkdir /user
bin/hdfs dfs -mkdir /user/<username>
```

6. Copy the dataset files into HDFS:
```$xslt
bin/hdfs dfs -mkdir input
bin/hdfs dfs -put $path_to_amazon_dataset input
```

7. Confirm the dataset files have been copied to HDFS:
```$xslt
bin/hdfs dfs -ls input
```

8. Run a Map Reduce command
```dtd
(see Map Reduce Commands below)
```

9. Copy the data in the HDFS to local filesystem:
```dtd
bin/hdfs dfs -get output output
```  

10. When you’re done, stop the daemons with:
```dtd
sbin/stop-dfs.sh
```

Source: https://hadoop.apache.org/docs/r3.1.3/hadoop-project-dist/hadoop-common/SingleCluster.html

## Map Reduce Commands

### Review Count
Counts the number of reviews for each Amazon Product

##### Structure
<review_id, review_count>

##### Command
```
bin/hadoop jar share/gifty/review-count.jar main.java.com.review_count.ReviewCountDriver input output
```

##### Example Output
```
0972683275	219
1400501466	43
1400501520	20
```

### Average Rating
Calculates the average rating for each product using the reviews data.

##### Command
```
bin/hadoop jar share/gifty/average-rating.jar main.java.com.average_rating.AverageRatingDriver input output
```

##### Example Output
```
0528881469	2
0594451647	4
0594481813	4
```

### Product Scores
Calculates a score for each product. Below is the equation we are using to calculate this score:
```
(average_rating * review_count) / (average_rating + review_count)
```
This gives us a score out of 5

##### Structure
<review_id, product_score>

##### Command
```
Coming soon
```

##### Example Output
```
Coming soon
```

### Related Products
Maps "also bought" products to each product id.

##### Structure
<review_id, [product_id1, product_id2]>

##### Command
```
bin/hadoop jar share/gifty/related-products.jar main.java.com.related_products.RelatedProductsDriver input output
```

##### Example Output
```
0000013714	[0005080789,0005476798,0005476216,0005064341,0005235073,1883206561,0006180116,0005064295,0871482215,
B005HWXXCI,0006458718,9990810397,B001U5LQG6,0894770004,0002877813,0005448506,0005064309,0834193345,B007ZJE5OG]
```

### Recommended Products
Maps Product Id to an array of sorted recommended products based on a calculated product score.

##### Structure
<product_id, [{product_id, product_score}]>

##### Command
```
Coming soon
```

##### Example Output
```
Coming soon
```

## Other Useful Commands

#### Format Cluster Id
```
bin/hdfs namenode -format -clusterId <cluster_id>
```

#### Delete HDFS directory
```
bin/hdfs dfs -rm -r <output_dir>
```

## Parsing the Product Metadata

The product metadata will be used to create an array of recommended products for each product id. The product metadata
is 10GB in size and contains 9.4 million products. Before we can feed this data into our mapreduce function, we need to
convert to strict json using the following python code:

```python
import json
import gzip

def parse(path):
  g = gzip.open(path, 'r')
  for l in g:
    yield json.dumps(eval(l))

f = open("metadata.json", 'w')
for l in parse("metadata.json.gz"):
  f.write(l + '\n')
```

Next we use the RelatedProducts MapReduce method to map each product id to its recommended products.

We end up with a file that is 1.6GB in size. Much easier to work with than the original 10GB file.

## Creating a Recommended Products Lookup

Now that we have a file where each Product Id is mapped to an array of related products that have been scores and sorted
we can easily return that array to the user.

Example Input:
```
[productId1, productId2, productId3]
```

Example Output:
```
[productId1: {productName, imageUrl, reviewCount, ratingCount, productScore, description}]
```