package com.dulno.google.maps.action.details;

import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class GoogleMapsPlaceDetailsAction implements Action<GoogleMapsPlaceDetailsActionExecutor> {
  public static GoogleMapsPlaceDetailsAction create(
    InputComponentSelect googleMapsAccountSelect,
    GoogleMapsDatabaseTable googleMapsDatabaseTable,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("account", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("place", DatabaseDataType.TEXT));
    return new GoogleMapsPlaceDetailsAction(
      googleMapsAccountSelect, googleMapsDatabaseTable,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_google_maps_place_details", contentColumns));
  }

  private final InputComponentSelect googleMapsAccountSelect;
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-maps-place-details-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("google.maps.action.place.details.name")
      .withDescription("google.maps.action.place.details.description")
      .withInputVariable(InputComponentVariable.createSelect("google.maps.action.place.details.input.account.name",
        "accountId", "google.maps.action.place.details.input.account.description", googleMapsAccountSelect))
      .withInputVariable(InputComponentVariable.createRequired("google.maps.action.place.details.input.place.name",
        "placeId", "google.maps.action.place.details.input.place.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.place.details.output.place.name", "placeName"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.place.details.output.place.website", "placeWebsite"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.place.details.output.place.phone", "placePhone"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.place.details.output.place.id", "placeId"))
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
      UUID.fromString((String) content.get("accountId")), content.get("placeId")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("accountId", row.findCell(2).uuidValue().toString(),
        "placeId", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<GoogleMapsPlaceDetailsActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      GoogleMapsPlaceDetailsActionExecutor.create(googleMapsDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).uuidValue(),
        content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}