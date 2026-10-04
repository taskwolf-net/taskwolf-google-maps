package net.taskwolf.google.maps;

import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.account.AccountLinkEntry;
import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
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
        .map(entry -> AccountLinkEntry.create(entry.id().toString(), entry.name()))
        .toList());
  }

  @Override
  public void removeAccount(UUID id, String identifier) {
    googleMapsDatabaseTable.deleteGoogleMaps(UUID.fromString(identifier));
  }

  @Override
  public String registrationUrl(UUID id, String apiKey) {
    return "https://taskwolf.net/google/maps/connect/";
  }

  @Override
  public String description() {
    return "google.maps.link.description";
  }
}