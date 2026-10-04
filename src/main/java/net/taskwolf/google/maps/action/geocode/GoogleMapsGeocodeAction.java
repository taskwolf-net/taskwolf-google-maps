package net.taskwolf.google.maps.action.geocode;

import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class GoogleMapsGeocodeAction implements Action<GoogleMapsGeocodeActionExecutor> {
  public static GoogleMapsGeocodeAction create(
    InputComponentSelect googleMapsAccountSelect,
    GoogleMapsDatabaseTable googleMapsDatabaseTable,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("account", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("query", DatabaseDataType.TEXT));
    return new GoogleMapsGeocodeAction(
      googleMapsAccountSelect, googleMapsDatabaseTable,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_google_maps_geocode", contentColumns));
  }

  private final InputComponentSelect googleMapsAccountSelect;
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-maps-geocode-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("google.maps.action.geocode.name")
      .withDescription("google.maps.action.geocode.description")
      .withInputVariable(InputComponentVariable.createSelect("google.maps.action.geocode.input.account.name",
        "accountId", "google.maps.action.geocode.input.account.description", googleMapsAccountSelect))
      .withInputVariable(InputComponentVariable.createRequired("google.maps.action.geocode.input.query.name",
        "query", "google.maps.action.geocode.input.query.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.geocode.output.address", "address"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.geocode.output.latitude", "latitude"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.geocode.output.longitude", "longitude"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      UUID.fromString((String) content.get("accountId")), content.get("query")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("accountId", row.findCell(2).uuidValue().toString(),
        "query", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<GoogleMapsGeocodeActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      GoogleMapsGeocodeActionExecutor.create(googleMapsDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).uuidValue(),
        content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}