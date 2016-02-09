package io.pivotal.weatherbus.app.testUtils;

import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.TestLifecycle;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.plugins.RxJavaTestPlugins;
import rx.schedulers.Schedulers;

import java.lang.reflect.Method;

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
            RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
                @Override
                public Scheduler getMainThreadScheduler() {
                    return Schedulers.immediate();
                }
            });
            RxJavaPlugins.getInstance().registerSchedulersHook(new RxJavaSchedulersHook() {
                @Override
                public Scheduler getNewThreadScheduler() {
                    return Schedulers.immediate();
                }
            });
            MockitoAnnotations.initMocks(test);
        }

        @Override
        public void afterTest(Method method) {
            super.afterTest(method);
            RxAndroidPlugins.getInstance().reset();
            RxJavaTestPlugins.resetPlugins();
        }
    }
}
