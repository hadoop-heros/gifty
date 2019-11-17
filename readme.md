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

### Related Products
Counts the number of reviews for each Amazon Product

##### Structure
<review_id, [product_id1, product_id2]>

##### Command
```
bin/hadoop jar share/gifty/related-products.jar main.java.com.related_products.RelatedProductsDriver input output
```

##### Example Output
```
0972683275	219
1400501466	43
1400501520	20
```

## Other Useful Commands

#### Format Cluster Id
```
hdfs namenode -format -clusterId <cluster_id>
```