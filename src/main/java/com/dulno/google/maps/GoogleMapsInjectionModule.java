package com.dulno.google.maps;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
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
    var googleMapsDatabaseTable = GoogleMapsDatabaseTable.create(connection, keyspace);
    googleMapsDatabaseTable.createIfNotExists();
    googleMapsDatabaseTable.createIndexIfNotExists("owner");
    return googleMapsDatabaseTable;
  }
}
