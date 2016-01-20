package io.pivotal.weatherbus.app;

import org.mockito.ArgumentMatcher;
import retrofit.client.Request;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class TestUtilities {
    public static FileReader fixtureReader(String fixture) throws FileNotFoundException {
        return new FileReader("src/test/resources/input/" + fixture + ".json");
    }

    public static class RequestWithUrl extends ArgumentMatcher<Request> {
        private String expectedUrl;

        public RequestWithUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
        }

        @Override
        public boolean matches(Object request) {
            if (!(request instanceof Request)) return false;
            return ((Request) request).getUrl().equals(expectedUrl);
        }
    }
}
