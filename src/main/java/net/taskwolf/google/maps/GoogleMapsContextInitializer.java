package net.taskwolf.google.maps;

import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
@RequiredArgsConstructor(staticName = "create")
public final class GoogleMapsContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("googleMapsDatabaseTable",
      googleMapsDatabaseTable);
  }
}
