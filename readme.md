# Gifty

## How it works
Gifty uses Hadoop's MapReduce to extract recommended products using an Amazon data set that contains over 100 million records.

Dataset: http://jmcauley.ucsd.edu/data/amazon/

## Compile project files into .jar files

Using IntelliJ IDE, build the Artifacts using `Build > Artifacts`

The artifacts will be built into jar files and stored in the following directory:

```$xslt
/hadoop/share/gifty/<filename>.jar
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
bin/hadoop jar share/gifty/review-count.jar <input> <output>
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
bin/hadoop jar share/gifty/average-rating.jar <input> <output>
```

##### Example Output
```
0528881469	2.42
0594451647	4.95
0594481813	4.25
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
bin/hadoop jar share/gifty/product-scores.jar <input> <output>
```

##### Example Output
```
0528881469	2.42
0594451647	4.95
0594481813	4.28
```

### Related Products
Maps "also bought" products to each product id.

##### Structure
<review_id, [product_id1, product_id2]>

##### Command
```
bin/hadoop jar share/gifty/related-products.jar <input> <output>
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
bin/hadoop jar share/gifty/recommended-products.jar <input1> <input2> <output>
```

##### Example Output
```
0000013714	[{0005476798, 4.95},{0005476216, 4.28},{0005080789, 2.42}]
```

## Map File Commands

### Convert Map File
Converts a text file separated by \t into a HDFS MapFile

### Command
```
bin/hadoop jar share/gifty/mapfile-converter.jar <input> <output>
```

### Read Map File
Searches a MapFile for a Key and returns values if any

### Command
```
bin/hadoop jar share/gifty/mapfle-reader.jar <input> <key>
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

## Doing the Project

## Create Directories
```
bin/hdfs dfs -mkdir /user
bin/hdfs dfs -mkdir /user/jason
bin/hdfs dfs -mkdir /user/jason/input
bin/hdfs dfs -mkdir /user/jason/output
```

## MapReduce the Product Scores

##### Add Reviews to HDFS
```
bin/hdfs dfs -put data/amazon/reviews_Electronics_5.json input/reviews
```

##### Run Product Scores MapReduce Job
```
bin/hadoop jar share/gifty/product-scores.jar input/reviews output/product-scores
```

##### Copy Results from HDFS to local FS
```
bin/hdfs dfs -get output/product-scores output/product-scores
```

## MapFile the Product Scores
```
bin/hadoop jar share/gifty/mapfile-converter.jar output/product-scores/part-r-00000 output/product-scores-map
```

##### Copy MapFile from HDFS to local FS
```
bin/hdfs dfs -get output/product-scores-map output/product-scores-map
```

##### Test Read the MapFile
```
bin/hadoop jar share/gifty/mapfile-reader.jar output/product-scores-map 0528881469
```

## MapReduce the Related Products

##### Add Metadata to HDFS
```
bin/hdfs dfs -put data/amazon/metadata.json input/metadata
```

##### Run Related Product Scores MapReduce Job
```
bin/hadoop jar share/gifty/related-products.jar input/metadata output/related-products
```

##### Copy Results from HDFS to local FS
```
bin/hdfs dfs -get output/related-products output/related-products
```

## MapFile the Related Products

##### Create the MapFile
```
bin/hadoop jar share/gifty/mapfile-converter.jar output/related-products/part-r-00000 output/related-products-map
```

##### Copy MapFile from HDFS to local FS
```
bin/hdfs dfs -get output/related-products-map output/related-products-map
```

##### Test Read the MapFile
```
bin/hadoop jar share/gifty/mapfile-reader.jar output/product-scores-map 0528881469
```