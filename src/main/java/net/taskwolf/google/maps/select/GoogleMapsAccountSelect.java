package net.taskwolf.google.maps.select;

import net.taskwolf.core.user.User;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentSelectEntry;
import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
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