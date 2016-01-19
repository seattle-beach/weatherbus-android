package io.pivotal.weatherbus.app.testUtils;

import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.TestLifecycle;

public class WeatherBusTestRunner extends RobolectricGradleTestRunner {
    public WeatherBusTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return TestLifeCycleWithInjection.class;
    }

    public static class TestLifeCycleWithInjection extends DefaultTestLifecycle {
        @Override
        public void prepareTest(Object test) {
            super.prepareTest(test);
            MockitoAnnotations.initMocks(test);
        }
    }
}
