Annotation-based command line interpreter based on the [JArgs Project](http://jargs.sourceforge.net).

Example:

```java
@EnableHelp(showOnExeption = true)
@ApplicationName(fromJar = true)
public class ImageConverter{
  @Option(shortForm = "i", longForm = "input", description = "inputfile")
  private String inputFile = "";
  @Option(shortForm = "o", longForm = "output", description = "outputfile")
  private String outputFile = "";
  @Option(shortForm = "s", longForm = "s", description = "(optional) image height")
  @InInterval(min = "0")
  private Integer s;
  @Option(shortForm = "t", longForm = "t", description = "(optional) image width")
  @InInterval(min = "0")
  private Integer t;

  public void convert() {
    convertImpl(inputFile, outputFile, s, t);
  }

  private void convertImpl(String inputFile, String outputFile, Integer s, Integer t) {
    // ...
  }
}

public class Main {
  public static void main(String[] args) {
    CommandLineReader<ImageConverter> reader = CommandLineReader.of(ImageConverter.class);
      ImageConverter app = reader.read(args);
      if (!reader.helpRequested()) {
        app.convert();
      }
    }
  }
}
```

TODO:

- mandatory values
 