import java.util.Arrays;

public class Intro {
    public static void main(String[] args) {
        System.out.println(
            "You executed the `runExample` Gradle task"
                + " without specifying a main class. "
                + "You can pass the main class name with"
                + " `gradlew runExample -PmainClass=<FULL CLASS NAME>`.");
        System.out.println(Arrays.toString(args));
    }
}
