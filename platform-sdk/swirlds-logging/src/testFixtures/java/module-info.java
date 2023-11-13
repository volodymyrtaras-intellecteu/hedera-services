module com.swirlds.logging.test.fixtures {
    exports com.swirlds.logging.test.fixtures;
    exports com.swirlds.logging.test.fixtures.internal to
            org.junit.platform.commons;

    requires com.swirlds.base.test.fixtures;
    requires transitive com.swirlds.logging;
    requires transitive org.junit.jupiter.api;
    requires static com.github.spotbugs.annotations;
}