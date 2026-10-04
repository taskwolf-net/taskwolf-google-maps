package net.taskwolf.google.maps.structure;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseComparison;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class GoogleMapsDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "google_maps";

  public static GoogleMapsDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("apiKey", DatabaseDataType.TEXT));
    var table = new GoogleMapsDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable ownerView;

  private GoogleMapsDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    ownerView = createMaterializedViewIfNotExists("owner_view", "owner",
      DatabaseColumn.Type.PARTITION_KEY);
  }

  public CompletableFuture<Void> insertGoogleMaps(GoogleMaps googleMaps) {
    return insertGoogleMaps(googleMaps.id(), googleMaps.ownerId(),
      googleMaps.name(), googleMaps.apiKey());
  }

  public CompletableFuture<Void> insertGoogleMaps(
    UUID id, UUID ownerId, String name, String apiKey
  ) {
    return insert(DatabaseRow.of(id, ownerId, name, apiKey));
  }

  public CompletableFuture<UUID> generateAvailableGoogleMapsId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    googleMapsExists(id).thenApply(exists -> exists ?
      generateAvailableGoogleMapsId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public void deleteGoogleMaps(UUID id) {
    delete(id);
  }

  public CompletableFuture<Boolean> googleMapsExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Boolean> googleMapsExistsByOwner(UUID ownerId) {
    var condition = DatabaseCondition.of(
      DatabaseComparison.create("owner", ownerId));
    return ownerView.exists(condition);
  }

  public CompletableFuture<GoogleMaps> findGoogleMaps(UUID id) {
    return selectRow(id).thenApply(row -> GoogleMaps.of(row, this));
  }

  public CompletableFuture<List<GoogleMaps>> findGoogleMapsOfOwner(UUID ownerId) {
    var condition = DatabaseCondition.of(
      DatabaseComparison.create("owner", ownerId));
    return ownerView.selectRows(condition).thenApply(rows ->
      rows.stream().map(row -> GoogleMaps.of(row, ownerView)).toList());
  }
}
