package com.dulno.google.maps.action.details;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.dulno.google.maps.structure.GoogleMaps;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class GoogleMapsPlaceDetailsActionExecutor implements ActionExecutor {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final UUID ownerId;
  private final UUID accountId;
  private String placeId;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    placeId = dissolve.dissolve(placeId);
    return googleMapsDatabaseTable.googleMapsExists(accountId)
      .thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(boolean googleMapsExists) {
    if (!googleMapsExists) {
      return ActionResult.futureFailure("google.maps.action.place.details.failure.account.not.found");
    }
    return googleMapsDatabaseTable.findGoogleMaps(accountId)
      .thenApplyAsync(this::execute);
  }

  private ActionResult execute(GoogleMaps account) {
    try {
      if (!account.ownerId().equals(ownerId)) {
        return ActionResult.failure("google.maps.action.place.details.failure.account.not.found");
      }
      var context = new GeoApiContext.Builder().apiKey(account.apiKey()).build();
      var placeDetails = PlacesApi.placeDetails(context, placeId).await();
      return ActionResult.success(buildInformation(placeDetails));
    } catch (Exception exception) {
      return ActionResult.failure(exception.getMessage());
    }
  }

  private Map<String, Object> buildInformation(PlaceDetails placeDetails) {
    var information = Maps.<String, Object>newHashMap();
    information.put("placeName", placeDetails.name);
    information.put("placeWebsite", placeDetails.website != null ?
      placeDetails.website : "");
    information.put("placePhone", placeDetails.formattedPhoneNumber != null ?
      placeDetails.formattedPhoneNumber : "");
    information.put("placeRating", placeDetails.rating);
    information.put("placeId", placeId);
    return information;
  }
}