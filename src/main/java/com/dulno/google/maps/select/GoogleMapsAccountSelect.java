package com.dulno.google.maps.select;

import com.dulno.core.user.User;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentSelectEntry;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public class GoogleMapsAccountSelect implements InputComponentSelect {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return googleMapsDatabaseTable.findGoogleMapsOfOwner(target)
      .thenApply(googleMaps -> googleMaps.stream()
        .map(entry -> InputComponentSelectEntry.create(entry.id().toString(),
          entry.name()))
        .toList());
  }
}