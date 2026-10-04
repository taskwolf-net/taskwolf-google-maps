package net.taskwolf.google.maps;

import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class GoogleMapsInjectionModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  GoogleMapsDatabaseTable provideGoogleMapsDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return GoogleMapsDatabaseTable.create(connection, keyspace);
  }
}
