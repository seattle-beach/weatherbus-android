package io.pivotal.weatherbus.app;

import com.google.inject.util.Modules;
import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

import static roboguice.RoboGuice.*;

public class WeatherBusTestApplication extends WeatherBusApplication implements TestLifecycleApplication {
    private final ApplicationModule module = new WeatherBusApplication.ApplicationModule();
    private final TestApplicationModule testModule = new TestApplicationModule();

    @Override
    public void onCreate() {
        super.onCreate();
        setBaseApplicationInjector(this, DEFAULT_STAGE, newDefaultRoboModule(this),
                Modules.override(module).with(testModule));
    }

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {
        getInjector(this).injectMembers(test);
    }

    @Override
    public void afterTest(Method method) {

    }
}
