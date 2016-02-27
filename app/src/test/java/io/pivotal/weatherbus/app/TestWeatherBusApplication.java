package io.pivotal.weatherbus.app;
import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

public class TestWeatherBusApplication extends WeatherBusApplication implements TestLifecycleApplication {
    @Override
    protected Object getModule() {
        return new TestApplicationModule();
    }

    @Override
    public void beforeTest(Method method) {
    }

    @Override
    public void prepareTest(Object test) {
    }

    @Override
    public void afterTest(Method method) {

    }
}
