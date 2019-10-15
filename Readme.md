# Crawler

A multi-threaded recursive crawler, implemented with [apache camel](https://camel.apache.org/).
Crawling ability is really basic, only text/html content will be downloaded and stored downloaded locally. 

## Build 

Tweak configuration in `application.properties` and use [maven](https://maven.apache.org/) for building.

## Usage

Package the fat JAR, from project directory:

    mvn package

Then start `crawler` with:

    java -jar target/scraper-0.0.1.jar http://mywebsite.com 
    
Pages will be saved to `output` folder in local directory, to stop crawling use `CTRL+C`