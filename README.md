# RgxReplaceIO
RgxReplaceIO is an IO library which provides you with streams able to replace the content,
that matches a specific regex, while being read/written.  
The library comprises 2 byte streams and 2 character streams (input/output)

## Usage
```Java
Reader underlyingStream = ...
ReplaceReader reader = new ReplaceReader(underlyingStream, "regex to be matched", "replacement");
```
You can also specify buffer size, instead of using the default one:
```Java
int bufferSize = 1024;
ReplaceReader reader = new ReplaceReader(underlyingStream, "regex to be matched", "replacement", bufferSize);
```
Then use it as a normal stream while replacements automatically take place underneath.
## TODO
+ implement readLine(), skip() and ready() in ReplaceReader
+ develop ReplaceWriter
+ develop ReplaceInputStream and ReplaceOutputStream
