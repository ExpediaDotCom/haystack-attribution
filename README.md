[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-console.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-attribution)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-attribution
Repo for attribution components for Haystack system.

## haystack-attributor module

For smooth operation of Haystack cluster, we need to monitor the total traffic and which service is sending how much 
data. Sometimes, we see a service going berserk, which puts severe load on whole infrastructure and impacting all the 
services. Attributor is the component responsible for measuring and keeping track of the volume of spans sent by each 
service.

In addition to few basic attribution fields like span count, span size, operation count; one can also do attribution on
the basis of span tags.

For using this attributed data, there are two ways provided for persistence. One can either setup the
email for getting the attribution report as an email at regular intervals. Or other way is to dump the report in s3 and 
use it the way user wants to use.

-----------------------------------------
## haystack-attribution-persistence-email module

This sub-component fetches the stats from attributor via rest call and sends the attributed data / stats as an email. 
It is scheduled as a cron job in our k8s cluster to send the email (for previous calender day, currently). 

What it does:
- rest call to attributor to fetch service stats for previous day's time window
- creates an email template for stats
- email using aws ses service / smtp to all the ids in configuration

-----------------------------------------
## haystack-attribution-persistence-s3 module

Component which persist the attributed data to s3 for consumption by any other tool. This component is also scheduled 
as a cron job in our k8s cluster to persist the cost attribution details as csv in S3. 

What it does:
- rest call to attributor to fetch service stats (for previous day's time window, currently)
- transforms the stats data using a transformer (transformer can be overridden)
- persist the transformed data to s3

-----------------------------------------
-----------------------------------------

## Example to show how attribution works

##### Incoming sample spans:

`Span 1`:
```
traceId = "traceId_1"
spanId = "spanId_1"
parentSpanId = "parentSpanId_1"
serviceName = "service_1"
operationName = "operation_1"
startTime = "1542708831000000"
duration = "1000"
tags : [
    key1_for_count: "datacenter_1",
    key2_for_count: "datacenter_2",
    key1_for_sum: "10",
    key2_for_sum: "20",
    key1_for_bagg: "service_1_baggage"
]
```

`Span 2`:
```
traceId = "traceId_2"
spanId = "spanId_2"
parentSpanId = "parentSpanId_2"
serviceName = "service_1"
operationName = "operation_2"
startTime = "1542708831000000"
duration = "2000"
tags : [
    key1_for_count: "datacenter_3",
    key1_for_sum: "30"
]
```

`Span 3`:
```
traceId = "traceId_3"
spanId = "spanId_3"
parentSpanId = "parentSpanId_3"
serviceName = "service_1"
operationName = "operation_3"
startTime = "1542708831000000"
duration = "3000"
tags : [
    key2_for_count: "datacenter_4",
    key2_for_sum: "40"
]
```

##### Config for attributing span tags
Sample config for _Count, Sum & Baggage_ operator

`tags.json`

```
{
  "items": [
    {
      "attributeName": "someKey_agg_count",
      "spanTagKeys": [
        "key1_for_count",
        "key2_for_count"
      ],
      "operatorType": "COUNT",
      "defaultValue": "0",
      "valueType": "COUNT"
    },
    {
      "attributeName": "someKey_agg_sum",
      "spanTagKeys": [
        "key1_for_sum",
        "key2_for_sum"
      ],
      "operatorType": "SUM",
      "defaultValue": "0",
      "valueType": "BYTES"
    },
    {
      "attributeName": "someKey_bagg",
      "spanTagKeys": [
        "key1_for_bagg"
      ],
      "operatorType": "BAGGAGE",
      "defaultValue": "baggage_default_value",
      "valueType": "NONE"
    }
  ]
}
```

###### Supported Operator Types
Type of operation that needs to be performed when two spans are aggregated.

`operatorType`: COUNT, SUM OR BAGGAGE

###### Supported Value Types
For formatting the result in email attribution report.

`valueType`: COUNT, BYTES OR NONE

Refer to the base.conf for attributor module for more description on _operatorType_ and _valueType_.

##### Attributed ServiceStats result:
So, result for `Count` operator for service: *service_1*  will be the number of occurrences of "key1_for_count" & 
"key2_for_count" key in all the spans for this service.

And result for `Sum` operator for the same service will be sum of the values for the keys "key1_for_sum" & 
"key2_for_sum".

Result for `Baggage` operator for the same service will be value of "key1_for_bagg" or if "key1_for_bagg" is not present
then "baggage_default_value".

Final result for ServiceStats would be:
```
{
    serviceName: "service_1"
    spanCount: "3"
    spanSizeBytes: "137"
    vNodeId: "123"
    lastSeen: "1542708831000000"
    attributedTags: {
        {
            key: "someKey_agg_count",
            value: "4"
        },
        {
            key: "someKey_agg_sum",
            value: "100"
        },
        {
            key: "someKey_bagg",
            value: "service_1_baggage"
        }
    }
}  
```

-------------------------

### Email config
To override the default email html template supply this config.

`notify.email.override.template`

```
<html>
    <head>
        <style>
            table, th, td {
                border: solid;
                border-width: 1px;
                border-collapse: collapse;
            }
            th, td {
                padding: 5px;
                text-align: left;
            }
        </style>
    </head>

    Hello Team,
    <br>
    <br>
    Please find the daily usage report:
    <br>
    <br>
    <table>
        <tr>
            <th id="sequenceNo">No.</th>
            <th id="serviceName">Service Name</th>
            <th id="spanCount">Span Count</th>
            <th id="spanSizeBytes">Span Size</th>
            <th id="operationCount">Operations Count</th>
            <th id="someKey_agg_count">Agg Count using Tags</th>
        </tr>
        {{row}}
    </table>

    <br>
    Thanks,
    <br>
    Haystack Bot
</html>
```

Note the additional column _Agg Count using Tag_ in the template.

------------------

### Transformer config in s3 module
Transformers config for dumping the data to s3 in required format.

`transformers.config`

```
{
  "items": [
    {
      "id": "t1",
      "classRelativePath": "com.expedia.www.haystack.attribution.persistence.s3.transformer.ServiceStatsCsvTransformer",

      "customTags": {
        "tagKey1": "tagValue1",
        "tagKey2": "tagValue2"
      }
    }
  ]
}
```

### S3 config where data needs to be dumped

`persist.s3`

```
{
  "items": [
    {
      "enabled": true,
      "useStsRole": false,
      "stsRoleArn": "",
      "bucket": "haystack",
      "folderPath": "attribution-dev/spans", // Optional. Just set "", if not required
      "transformerId": ["t1"]
    }
  ]
}
```

-----------------------

## Building

#### Prerequisite: 

* Make sure you have Java 1.8
* Make sure you have Scala 2.12.*
* Make sure you have docker 1.13 or higher

#### Build

You can choose to build the individual subdirectories if you're working on any specific sub-app but in case you are making changes to the contract such as span or ServiceStats which would effect multiple modules you should run

```
make all
```
This would build all the individual apps and including unit tests & jar + docker image build for haystack-attribution.
