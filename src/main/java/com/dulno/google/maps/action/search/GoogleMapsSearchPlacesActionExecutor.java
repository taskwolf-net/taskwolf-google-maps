package com.dulno.google.maps.action.search;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.dulno.google.maps.structure.GoogleMaps;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.common.collect.Lists;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResult;
import lombok.AllArgsConstructor;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class GoogleMapsSearchPlacesActionExecutor implements ActionExecutor {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final UUID ownerId;
  private final UUID accountId;
  private String placesQuery;
  private String locationLatitude;
  private String locationLongitude;
  private String searchRadius;
  private String pages;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    try {
      var dissolve = PlaceholderDissolve.create(information);
      placesQuery = dissolve.dissolve(placesQuery);
      locationLatitude = dissolve.dissolve(locationLatitude);
      var locationLatitude = Double.parseDouble(this.locationLatitude);
      locationLongitude = dissolve.dissolve(locationLongitude);
      var locationLongitude = Double.parseDouble(this.locationLongitude);
      searchRadius = dissolve.dissolve(searchRadius);
      var searchRadius = Integer.parseInt(this.searchRadius);
      pages = dissolve.dissolve(pages);
      var pages = Math.min(Integer.parseInt(this.pages), 3);
      return googleMapsDatabaseTable.googleMapsExists(accountId)
        .thenCompose(exists -> execute(exists, locationLatitude,
          locationLongitude, searchRadius, pages));
    } catch (Exception exception) {
      return ActionResult.futureFailure("google.maps.action.places.search.failure.wrong.format");
    }
  }

  private CompletableFuture<ActionResult> execute(
    boolean googleMapsExists, double locationLatitude, double locationLongitude,
    int searchRadius, int pages
    ) {
    if (!googleMapsExists) {
      return ActionResult.futureFailure("google.maps.action.places.search.failure.account.not.found");
    }
    return googleMapsDatabaseTable.findGoogleMaps(accountId)
      .thenApplyAsync(account -> execute(account, locationLatitude,
        locationLongitude, searchRadius, pages));
  }

  private ActionResult execute(
    GoogleMaps account, double locationLatitude, double locationLongitude,
    int searchRadius, int pages
  ) {
    try {
      if (!account.ownerId().equals(ownerId)) {
        return ActionResult.failure("google.maps.action.places.search.failure.account.not.found");
      }
      var context = new GeoApiContext.Builder().apiKey(account.apiKey()).build();
      var location = new LatLng(locationLatitude, locationLongitude);
      var searchResponse = PlacesApi.nearbySearchQuery(context, location)
        .radius(searchRadius).keyword(placesQuery).await();
      var places = Lists.newArrayList(searchResponse.results);
      var nextPageToken = searchResponse.nextPageToken;
      var pageCount = 1;
      while (nextPageToken != null && pageCount < pages) {
        Thread.sleep(2000);
        searchResponse = PlacesApi.nearbySearchNextPage(context, nextPageToken)
          .await();
        places.addAll(List.of(searchResponse.results));
        nextPageToken = searchResponse.nextPageToken;
        pageCount++;
      }
      return ActionResult.success(buildInformation(places.stream()
        .map(this::buildPlaceInformation).toList()));
    } catch (Exception exception) {
      return ActionResult.failure(exception.getMessage());
    }
  }

  private Map<String, Object> buildPlaceInformation(PlacesSearchResult place) {
    var information = Maps.<String, Object>newHashMap();
    information.put("placeId", place.placeId);
    information.put("placeName", place.name);
    information.put("placeAddress", place.vicinity);
    return information;
  }

  private Map<String, Object> buildInformation(List<Map<String, Object>> places) {
    var information = Maps.<String, Object>newHashMap();
    information.put("places", new JSONArray(places));
    information.put("placesNumber", places.size());
    information.put("placesQuery", placesQuery);
    information.put("locationLatitude", locationLatitude);
    information.put("locationLongitude", locationLongitude);
    information.put("searchRadius", searchRadius);
    information.put("pages", pages);
    return information;
  }
}