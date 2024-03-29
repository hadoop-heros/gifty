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

### Product Metadata
Maps Product Id to Product Object Json

##### Structure
<product_id, {"asin":"118920", "title":"Product title", "imgUrl":"http://img.url/"}>

##### Command
```
bin/hadoop jar share/gifty/product-metadata.jar input/metadata output/product-metadata
```

##### Example Output
```
007001356X	{"asin":"007001356X","title":"Diez Cuentos De Eva Luna Con Guia De Comprension Y Repaso De Gramatica (Spanish and English Edition)","imgUrl":"http://ecx.images-amazon.com/images/I/51FJs0Y-jBL.jpg"}
0070013683	{"asin":"0070013683","title":"An Introduction to American Forestry","imgUrl":"http://ecx.images-amazon.com/images/I/31OMNKvQqgL.jpg"}
0070013721	{"asin":"0070013721","title":"Apartments for the Affluent: A Historical Survey of Buildings in New York","imgUrl":"http://ecx.images-amazon.com/images/I/41EsuBX8ZIL.jpg"}
```

## Map File Commands

### Convert Map File

Converts a text file separated by \t into a HDFS MapFile

##### Command
```
bin/hadoop jar share/gifty/mapfile-converter.jar <input> <output>
```

### Read Map File
Searches a MapFile for a Key and returns values if any

##### Command
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

# Setting up the Recommendation System

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
bin/hdfs dfs -put data/amazon/reviews.json input/reviews
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

##### Create the MapFile
```
bin/hadoop jar share/gifty/mapfile-writer.jar output/product-scores/part-r-00000 output/product-scores-map
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
bin/hadoop jar share/gifty/mapfile-writer.jar output/related-products/part-r-00000 output/related-products-map
```

##### Copy MapFile from HDFS to local FS
```
bin/hdfs dfs -get output/related-products-map output/related-products-map
```

##### Test Read the MapFile
```
bin/hadoop jar share/gifty/mapfile-reader.jar output/related-products-map 0000031852
```

## MapReduce the Product Metadata

##### Run Product Metadata MapReduce Job
```
bin/hadoop jar share/gifty/product-metadata.jar input/metadata output/product-metadata
```

##### Copy Results from HDFS to local FS
```
bin/hdfs dfs -get output/product-metadata output/product-metadata
```

## MapFile the Product Metadata

##### Create the MapFile
```
bin/hadoop jar share/gifty/mapfile-writer.jar output/product-metadata/part-r-00000 output/product-metadata-map
```

##### Copy MapFile from HDFS to local FS
```
bin/hdfs dfs -get output/product-scores-map output/product-scores-map
```

##### Test Read the MapFile
```
bin/hadoop jar share/gifty/mapfile-reader.jar output/product-metadata-map 0000031852
```

## Extract Recommended Products from Maps

##### Run Recommend Me
```
bin/hadoop jar share/gifty/recommend-me.jar output/related-products-map/ output/product-scores-map/ output/product-metadata-map 0000031852
```

## Results

## Case #1: Gift for Professor

#### Input
```
bin/hadoop jar share/gifty/recommend-me.jar output/related-products-map/ output/product-scores-map/ output/product-metadata-map 1621522172 962888400X 9991122060 B001AF9M12
```

#### Output
<details>

```
[
   {
      "asin":"B008JSZ9PQ",
      "score":4.51,
      "title":"Warmen Women\u0027s Lambskin Leather Cold Weather Gloves with Crossing Bow"
   },
   {
      "asin":"B004LBS9AW",
      "score":4.07,
      "title":"Rainbow Crystal Rosary with Faceted Rondell Beads in 8x6mm - 28\u0027\u0027 Necklace - 21\u0027\u0027 Overall Length"
   },
   {
      "asin":"B00C1X7O62",
      "score":3.63,
      "title":"Copco 2510-0410 Acadia Travel Mug, 16-Ounce, Translucent Pink"
   },
   {
      "asin":"B00BT9K59C",
      "score":3.63,
      "title":"Estee Lauder Lilly Pulitzer Snapdragon Print Cosmetic Bag wi/ 2 Travel Bottles"
   },
   {
      "asin":"B001OWFQ6G",
      "score":3.61,
      "title":"Vintage Classic Leopard Tortoise Wayfarer Style Sunglasses"
   },
   {
      "asin":"B006H9Z9RW",
      "score":3.53,
      "title":"ERO Hybrid TPU Cover for iPhone 4 \u0026amp; 4S, Black Cat"
   },
   {
      "asin":"B0042FV2SI",
      "score":3.52,
      "title":"iPhone 4 / 4S Anti-Glare, Anti-Scratch, Anti-Fingerprint - Matte Finishing Screen Protector"
   },
   {
      "asin":"B0002T6AZS",
      "score":3.47,
      "title":"Fratelli Orsini Women\u0027s Italian Silk Lined Leather Gloves"
   },
   {
      "asin":"047050160X",
      "score":3.17,
      "title":"Lilly: Palm Beach, Tropical Glamour, and the Birth of a Fashion Legend"
   },
   {
      "asin":"B0018MZPHC",
      "score":2.87,
      "title":"Fratelli Orsini Women\u0027s Leather Driving Gloves"
   },
   {
      "asin":"B0049WLQOE",
      "score":2.66,
      "title":"Divine Guardian Bracelets"
   },
   {
      "asin":"B00JHV0REW",
      "score":2.5,
      "title":"Estee Lauder Lilly Pulitzer Spring Cosmetic Bag 2014"
   },
   {
      "asin":"B00HZVILEO",
      "score":2.34,
      "title":"iPhone 5c Case White Lilly Monogram Blue (Generic for Otterbox Commuter)"
   },
   {
      "asin":"B00DU07QBS",
      "score":2.31,
      "title":"Lilly Pulitzer iPhone 5 Cover - Let\u0027s Cha Cha"
   },
   {
      "asin":"B00DU078HK",
      "score":2.25,
      "title":"Lilly Pulitzer iPhone 5 Cover with 2 card slot - Tusk in Sun"
   },
   {
      "asin":"B005HKMO7U",
      "score":2.22,
      "title":"Estee Lauder Fall Flowered Cosmetic Bag"
   },
   {
      "asin":"B00D9TAPEK",
      "score":2.22,
      "title":"Lilly Pulitzer for Estee Lauder Collection Cosmetic Makeup Bag (Blue Flower)"
   },
   {
      "asin":"B0016JIPQA",
      "score":2.21,
      "title":"Magnetic Hematite Tanker Bracelet - Photos of Christian Icons."
   },
   {
      "asin":"B00DU079Q0",
      "score":2.16,
      "title":"Lilly Pulitzer iPad Mini Case with Smart Cover - Coronado Crab"
   },
   {
      "asin":"B007N6IIB2",
      "score":2.12,
      "title":"Our Lady of Guadalupe John Paul II Devotional Religious Women\u0027s Hand Tote Bag"
   },
   {
      "asin":"B00BWFDIS8",
      "score":2.06,
      "title":"Lilly Pulitzer iPhone 5 Cover with 2 card slot - Chiquita Bonita"
   },
   {
      "asin":"B00AY09VYC",
      "score":2.06,
      "title":"Lilly Pulitzer iPhone 5 Cover - Chiquita Bonita"
   },
   {
      "asin":"B0091MCHX6",
      "score":1.88,
      "title":"Lilly Pulitzer Pencils - Set of 10"
   },
   {
      "asin":"B00DU07XN4",
      "score":1.88,
      "title":"Lilly Pulitzer iPhone 5 Cover - Tiger Lilly"
   },
   {
      "asin":"B00I8U5MCA",
      "score":1.88,
      "title":"Lilly Pulitzer Sunglass Strap, Lobstah Roll"
   },
   {
      "asin":"B00I9DJOX4",
      "score":1.88,
      "title":"Lilly Pulitzer Acrylic Tumbler with Straw -Trippin\u0027 and Sippin\u0027"
   },
   {
      "asin":"B00E4S8FIS",
      "score":1.83,
      "title":"Saint St Benedict of Nursia Silver Tone Medal 8\u0026quot; Adjustable Black Cord Wrist Bracelet"
   },
   {
      "asin":"B00G70VM40",
      "score":1.79,
      "title":"Kate Spade Thermal Mug - Black and White Stripe"
   },
   {
      "asin":"B00BZBW3VW",
      "score":1.77,
      "title":"Lilly Pulitzer Sticker \u0026amp; Label Book 2013 Various Patterns"
   },
   {
      "asin":"B00DU07C5S",
      "score":1.77,
      "title":"Lilly Pulitzer iPhone 5 Cover with 2 Card Slot - ChaCha"
   },
   {
      "asin":"B00DU07VBS",
      "score":1.65,
      "title":"Lilly Pulitzer Thermal Mug - Tiger Lilly"
   },
   {
      "asin":"B00B43IX6A",
      "score":1.65,
      "title":"Lilly Pulitzer Thermal Mug - Chin Chin"
   },
   {
      "asin":"B00DU07802",
      "score":1.65,
      "title":"Lilly Pulitzer Thermal Mug - Let\u0027s Cha Cha"
   },
   {
      "asin":"B009LETL2Y",
      "score":1.58,
      "title":"Lilly Pulitzer Acrylic Tumbler with Straw -Lobstah Roll"
   },
   {
      "asin":"B00I8U8XPS",
      "score":1.58,
      "title":"Lilly Pulitzer Sunglass Strap, Trippin and Sippin"
   },
   {
      "asin":"B00DU06AB0",
      "score":1.43,
      "title":"Lilly Pulitzer Acrylic Stemless Wine Glass Set - Let\u0027s Cha Cha - Kitchen Beverage Travel Wine 134414-LGPLP"
   },
   {
      "asin":"B00I8AVXIC",
      "score":1.43,
      "title":"Lilly Pulitzer Insulated Cooler - Trippin\u0027 and Sippin"
   },
   {
      "asin":"B00I9DGEHS",
      "score":1.43,
      "title":"Lilly Pulitzer Acrylic Tumbler with Straw - Elephant Ears"
   },
   {
      "asin":"B00I8S3GGQ",
      "score":1.27,
      "title":"Lilly Pulitzer Set of 8 Tumblers - Trippin and Sippin"
   },
   {
      "asin":"B00I8PM1HY",
      "score":1.2,
      "title":"Lilly Pulitzer Market Bag - Lobstah Roll"
   },
   {
      "asin":"B00DU07O9W",
      "score":1.11,
      "title":"Lilly Pulitzer iPhone 5 Mobile Charger - Tuck In Sun (Elephant)"
   },
   {
      "asin":"B0091SZ1FQ",
      "score":0.83,
      "title":"Lilly Pulitzer Koozie Drink Can Cover See You Later"
   },
   {
      "asin":"B00I8AWDUE",
      "score":0.83,
      "title":"Lilly Pulitzer Insulated Cooler - Booze Cruise"
   },
   {
      "asin":"B00DU0768Q",
      "score":0.83,
      "title":"Lilly Pulitzer Thermal Mug - Coronado Crab"
   },
   {
      "asin":"B00I9DDW96",
      "score":0.83,
      "title":"Lilly Pulitzer Acrylic Tumbler with Straw - Beach Rose"
   },
   {
      "asin":"B00I8P2P7U",
      "score":0.83,
      "title":"Lilly Pulitzer Travel Mug - Beach Rose"
   },
   {
      "asin":"B00I8ARFXE",
      "score":0.83,
      "title":"Lilly Pulitzer Drink Hugger - Trippin\u0027 and Sippin\u0027"
   },
   {
      "asin":"B00I8P0IYM",
      "score":0.83,
      "title":"Lilly Pulitzer Travel Mug - Lobstah Roll"
   },
   {
      "asin":"B00B43KS36",
      "score":0.83,
      "title":"Lilly Pulitzer Thermal Mug, Chiquita Bonita - Kitchen Lilly - 133002-LGPLP"
   },
   {
      "asin":"B00DU07Z32",
      "score":0.83,
      "title":"Lilly Pulitzer Thermal Mug - First Impression"
   },
   {
      "asin":"B00B43LVVE",
      "score":0.67,
      "title":"Lilly Pulitzer Thermal Mug - Garden By the Sea"
   },
   {
      "asin":"B00IG5R1L2",
      "score":0.5,
      "title":"Lilly Pulitzer Acrylic Tumbler with Straw - Booze Cruise"
   }
]
```

</details>

## Case #2: Gift for Student

#### Input
```
bin/hadoop jar share/gifty/recommend-me.jar output/related-products-map/ output/product-scores-map/ output/product-metadata-map B00005BZ3Z B00005CDPY B000001OMJ B0019FLMP2
```

#### Output
<details>

```
[
   {
      "asin":"155407147X",
      "score":4.66,
      "title":"NightWatch: A Practical Guide to Viewing the Universe"
   },
   {
      "asin":"B00005YY9X",
      "score":4.43,
      "title":"Bodum 1308-16 Columbia 8-Cup Stainless-Steel Thermal Press Pot"
   },
   {
      "asin":"B008AXURAW",
      "score":4.39,
      "title":"Fossil FS4735 Grant Brown Leather Watch"
   },
   {
      "asin":"B002T1M8A8",
      "score":4.36,
      "title":"Fossil Men\u0027s FS4487 Black Silicone Bracelet Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B003VYQK7E",
      "score":4.35,
      "title":"Fossil Men\u0027s FS4545 Black Leather Strap Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B006GVP11A",
      "score":4.35,
      "title":"FOSSIL JR1353 Nate Chronograph Stainless Steel Watch"
   },
   {
      "asin":"B00178494W",
      "score":4.32,
      "title":"Fossil Women\u0027s AM4141 Stainless Steel Bracelet Mother-Of-Pearl Glitz Analog Dial Watch"
   },
   {
      "asin":"B0058VCYUU",
      "score":4.32,
      "title":"Chef\u0027s Choice 681 Cordless Electric Kettle"
   },
   {
      "asin":"B007PW9TG2",
      "score":4.31,
      "title":"Fossil Women\u0027s ES3098 Stainless Steel Analog White Dial Watch"
   },
   {
      "asin":"B005OT7MBM",
      "score":4.28,
      "title":"Tommy Hilfiger  Men\u0027s 1710294 Stainless Steel and Leather Strap White Dial Watch"
   },
   {
      "asin":"B0096IQZ8S",
      "score":4.27,
      "title":"Stuhrling Original Men\u0027s 395.33U16 Aquadiver Regatta Champion Professional Diver Swiss Quartz Date Blue Bezel Watch"
   },
   {
      "asin":"B000KEM4TQ",
      "score":4.26,
      "title":"Bodum Brazil 8-Cup French Press Coffee Maker, 34-Ounce, Black"
   },
   {
      "asin":"B00248GSY8",
      "score":4.25,
      "title":"Fossil Men\u0027s CH2573 Black silicon Strap Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B001T6OPZ0",
      "score":4.24,
      "title":"Fossil Men\u0027s CH2564 Black Leather Strap Blue Glass Silver Analog Dial Chronograph Watch"
   },
   {
      "asin":"B007X000Q4",
      "score":4.22,
      "title":"XOXO Women\u0027s XO9028 Watch Set with Seven Interchangeable Silicone Rubber Straps"
   },
   {
      "asin":"B000PSQXH6",
      "score":4.22,
      "title":"Nautica Men\u0027s N10061 Stainless Steel Round Multi-Function Watch"
   },
   {
      "asin":"B00422MCUS",
      "score":4.22,
      "title":"Tommy Hilfiger Men\u0027s Ranger Passcase Wallet"
   },
   {
      "asin":"B000XQKFHE",
      "score":4.18,
      "title":"GUESS Women\u0027s G75511M Mid-Size Sporty Chic Silver-Tone Watch"
   },
   {
      "asin":"B00004S1DB",
      "score":4.18,
      "title":"Thermos 34-Ounce Vacuum Insulated Stainless-Steel Gourmet Coffee Press"
   },
   {
      "asin":"B000GOO03I",
      "score":4.18,
      "title":"Casio Watch - A178WGA1A (Size: men)"
   },
   {
      "asin":"B001S073P6",
      "score":4.16,
      "title":"Fossil Women\u0027s ES2189 Stainless Steel Bracelet Pink Mother-Of-Pearl Glitz Analog Dial Watch"
   },
   {
      "asin":"B0048LRK5K",
      "score":4.1,
      "title":"Fossil Quartz Rosegold Gem Dial Rosegold Band - Women\u0027s Watch ES2811"
   },
   {
      "asin":"B000KDVTJI",
      "score":4.1,
      "title":"Aroma AWK-115S Hot H20 X-Press 1-1/2-Liter Cordless Water Kettle"
   },
   {
      "asin":"B003FN7PZW",
      "score":4.09,
      "title":"Fossil Men\u0027s JR1156 Black Leather Strap Blue Analog Dial Chronograph Watch"
   },
   {
      "asin":"B0016HTYS0",
      "score":4.08,
      "title":"Fossil Women\u0027s ES1967 Stella Day/Date Display Quartz White Dial Watch"
   },
   {
      "asin":"B009BEO9DU",
      "score":4.07,
      "title":"Fossil Women\u0027s ES3204 Riley Silver and Gold Tone Watch"
   },
   {
      "asin":"B000K1MY6W",
      "score":4.06,
      "title":"GUESS Women\u0027s G75916L Brilliance on Links Silver-Tone Bracelet Watch"
   },
   {
      "asin":"B000KNCB7C",
      "score":4.05,
      "title":"Opteka T-Mount Adapter for Canon EOS 60D, 50D, 40D, 30D, 20D, 7D, 5D, 1D, Rebel T3i, T3, T2i, T1i, XS, XSi, XTi and XT Digital SLR Cameras"
   },
   {
      "asin":"B005LBZPPI",
      "score":4.04,
      "title":"Fossil Women\u0027s ES3003 Stainless Steel Analog Pink Dial Watch"
   },
   {
      "asin":"B008AZH36G",
      "score":4.0,
      "title":"Fossil Women\u0027s ES3101 Stainless Steel Analog Gold Dial Watch"
   },
   {
      "asin":"B008AXUQ4O",
      "score":3.99,
      "title":"Fossil Men\u0027s FS4736 Grant Stainless Steel Watch"
   },
   {
      "asin":"B0058ZAKCK",
      "score":3.97,
      "title":"Fossil Men\u0027s FS4662 Stainless Steel Analog Black Dial Watch"
   },
   {
      "asin":"B003XSWPLI",
      "score":3.97,
      "title":"Fossil Women\u0027s ES2683 Gold-Tone Stainless Steel Bracelet Gold Glitz Analog Dial Chronograph Watch"
   },
   {
      "asin":"B003KYSLNQ",
      "score":3.94,
      "title":"Cuisinart CPK-17 PerfecTemp 1.7-Liter Stainless Steel Cordless Electric Kettle"
   },
   {
      "asin":"B00A157JOY",
      "score":3.94,
      "title":"Russell Hobbs Electric Kettle"
   },
   {
      "asin":"B007PW9TRQ",
      "score":3.9,
      "title":"Fossil Women\u0027s ES3110 \u0026quot;Georgia\u0026quot; Stainless Steel Bangle Watch"
   },
   {
      "asin":"B004MFTKKU",
      "score":3.9,
      "title":"Fossil Women\u0027s ES2889 Riley Analog Display Analog Quartz Rose Gold Watch"
   },
   {
      "asin":"B008D902Q2",
      "score":3.88,
      "title":"U.S. Polo Assn. Sport Men\u0027s US9061 Watch with Black Rubber Strap Watch"
   },
   {
      "asin":"B003WVJNNY",
      "score":3.88,
      "title":"Fossil Men\u0027s FS4542 Stainless Steel Bracelet Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B001KZH3ZY",
      "score":3.88,
      "title":"Chef\u0027sChoice 679 Cordless 1-3/4-Quart Electric Glass Kettle"
   },
   {
      "asin":"B0094KUKOI",
      "score":3.86,
      "title":"Fossil Men\u0027s FS4774 Machine Analog Display Analog Quartz Grey Watch"
   },
   {
      "asin":"B007GNHF48",
      "score":3.86,
      "title":"Fossil Women\u0027s ES3083 Georgia Stainless Steel Watch"
   },
   {
      "asin":"B00AFTTQ8I",
      "score":3.85,
      "title":"Fossil Men\u0027s FS4813 \u0026quot;Grant\u0026quot; Stainless Steel Watch with Leather Band"
   },
   {
      "asin":"B004L4ODB8",
      "score":3.85,
      "title":"Fossil ES2859 Stella Plated Stainless Steel Watch - Rose"
   },
   {
      "asin":"B007X0DWW8",
      "score":3.84,
      "title":"XOXO Women\u0027s XO5475 Rhinestone-Accented Gold-Tone Bracelet Watch"
   },
   {
      "asin":"B0093Q0VB0",
      "score":3.83,
      "title":"GUESS Women\u0027s U0026L1 Dazzling Sporty Silver \u0026amp; Gold-Tone Mid-Size Watch"
   },
   {
      "asin":"B00495NI7O",
      "score":3.82,
      "title":"GUESS Women\u0027s U10075L1 Feminine Hi-Shine Mid-Size Silver-Tone Sport Watch"
   },
   {
      "asin":"B008AXUQPS",
      "score":3.8,
      "title":"Fossil Men\u0027s FS4734 Grant Stainless Steel Watch"
   },
   {
      "asin":"B004FV4WCW",
      "score":3.78,
      "title":"Onitsuka Tiger Women\u0027s Mexico 66 Sneaker"
   },
   {
      "asin":"B003EKNMAI",
      "score":3.78,
      "title":"U.S. Polo Assn. Sport Men\u0027s US9057 Watch"
   },
   {
      "asin":"B0094KUL0Q",
      "score":3.75,
      "title":"Fossil Men\u0027s FS4776 Machine Stainless Steel Watch"
   },
   {
      "asin":"B009LSKPYI",
      "score":3.74,
      "title":"Fossil Men\u0027s JR1437 Nate Chronograph Smoke Stainless Steel Watch"
   },
   {
      "asin":"B000ES7I8A",
      "score":3.74,
      "title":"Hamilton Beach 40898 Cool-Touch 8-Cup Cordless Electric Kettle"
   },
   {
      "asin":"B0042GK2B0",
      "score":3.72,
      "title":"GUESS Women\u0027s U12631L1 Active Shine Gold-Tone Watch"
   },
   {
      "asin":"B007X08VJM",
      "score":3.72,
      "title":"XOXO Women\u0027s XO5465 Rhinestone-Accented Gold-Tone Bracelet Watch"
   },
   {
      "asin":"B008H2Z1Q6",
      "score":3.7,
      "title":"Fossil Women\u0027s ES3106 Stainless Steel Analog Gold Dial Watch"
   },
   {
      "asin":"B00BF2CVMM",
      "score":3.67,
      "title":"G-SHOCK G-SHOCK MENS GA-200SH-1A Sports Watch BLACK"
   },
   {
      "asin":"B00004VVA4",
      "score":3.67,
      "title":"Meade 07366 No.64ST 35-Millimeter SLR Camera T-Adapter for ETX-60, ETX-70 and ETX-80 Series Telescopes (Black)"
   },
   {
      "asin":"B0018AM6M6",
      "score":3.66,
      "title":"Fossil Men\u0027s FS4359 Stainless Steel Bracelet Silver Analog Dial Chronograph Watch"
   },
   {
      "asin":"B0018AODYA",
      "score":3.65,
      "title":"Fossil Men\u0027s FS4358 Black Stainless Steel Bracelet Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B004LXIXP6",
      "score":3.65,
      "title":"Fossil Women\u0027s ES2829 Georgia White Leather Watch"
   },
   {
      "asin":"B00AG37I6U",
      "score":3.64,
      "title":"Fossil FS4788 Dean Chronograph Leather Watch - Brown"
   },
   {
      "asin":"B004P0UUBK",
      "score":3.64,
      "title":"XOXO Women\u0027s XO5301A Rhinestone-Accented Silver-Tone Bracelet Watch"
   },
   {
      "asin":"B0058Z9FVW",
      "score":3.62,
      "title":"Fossil Men\u0027s AM4368 Stainless Steel Analog with Black Dial Watch"
   },
   {
      "asin":"B005NGRB80",
      "score":3.61,
      "title":"Tommy Hilfiger Men\u0027s 1710308 Classic Stainless Steel and Blue Dial Bracelet Watch"
   },
   {
      "asin":"B0013PUS26",
      "score":3.6,
      "title":"Fossil Stella White Dial Women\u0027s Quartz Watch - ES1967"
   },
   {
      "asin":"B0066T2P1G",
      "score":3.59,
      "title":"Fossil Stella Aluminum and Stainless Steel Watch Green ES3039"
   },
   {
      "asin":"B0058ZIGVC",
      "score":3.58,
      "title":"Fossil AM4373 Decker Stainless Steel Watch, Black"
   },
   {
      "asin":"B007SUZGNG",
      "score":3.55,
      "title":"XOXO Women\u0027s XO5473 Rose Gold Tone and Black Epoxy Bracelet Watch"
   },
   {
      "asin":"B005LBY42I",
      "score":3.54,
      "title":"Fossil Men\u0027s FS4674 Ansel Chronograph Stainless Watch"
   },
   {
      "asin":"B003EKIQDG",
      "score":3.51,
      "title":"U.S. Polo Assn. Classic Men\u0027s US5159 Black Synthetic Leather Strap Watch"
   },
   {
      "asin":"B004DKNHZS",
      "score":3.47,
      "title":"I By Invicta Men\u0027s 41704-003 Stainless Steel Black Dress Watch"
   },
   {
      "asin":"B003FN926M",
      "score":3.45,
      "title":"Fossil Men\u0027s FS4532 Stainless Steel Bracelet Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B003DIHB6M",
      "score":3.41,
      "title":"XOXO Women\u0027s XO5218 Black Dial Two-Tone Half Cuff and Half Bracelet Watch"
   },
   {
      "asin":"B007GO7BVY",
      "score":3.39,
      "title":"Fossil Dean FS4721 Stainless Steel Watch, Smoke"
   },
   {
      "asin":"B00AMNY46G",
      "score":3.39,
      "title":"Tommy Hilfiger Women\u0027s 1781306 Stainless Steel Watch with White Silicone Strap"
   },
   {
      "asin":"B003DIHD6U",
      "score":3.37,
      "title":"U.S. Polo Assn. Classic Men\u0027s USC80038 Analogue Black Dial Bracelet Watch"
   },
   {
      "asin":"B007OAESMK",
      "score":3.36,
      "title":"Tommy Hilfiger Men\u0027s 1710318 Classic Tank Roman Numeral Enamel Dial Watch"
   },
   {
      "asin":"B00AATE9IU",
      "score":3.36,
      "title":"XOXO Women\u0027s XO5591 Rose Gold-Tone Bracelet Watch"
   },
   {
      "asin":"B006G0F0G2",
      "score":3.35,
      "title":"Tommy Hilfiger 1790855 Sport Stainless Steel and Blue Silicone Watch"
   },
   {
      "asin":"B00AJGNA20",
      "score":3.33,
      "title":"Fossil Retro Traveler Three Hand Stainless Steel Watch Am4441"
   },
   {
      "asin":"B0058ZAKQ6",
      "score":3.32,
      "title":"Machine Stainless Steel Watch"
   },
   {
      "asin":"B00AG37MJS",
      "score":3.28,
      "title":"FOSSIL CH2848 Retro Traveler Chronograph Stainless Steel Watch"
   },
   {
      "asin":"B00B81RXC8",
      "score":3.27,
      "title":"Bradshaw Women\u0027s Watch"
   },
   {
      "asin":"B008BPOLZG",
      "score":3.27,
      "title":"XOXO Women\u0027s XO5300 Rose Gold-Tone and White Bracelet Watch"
   },
   {
      "asin":"B00AG37H8Y",
      "score":3.26,
      "title":"Fossil Men\u0027s FS4795 Dean Chronograph Stainless Steel Watch"
   },
   {
      "asin":"B00AFTQQQI",
      "score":3.24,
      "title":"Fossil Men\u0027s CH2869 Retro Traveler Analog Display Analog Quartz Grey Watch"
   },
   {
      "asin":"B00880ZWI4",
      "score":3.22,
      "title":"Fossil ME1120 Machine Twist Stainless Steel Watch"
   },
   {
      "asin":"B003DIHBEY",
      "score":3.21,
      "title":"XOXO Women\u0027s XO115 Black Enamel and Rhinestone Accent Bracelet Watch"
   },
   {
      "asin":"B007GNHSV8",
      "score":3.19,
      "title":"Fossil AM4384 Decker Silicone Watch, Black"
   },
   {
      "asin":"B0048LOGAC",
      "score":3.17,
      "title":"Fossil Men\u0027s FS4584 Stainless Steel Analog Grey Dial Watch"
   },
   {
      "asin":"B005LBY6Q2",
      "score":3.17,
      "title":"Fossil Men\u0027s CH2802 Stainless Steel Analog Grey Dial Watch"
   },
   {
      "asin":"B0052ZIGHM",
      "score":3.16,
      "title":"Fossil Men\u0027s CH2731 Dylan Blue Dial Watch"
   },
   {
      "asin":"B003G3799I",
      "score":3.08,
      "title":"Fossil Men\u0027s FS4531 Black Stainless Steel Bracelet Black Analog Dial Chronograph Watch"
   },
   {
      "asin":"B0068RLT3Q",
      "score":3.07,
      "title":"U.S. Polo Assn. Classic Men\u0027s US2040  Silver-Tone Bracelet with Two Interchangeable Strap Bands Watch Set"
   },
   {
      "asin":"B00AG37G5I",
      "score":3.03,
      "title":"Fossil Men\u0027s FS4784 Townsman Chronograph Stainless Steel Watch"
   },
   {
      "asin":"B00BOVD1VO",
      "score":2.98,
      "title":"GUESS Women\u0027s U0208L2 Dazzling Iconic Logo Black \u0026amp; Gold-Tone Watch"
   },
   {
      "asin":"B00AMO07BQ",
      "score":2.93,
      "title":"Tommy Hilfiger Men\u0027s 1790918 \u0026quot;Casual\u0026quot; Stainless Steel Watch"
   },
   {
      "asin":"B009LSKCEQ",
      "score":2.87,
      "title":"Fossil Retro Traveler Chronograph Silicone Watch - Black Ch2851"
   },
   {
      "asin":"B00CB31602",
      "score":2.83,
      "title":"SquareTrade 2-Year Watch Protection Plan ($50-75)"
   },
   {
      "asin":"B0094KUKGQ",
      "score":2.83,
      "title":"Fossil Machine Three Hand Stainless Steel Watch Fs4773"
   },
   {
      "asin":"B00DB2STYI",
      "score":2.77,
      "title":"Nautica Men\u0027s N11609G Classic Analog Sport Watch"
   },
   {
      "asin":"B005OQL4UA",
      "score":2.76,
      "title":"Bistro 4-Cup Electric French Press Coffeemaker, 0.5-Liter 17-Ounce, Off-White"
   },
   {
      "asin":"B003EKNMPS",
      "score":2.75,
      "title":"U.S. Polo Assn. Sport Men\u0027s US8163 Bracelet Watch with Black and Gun Metal Band"
   },
   {
      "asin":"B00AFTSNBE",
      "score":2.73,
      "title":"Fossil Retro Traveler Chronograph Stainless Steel Watch Ch2866"
   },
   {
      "asin":"B004EKAROG",
      "score":2.68,
      "title":"Bodum Bistro Electric French Press Coffee Maker and Tea Dripper, 4-Cup, Black"
   },
   {
      "asin":"B004C1AFY4",
      "score":2.56,
      "title":"Fossil Men\u0027s CH2692 Decker Stainless Steel Blue Dial Watch"
   },
   {
      "asin":"B00AFSRJJM",
      "score":2.5,
      "title":"Fossil Women\u0027s AM4494 Cecile White Plastic Watch"
   },
   {
      "asin":"B00AFTP0EC",
      "score":2.46,
      "title":"Fossil Heather Three Hand Leather Watch - Pink Es3277"
   },
   {
      "asin":"B009BEOK8E",
      "score":2.46,
      "title":"Fossil Nate Chronograph Leather Watch - White Jr1423"
   },
   {
      "asin":"B00AG37E2S",
      "score":2.28,
      "title":"FOSSIL CH2849 Retro Traveler Chronograph Stainless Steel Watch"
   },
   {
      "asin":"B00HFYZ23E",
      "score":2.22,
      "title":"Fossil Men\u0027s CH2916 Retro Traveler Analog Display Analog Quartz Silver Watch"
   },
   {
      "asin":"B00AG37FLI",
      "score":2.22,
      "title":"Fossil Men\u0027s CH2847 Retro Traveler Chronograph Stainless Steel Watch"
   },
   {
      "asin":"B0056RYA5I",
      "score":2.17,
      "title":"Fossil Men\u0027s JR1265 Jake Stainless Steel Watch"
   },
   {
      "asin":"B007GNI0YW",
      "score":2.14,
      "title":"Fossil CH2814 Keaton Stainless Steel Watch"
   },
   {
      "asin":"B00AMO0928",
      "score":2.06,
      "title":"Tommy Hilfiger Men\u0027s 1790919 \u0026quot;Casual\u0026quot; Stainless Steel Watch"
   },
   {
      "asin":"B00F2NRQRU",
      "score":2.0,
      "title":"Fossil Men\u0027s JR1445 Nate Analog Display Analog Quartz Silver Watch"
   },
   {
      "asin":"B00DUCIWTQ",
      "score":1.71,
      "title":"Fossil Riley Multifunction Stainless Steel Watch - Gold-Tone Es3384"
   },
   {
      "asin":"B00C6PHLUO",
      "score":1.65,
      "title":"Fossil Men\u0027s FS4844 Grant Analog Display Analog Quartz Silver Watch"
   },
   {
      "asin":"B000BW9Y04",
      "score":1.5,
      "title":"Meade ETX-80 Hard Carry Case."
   },
   {
      "asin":"B00FF8CQEK",
      "score":1.11,
      "title":"Fossil Men\u0027s FS4863 Foreman Analog Display Analog Quartz Black Watch"
   },
   {
      "asin":"B00HG09BUM",
      "score":0.83,
      "title":"Fossil Machine Chronograph Black Dial Gunmetal Ion-plated Mens Watch FS4931"
   },
   {
      "asin":"B00AG37D38",
      "score":0.83,
      "title":"Fossil Machine Chronograph Stainless Steel Watch Fs4791"
   },
   {
      "asin":"B00C2HOGDG",
      "score":0.83,
      "title":"Swatch Irony Be Surprised Black Dial Stainless Steel Ladies Watch YSS279G"
   },
   {
      "asin":"B00HVB81LG",
      "score":0.8,
      "title":"Fossil Men\u0027s FS4888 Dean Analog Display Analog Quartz Silver Watch"
   }
]
```

</details>

## Case #3: Gift for Hiker

#### Input
```
bin/hadoop jar share/gifty/recommend-me.jar output/related-products-map/ output/product-scores-map/ output/product-metadata-map B000093ILF B00009NH3S B0000AFSX4 B0000YWMSO B00019H602
```

#### Output
<details>

```
[
   {
      "asin":"B000WEOQV8",
      "score":4.84,
      "title":"Weber 7416 Rapidfire Chimney Starter"
   },
   {
      "asin":"B0009JKG9M",
      "score":4.76,
      "title":"Lodge LCC3 Pre-Seasoned Cast-Iron Combo Cooker, 3-Quart"
   },
   {
      "asin":"B00008GKDW",
      "score":4.75,
      "title":"Lodge L12DCO3 Deep Camp Dutch Oven, 8-Quart"
   },
   {
      "asin":"B00006JSUH",
      "score":4.72,
      "title":"Lodge Camp Dutch Oven, 6 Qt"
   },
   {
      "asin":"B00006JSUF",
      "score":4.71,
      "title":"Lodge L8DO3 Pre-Seasoned Dutch Oven, 5-Quart"
   },
   {
      "asin":"B004QM8SLG",
      "score":4.63,
      "title":"Lodge L5HS3 5-Piece Pre-Seasoned Cast-Iron Cookware Set"
   },
   {
      "asin":"B00063RXKQ",
      "score":4.59,
      "title":"Lodge L8DOT3 Pre-Seasoned Cast-Iron Meat Rack/Trivet, 8-inch"
   },
   {
      "asin":"B000GCRWCG",
      "score":4.58,
      "title":"Emergency Mylar Thermal Blankets (Pack of 10)"
   },
   {
      "asin":"B00063RWYI",
      "score":4.57,
      "title":"Lodge L8DOL3 Pre-Seasoned Cast-Iron Dutch Oven with Dual Handles, 5-Quart"
   },
   {
      "asin":"B009IH0ICG",
      "score":4.55,
      "title":"Weber 7447 Compact Rapidfire Chimney Starter"
   },
   {
      "asin":"B00008GKDN",
      "score":4.53,
      "title":"Lodge L9OG3 Pre-Seasoned Cast-Iron Round Griddle, 10.5-inch"
   },
   {
      "asin":"B0026OOS60",
      "score":4.46,
      "title":"Chainmate Cm-24ssp 24-inch Survival Pocket Chain Saw with Pouch"
   },
   {
      "asin":"B005Q5P914",
      "score":4.45,
      "title":"New Balance KJ990 Lace-Up Running Shoe (Infant/Toddler)"
   },
   {
      "asin":"B000FNLXWG",
      "score":4.43,
      "title":"Rome\u0027s #1705 Square Pie Iron with Steel and Wood Handles"
   },
   {
      "asin":"B0001DJVGU",
      "score":4.42,
      "title":"Lodge Pro-Logic P12D3 Cast Iron Dutch Oven, Black, 7-Quart"
   },
   {
      "asin":"B0039UU9UO",
      "score":4.42,
      "title":"Lodge SCRAPERPK Durable Polycarbonate Pan Scrapers, Red and Black, 2-Pack"
   },
   {
      "asin":"B0000TPDJE",
      "score":4.42,
      "title":"Lodge A5 Camp Dutch Oven Lid Lifter"
   },
   {
      "asin":"B0008G2W0M",
      "score":4.4,
      "title":"Lodge A1-12 Camp Dutch Oven Tote Bag, 12-inch"
   },
   {
      "asin":"B0009PURJA",
      "score":4.4,
      "title":"Coleman 5010D700T Camp Oven"
   },
   {
      "asin":"B002HU086C",
      "score":4.39,
      "title":"Coleman 10-Inch Steel Tent Stakes"
   },
   {
      "asin":"B0002YUNXS",
      "score":4.39,
      "title":"Camp Chef Deluxe 12-Quart Dutch Oven"
   },
   {
      "asin":"B00B4QJVQI",
      "score":4.39,
      "title":"crocs 14809 Hndle Mcqueen Boot (Toddler/Little Kid)"
   },
   {
      "asin":"B00BCJXLHW",
      "score":4.37,
      "title":"Timberland Women\u0027s Teddy Fleece Fold Down WP Ankle Boot"
   },
   {
      "asin":"B000J084KY",
      "score":4.37,
      "title":"Coleman Tie-Style Mantle, 4-Pack"
   },
   {
      "asin":"B000P9F1EQ",
      "score":4.37,
      "title":"Texsport Black Ice The Scouter Hard Anodized Cook Set"
   },
   {
      "asin":"B0009PUQ50",
      "score":4.3,
      "title":"Coleman Rugged Battery Powered Lantern (Family Size)"
   },
   {
      "asin":"B000EH0NLK",
      "score":4.3,
      "title":"Coleman MicroPacker Compact Battery Lantern"
   },
   {
      "asin":"B0000B15HP",
      "score":4.29,
      "title":"Coghlan\u0027s Non-Stick Two Burner Griddle"
   },
   {
      "asin":"B0002LYZNK",
      "score":4.29,
      "title":"Lodge A53 Original Finish Camp Dutch Oven Lid Stand"
   },
   {
      "asin":"B0001DJVGK",
      "score":4.27,
      "title":"Lodge Pro-Logic P10D3 Cast Iron Dutch Oven, Black, 4-Quart"
   },
   {
      "asin":"B00063RXM4",
      "score":4.26,
      "title":"Lodge LCK3 Pre-Seasoned Country Kettle, 1-pint"
   },
   {
      "asin":"B00A1LUFK8",
      "score":4.26,
      "title":"5 Gallon White Bucket \u0026amp; Lid - Set of 3 - Durable 90 Mil All Purpose Pail - Food Grade - BPA Free Plastic -"
   },
   {
      "asin":"B000P9ISSC",
      "score":4.25,
      "title":"Texsport Black Ice The Trailblazer H.A. QT. Cook Set"
   },
   {
      "asin":"0764537148",
      "score":4.23,
      "title":"Cast Iron Cooking For Dummies"
   },
   {
      "asin":"B000VXEW00",
      "score":4.23,
      "title":"Bayou Classic 7485, Tripod Stand with Chain and Bag"
   },
   {
      "asin":"B003Z8YQAY",
      "score":4.2,
      "title":"Camp Chef  DOLL14 Cast Iron 14-Inch Dutch Oven Lid Lifter for Dutch Ovens"
   },
   {
      "asin":"B004EBJLXS",
      "score":4.2,
      "title":"High Sierra Tech Series 59404 Titan 55 Internal Frame Pack"
   },
   {
      "asin":"B000P9F092",
      "score":4.18,
      "title":"Texsport Heavy Duty Camp 24\u0026quot; X 16\u0026quot; Grill"
   },
   {
      "asin":"1586857614",
      "score":4.17,
      "title":"Camp Cooking: 100 Years"
   },
   {
      "asin":"B0009J3ROI",
      "score":4.14,
      "title":"Lodge A5-2 Red Leather Gloves"
   },
   {
      "asin":"B00168YEK2",
      "score":4.14,
      "title":"Coleman Stove Carry Case"
   },
   {
      "asin":"B004R1GXPO",
      "score":4.12,
      "title":"Coleman Expedition First Aid Kit"
   },
   {
      "asin":"B000VTMV5C",
      "score":4.11,
      "title":"Universal Housewares Pre-Seasoned Cast Iron Camping 5-Quart Dutch Oven"
   },
   {
      "asin":"0762778083",
      "score":4.11,
      "title":"The Scout\u0027s Dutch Oven Cookbook"
   },
   {
      "asin":"B0009PUQ8M",
      "score":4.1,
      "title":"Coleman Tent Kit"
   },
   {
      "asin":"B00339912S",
      "score":4.1,
      "title":"Coleman Pack-Away Kitchen"
   },
   {
      "asin":"B000LC84PK",
      "score":4.1,
      "title":"Coghlan\u0027s 511A Camper Egg Carrier"
   },
   {
      "asin":"B0045TAY30",
      "score":4.09,
      "title":"Gregory Baltoro 65 Technical Pack"
   },
   {
      "asin":"1586857851",
      "score":4.08,
      "title":"101 Things to Do with a Dutch Oven"
   },
   {
      "asin":"B000GOW0GM",
      "score":4.04,
      "title":"Panacea 15351 Fireplace Shovel, Black, 19.25-Inch"
   },
   {
      "asin":"B00FNHMERW",
      "score":4.04,
      "title":"Breckelles Womens Blazer-11 Heels Booties"
   },
   {
      "asin":"B007H3TMMA",
      "score":4.03,
      "title":"Caterpillar Men\u0027s Transform Boot"
   },
   {
      "asin":"0071361103",
      "score":4.03,
      "title":"The Essential Wilderness Navigator: How to Find Your Way in the Great Outdoors, Second Edition"
   },
   {
      "asin":"B00363WZSS",
      "score":4.03,
      "title":"Coleman Packaway Deluxe Camp Kitchen"
   },
   {
      "asin":"1599559714",
      "score":3.98,
      "title":"Best of the Black Pot: Must-Have Dutch Oven Favorites"
   },
   {
      "asin":"B00005OU9D",
      "score":3.97,
      "title":"Coleman Two-Burner Propane Stove"
   },
   {
      "asin":"1563832607",
      "score":3.96,
      "title":"Fix It In Foil"
   },
   {
      "asin":"B000VNITQS",
      "score":3.94,
      "title":"Coghlan\u0027s Camp Shower"
   },
   {
      "asin":"B0010O748Q",
      "score":3.93,
      "title":"Magnesium Fire Starter"
   },
   {
      "asin":"B001TS6WWC",
      "score":3.92,
      "title":"Coleman WeatherMaster Screened 6 Tent"
   },
   {
      "asin":"B0073E1O6U",
      "score":3.9,
      "title":"Lodge A5DOL Parchment Paper Dutch Oven Liners, pack of 8"
   },
   {
      "asin":"B0000V1PLW",
      "score":3.89,
      "title":"Camp Chef Deluxe 10IN Dutch Oven"
   },
   {
      "asin":"1565237242",
      "score":3.84,
      "title":"Easy Campfire Cooking: 200+ Family Fun Recipes for Cooking Over Coals and In the Flames with a Dutch Oven, Foil Packets, and More!"
   },
   {
      "asin":"B001CZFJDU",
      "score":3.82,
      "title":"Texsport Campfire Tripod"
   },
   {
      "asin":"B001DC5HG6",
      "score":3.81,
      "title":"Stansport Campers Coffee Pot"
   },
   {
      "asin":"B000P9IRLA",
      "score":3.8,
      "title":"Texsport Rotisserie \u0026amp; Spit Grill"
   },
   {
      "asin":"B000EGM1UM",
      "score":3.79,
      "title":"Coghlan\u0027s Expandable Water Carrier"
   },
   {
      "asin":"B000RA3Z0K",
      "score":3.76,
      "title":"Granite Ware 6006-1 3-Quart Coffee Boiler"
   },
   {
      "asin":"B00168YEQ6",
      "score":3.75,
      "title":"Coleman 9-Cup Aluminum Coffee Pot"
   },
   {
      "asin":"0882906887",
      "score":3.75,
      "title":"The Beginners Guide to Dutch Oven Cooking"
   },
   {
      "asin":"B0008G2W2K",
      "score":3.74,
      "title":"Lodge 5TP2 Tall Boy Tripod"
   },
   {
      "asin":"B0008G2W0C",
      "score":3.73,
      "title":"Lodge A1-10 Camp Dutch Oven Tote Bag, 10-inch"
   },
   {
      "asin":"B000P9D062",
      "score":3.73,
      "title":"Texsport Heavy Duty Camp Large Grill"
   },
   {
      "asin":"B001AZLNVY",
      "score":3.73,
      "title":"CUSCUS 75+10L 5400ci Internal Frame Camping Hiking Travel Backpack"
   },
   {
      "asin":"B00029I3EE",
      "score":3.7,
      "title":"Lodge 3TP2 Camp Dutch Oven Tripod"
   },
   {
      "asin":"B0049BG4YC",
      "score":3.65,
      "title":"Ride Free Motorcycle Distressed Retro Vintage Tin Sign"
   },
   {
      "asin":"B0009PUR86",
      "score":3.64,
      "title":"Coleman Speckled Enamelware Dining Kit (Red)"
   },
   {
      "asin":"B0054I2XV2",
      "score":3.64,
      "title":"Disposable Foil Dutch Oven Liner, 12 Pack 10\u0026quot; 4Q liners, No more Cleaning, Seasoning your Dutch ovens. Lodge, Camp Chef. 12-10\u0026quot;"
   },
   {
      "asin":"0071546596",
      "score":3.63,
      "title":"The Outdoor Dutch Oven Cookbook, Second Edition"
   },
   {
      "asin":"B000P9IRKQ",
      "score":3.61,
      "title":"Texsport Heavy Duty Camp 16\u0026quot; x 12\u0026quot; Grill"
   },
   {
      "asin":"B0099MJT6Q",
      "score":3.58,
      "title":"Toddler\u0027s New Balance KL574KBI (Raven-Pink)"
   },
   {
      "asin":"B001FSJHHI",
      "score":3.55,
      "title":"Stansport Cast Iron Cooking Tripod"
   },
   {
      "asin":"B000BQWLE6",
      "score":3.53,
      "title":"Hopkins TRASH-BLA Pop Up Trash Can"
   },
   {
      "asin":"B00CMLQXM4",
      "score":3.47,
      "title":"H2H Mens Various Colors Fine Cotton Giraffe Polo Shirts"
   },
   {
      "asin":"B00FX48GHM",
      "score":3.47,
      "title":"Thomas \u0026amp; Friends \u0026quot;Tank Engine\u0026quot; Blue Infant Toddler Sock Top Slippers"
   },
   {
      "asin":"B00DEXNYTO",
      "score":3.44,
      "title":"KLOUD City \u0026reg; Nylon Backpack Rain Cover for Hiking Camping Traveling (Size: L / M / S)"
   },
   {
      "asin":"B001CZ9UY4",
      "score":3.42,
      "title":"Cold Steel Trail Boss Hickory Handle"
   },
   {
      "asin":"B009X2FN3U",
      "score":3.41,
      "title":"AutoM DIY 100pcs 9.5mm Gold Metal Bullet Spike Studs Rivet Punk Bag Belt Leathercraft"
   },
   {
      "asin":"B0017GZ93O",
      "score":3.4,
      "title":"Wenzel Deluxe Mess Kit"
   },
   {
      "asin":"B001XUS4QI",
      "score":3.34,
      "title":"Stansport Cast Iron 4 Quart Dutch Oven"
   },
   {
      "asin":"B009UC76VA",
      "score":3.34,
      "title":"American Trails Ozzie and Harriet Double 2 Person Giant Sleeping Bag, 80-Inch x 66-Inch"
   },
   {
      "asin":"B007WX609S",
      "score":3.31,
      "title":"Air Mattress (Single Size:73\u0026quot;x29\u0026quot;x7.5\u0026quot;)"
   },
   {
      "asin":"B0007LNJ3M",
      "score":3.28,
      "title":"The Camp Chef DO-5-Mini 3/4 Quart Dutch Oven"
   },
   {
      "asin":"B00168XMD2",
      "score":3.25,
      "title":"Coleman Cool Zephyr Ceiling Fan with Light"
   },
   {
      "asin":"B0009PUR4A",
      "score":3.2,
      "title":"Coleman Aluminum Mess Kit"
   },
   {
      "asin":"B001ASBUVY",
      "score":3.2,
      "title":"Rome\u0027s #117 Tri-Pod Grill with 21.5 Inch Diameter Grill Grate, Chrome Plated Stee"
   },
   {
      "asin":"B0019UDNCM",
      "score":3.12,
      "title":"Texsport Cast Iron Griddle - 9-1/2-Inch x 20-Inch"
   },
   {
      "asin":"B006EHACXS",
      "score":3.02,
      "title":"Coghlans Collapsible Water Container"
   },
   {
      "asin":"B00023BH8Y",
      "score":3.01,
      "title":"Gregory Whitney 95"
   },
   {
      "asin":"B00CENV5CI",
      "score":2.98,
      "title":"KLOUD City \u0026reg; Black nylon backpack rain cover for hiking / camping / traveling (Size: L)"
   },
   {
      "asin":"B0074HSGO4",
      "score":2.96,
      "title":"Timberland Men\u0027s Newmarket High-Top Sneaker"
   },
   {
      "asin":"B000P9IRPG",
      "score":2.93,
      "title":"Texsport Non-Stick Popcorn Popper"
   },
   {
      "asin":"B003T0E9F0",
      "score":2.92,
      "title":"Sturgis Bike Week Classic Rally Motorcycle Distressed Retro Vintage Tin Sign"
   },
   {
      "asin":"B00BCJX6TU",
      "score":2.87,
      "title":"Timberland Women\u0027s Brookton Roll Top Boot"
   },
   {
      "asin":"B003GEV9KW",
      "score":2.87,
      "title":"Jordan 6 Retro (Td) Toddlers"
   },
   {
      "asin":"B001A62CK4",
      "score":2.83,
      "title":"Texsport Tripod Grill"
   },
   {
      "asin":"B004H0YWSA",
      "score":2.71,
      "title":"Texsport Pre-Seasoned Cast Iron Dutch Oven - 8 Quart"
   },
   {
      "asin":"B002V9XVDQ",
      "score":2.59,
      "title":"Coleman Family Cook Set"
   },
   {
      "asin":"B001TS6N44",
      "score":2.57,
      "title":"Coleman Cast Iron Dutch Oven"
   },
   {
      "asin":"B004H10HKG",
      "score":2.55,
      "title":"Texsport Pre-Seasoned Cast Iron Dutch Oven without Legs - 8 Quart"
   },
   {
      "asin":"B001RTZVY8",
      "score":2.34,
      "title":"Texsport Cast Iron Dutch Oven - 8 Quart"
   },
   {
      "asin":"B004SKPQX4",
      "score":2.3,
      "title":"Gerber Graduates Fruit Pick Ups - Diced Apples, 4.5-Ounce (Pack of 8)"
   },
   {
      "asin":"B0018E57PA",
      "score":2.25,
      "title":"Stansport Camping 12 Egg Container"
   },
   {
      "asin":"B00369X75C",
      "score":2.22,
      "title":"Arcteryx Altra 65 LT Pack"
   },
   {
      "asin":"B00B1VBS5S",
      "score":2.17,
      "title":"Osprey Xenith 88 Pack"
   },
   {
      "asin":"B004QMZS0A",
      "score":2.17,
      "title":"Timberland Women\u0027s Premium Boot"
   },
   {
      "asin":"B009PPFFK6",
      "score":2.12,
      "title":"King Kooker CI4S Pre-seasoned Outdoor Cast Iron Dutch Oven, 4-Quart"
   },
   {
      "asin":"B004H13NZM",
      "score":1.95,
      "title":"Texsport Pre-Seasoned Cast Iron Dutch Oven without Legs - 4 Quart"
   },
   {
      "asin":"B009PPFFLU",
      "score":1.83,
      "title":"King Kooker Pre-seasoned Outdoor Cast Iron Dutch Oven with Feet, 8-Quart"
   },
   {
      "asin":"B001EPPJB0",
      "score":1.43,
      "title":"Gregory Denali Pro 105 Mountaineering Pack"
   },
   {
      "asin":"B005IDBQWA",
      "score":1.38,
      "title":"Gregory Fury 40 Backpack"
   },
   {
      "asin":"B005IDBNF0",
      "score":1.33,
      "title":"Gregory Savant 48 Backpack"
   },
   {
      "asin":"B00GT1RKE8",
      "score":1.2,
      "title":"Toddler Boys\u0027 6 Inch Premium Boots"
   },
   {
      "asin":"B005GS6X2A",
      "score":0.83,
      "title":"Kelty Ridgeway 50.8 Liter Internal Frame Backpack \u0026amp; Hydration System"
   },
   {
      "asin":"B0000AUT69",
      "score":0.83,
      "title":"Texsport CAST IRON LID LIFTER"
   },
   {
      "asin":"B0010FNKQU",
      "score":0.5,
      "title":"Texsport Cast Iron Dutch Oven - 2 Quart"
   }
]
```

</details>