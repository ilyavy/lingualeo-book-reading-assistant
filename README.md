# English books reader helper
The GUI application allows to analyze English books and add the most frequently used in the text words into https://lingualeo.com dictionary for further learning.

It is supposed that it will help to learn the most frequently used words before reading the book, to make reading experience more smooth and enjoyable.

**Supported book formats:**
- txt

## Usage
The application is distributed via jar-file.

In order to build the executable jar-file from the sources, it's needed to use the command:
```shell
mvn package
```
Then jar-file can be launched from the `target` directory:
```shell
java -jar lingualeo-book-reading-helper.jar
```

The application will start in a new window.

## Project's structure
```
├── src
    └── main      # source-set of the appliaction
    └── perf      # source-set of the performance benchmark
├── book-samples  # books samples
```
