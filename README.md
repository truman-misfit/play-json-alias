# play-json-alias
A JSON attributes renaming module for Play! Framework.

# Preview
The original JSON Object
```json
{
  "oneLongAttribute" : "attribute",
  "anotherLongAttribute" : "another attribute",
  "oneAnotherLongAttribute" : "another once again attribute"
}
```

The encoded JSON Object
```json
{
  "g9" : "attribute",
  "ga" : "another attribute",
  "gb" : "another once again attribute"
}
```

# Introduction
This module provide two methods and 4 types of alias storage strategy:
 * Local in memory (A singleton counter service and two alias mapping objects)
 * File (Keep the data generated in the first strategy in a file so that it can be pre-loaded in the next start-up).
 * Redis (Powered by Redis/ElastiCache).
 * DynamoDB (Powered by DynamoDB).

# Steps
#### Step 1. Generate unique ID for attribute
Each time the encoder find a new JSON attribute, a unified Counter Service will generate a
new count as a global unique BigInt type id for its attribute.

#### Step 2. Encoded by [hashids](http://hashids.org/)
hashids is a kind of encoding strategy for BigInt data. So the unqiue counter number of each different attribute can be encoded by hashids to short (literally fix length of) String.

#### Step 3. Alias data persistence
4 modes have been provided by dependency injection. You can just change the configure below in your application.conf file:
```
# JSON serial configuration
# 3 modes :
# - local
# - file (coming soon)
# - redis (coming soon)
# - dynamo (coming soon)
ms.module.json.alias.mode = "local"
```

# Future
- Embedded JSON structure encoding, for example:
  

# Author
truman@misfit.com
