package net.taskwolf.google.maps.action.search;

import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.action.ActionContentDatabaseTable;
import net.taskwolf.workflow.action.ActionInformation;
import net.taskwolf.core.database.*;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.ListOutputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class GoogleMapsSearchPlacesAction implements Action<GoogleMapsSearchPlacesActionExecutor> {
  public static GoogleMapsSearchPlacesAction create(
    InputComponentSelect googleMapsAccountSelect,
    GoogleMapsDatabaseTable googleMapsDatabaseTable,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("account", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("query", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("latitude", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("longitude", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("radius", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("pages", DatabaseDataType.TEXT));
    return new GoogleMapsSearchPlacesAction(
      googleMapsAccountSelect, googleMapsDatabaseTable,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_google_maps_places_search", contentColumns));
  }

  private final InputComponentSelect googleMapsAccountSelect;
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "google-maps-places-search-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("google.maps.action.places.search.name")
      .withDescription("google.maps.action.places.search.description")
      .withInputVariable(InputComponentVariable.createSelect("google.maps.action.places.search.input.account.name",
        "accountId", "google.maps.action.places.search.input.account.description", googleMapsAccountSelect))
      .withInputVariable(InputComponentVariable.createRequired("google.maps.action.places.search.input.query.name",
        "placesQuery", "google.maps.action.places.search.input.query.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("google.maps.action.places.search.input.latitude.name",
        "locationLatitude", "google.maps.action.places.search.input.latitude.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("google.maps.action.places.search.input.longitude.name",
        "locationLongitude", "google.maps.action.places.search.input.longitude.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("google.maps.action.places.search.input.radius.name",
        "searchRadius", "google.maps.action.places.search.input.radius.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("google.maps.action.places.search.input.pages.name",
        "pages", "google.maps.action.places.search.input.pages.description", InputComponentDataType.TEXT))
      .withOutputVariable(ListOutputComponentVariable.create("google.maps.action.places.search.output.places", "places",
        OutputComponentVariable.create("google.maps.action.places.search.output.place.id", "placeId"),
        OutputComponentVariable.create("google.maps.action.places.search.output.place.name", "placeName"),
        OutputComponentVariable.create("google.maps.action.places.search.output.place.address", "placeAddress")))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.places.search.output.places.length", "placesNumber"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.places.search.output.query", "placesQuery"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.places.search.output.latitude", "locationLatitude"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.places.search.output.longitude", "locationLongitude"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.places.search.output.radius", "searchRadius"))
      .withOutputVariable(OutputComponentVariable.create("google.maps.action.places.search.output.pages", "pages"))
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
    var pages = content.get("pages");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      UUID.fromString((String) content.get("accountId")),
      content.get("placesQuery"), content.get("locationLatitude"),
      content.get("locationLongitude"), content.get("searchRadius"),
      pages == null ? "" : pages));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("accountId", row.findCell(2).uuidValue().toString(),
        "placesQuery", row.findCell(3).stringValue(),
        "locationLatitude", row.findCell(4).stringValue(),
        "locationLongitude", row.findCell(5).stringValue(),
        "searchRadius", row.findCell(6).stringValue(),
        "pages", row.findCell(7).stringValue()));
  }

  @Override
  public CompletableFuture<GoogleMapsSearchPlacesActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      GoogleMapsSearchPlacesActionExecutor.create(googleMapsDatabaseTable,
        content.findCell(1).uuidValue(), content.findCell(2).uuidValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue(),
        content.findCell(5).stringValue(), content.findCell(6).stringValue(),
        content.findCell(7).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}