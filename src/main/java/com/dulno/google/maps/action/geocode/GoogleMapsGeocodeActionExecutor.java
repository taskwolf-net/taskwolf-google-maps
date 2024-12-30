package com.dulno.google.maps.action.geocode;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.dulno.google.maps.structure.GoogleMaps;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class GoogleMapsGeocodeActionExecutor implements ActionExecutor {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final UUID ownerId;
  private final UUID accountId;
  private String query;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    query = dissolve.dissolve(query);
    return googleMapsDatabaseTable.googleMapsExists(accountId)
      .thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(boolean googleMapsExists) {
    if (!googleMapsExists) {
      return ActionResult.futureFailure("google.maps.action.geocode.failure.account.not.found");
    }
    return googleMapsDatabaseTable.findGoogleMaps(accountId)
      .thenApplyAsync(this::execute);
  }

  private ActionResult execute(GoogleMaps account) {
    try {
      if (!account.ownerId().equals(ownerId)) {
        return ActionResult.failure("google.maps.action.geocode.failure.account.not.found");
      }
      var context = new GeoApiContext.Builder().apiKey(account.apiKey()).build();
      var geocoding = GeocodingApi.geocode(context, query).await();
      if (geocoding.length == 0) {
        return ActionResult.failure("google.maps.action.geocode.failure.geocoding.not.found");
      }
      return ActionResult.success(buildInformation(geocoding[0]));
    } catch (Exception exception) {
      return ActionResult.failure(exception.getMessage());
    }
  }

  private Map<String, Object> buildInformation(GeocodingResult geocoding) {
    var information = Maps.<String, Object>newHashMap();
    information.put("address", geocoding.formattedAddress);
    information.put("latitude", geocoding.geometry.location.lat);
    information.put("longitude", geocoding.geometry.location.lng);
    return information;
  }
}