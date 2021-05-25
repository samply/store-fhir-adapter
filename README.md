# Store FHIR Adapter

This service provides a classic Samply.Store API in front of a FHIR server.

## Usage

Check out this repo and run:

```sh
mvn clean package
docker-compose build
docker-compose up
```

Import data into Blaze:

```sh
blazectl --server http://localhost:8090/fhir upload <dir>
```

Create a request:

```sh
curl 'http://localhost:8080/requests' -H 'Content-Type: application/xml' -d '<foo></foo>' -vs 2>&1 | grep Location
```

The Location header contains the request identifier (an UUID).

Fetch statistics:

```sh
curl 'http://localhost:8080/requests/3115a0a9-1e32-47ce-867d-1f4f4924990a/stats' -H 'Accept: application/xml'
```

which should output:

```xml
<ns2:queryResultStatistic xmlns:ns2="http://schema.samply.de/osse/QueryResultStatistic">
  <requestId>3115a0a9-1e32-47ce-867d-1f4f4924990a</requestId>
  <numberOfPages>2</numberOfPages>
  <totalSize>80</totalSize>
</ns2:queryResultStatistic>
```

Fetch the first result page:

```sh
curl 'http://localhost:8080/requests/3115a0a9-1e32-47ce-867d-1f4f4924990a/result?page=0' -H 'Accept: application/xml'
```

## References

The FHIR Implementation Guide, that is the basis of the conversions, can be found [here][1]. The data elements of the target format can be found [here][2].

## License

Copyright 2021 The Samply Community

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[1]: <https://simplifier.net/oncology/>
[2]: <https://mdr.ccp-it.dktk.dkfz.de/view.xhtml?namespace=dktk>
