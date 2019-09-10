What
----

This is a menagerie of data structure and algorithm implementations
that I run into. The implementation language is Java 11.

Setup
-----

This codebase is instrumented with Gradle, mostly to avoid checking
in a few trivial dependencies. The Gradle wrapper, which is checked
into this repo, will try to download Gradle when a task is executed
for the first time.

IDE Setup
---------

I'm using Intellij IDEA with this codebase. Run `gradlew idea` from
the root of the repo, then open the generated `.ipr` file from within
IDEA. If you are an Eclipse user, execute `gradlew eclipse`
instead.

Running Examples
----------------

Some of the implementations have `main()` methods that function as
demos. In those cases, the implementation class offers info about
usage and argument flags. Try running the class with no flags or
with `-h` to see how to use the demo.

Code examples can be run directly from within your IDE. You will
likely need to create a run configuration to configure JVM args
and program arguments.

Alternatively, demos can be run using Gradle from the command line,
albeit with a little ceremony. From the root of the repo, execute
```shell
    gradlew runExample -Pmain=CLASS
```
`CLASS` must be the fully qualified name of the class containing
the main method (namely, `package.Class`, not just `Class`).

Arguments can be passed to demo programs using the syntax
`--args '-foo -bar -baz'`.

You can also pass JVM arguments, separated with spaces, using
`-PjvmArgs='<>'`. Suggestions: `-Xms###m` and `-Xmx###m` for
setting program heap size and `-XX:+HeapDumpOnOutOfMemoryError`
with `-XX:HeapDumpPath=###` if you'd like to do heap analysis.

