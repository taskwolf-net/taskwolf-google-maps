package com.dulno.google.maps;

import com.dulno.core.account.AccountLink;
import com.dulno.core.account.AccountLinkEntry;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class GoogleMapsAccountLink implements AccountLink {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;

  @Override
  public CompletableFuture<Boolean> accountExists(UUID id) {
    return googleMapsDatabaseTable.googleMapsExistsByOwner(id);
  }

  @Override
  public CompletableFuture<List<AccountLinkEntry>> findAccounts(UUID id) {
    return googleMapsDatabaseTable.findGoogleMapsOfOwner(id)
      .thenApply(googleMaps -> googleMaps.stream()
        .map(entry -> AccountLinkEntry.create(entry.id().toString(), ""))
        .toList());
  }

  @Override
  public void removeAccount(UUID id, String identifier) {
    googleMapsDatabaseTable.deleteGoogleMaps(UUID.fromString(identifier));
  }

  @Override
  public String registrationUrl(UUID id, String apiKey) {
    return "https://dulno.com/google/maps/connect/";
  }

  @Override
  public String description() {
    return "google.maps.link.description";
  }
}