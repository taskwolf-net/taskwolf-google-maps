package com.dulno.google.maps.access;

import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.access.DulnoRestController;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public class GoogleMapsController extends DulnoRestController {
  private final GoogleMapsDatabaseTable googleMapsDatabaseTable;
  private final UserTargetDatabaseTable userTargetDatabaseTable;
  private final TeamTargetDatabaseTable teamTargetDatabaseTable;

  private GoogleMapsController(
    Key productKey, UserDatabaseTable userDatabaseTable,
    GoogleMapsDatabaseTable googleMapsDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable
  ) {
    super(productKey, userDatabaseTable);
    this.googleMapsDatabaseTable = googleMapsDatabaseTable;
    this.userTargetDatabaseTable = userTargetDatabaseTable;
    this.teamTargetDatabaseTable = teamTargetDatabaseTable;
  }

  @RequestMapping(path = "/google/maps/add/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> addGoogleMaps(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    return findUser(request)
      .thenCompose(user -> userTargetDatabaseTable.findTargetSecured(user.id())
        .thenCompose(target -> findGoogleMapsOwner(user, target)
          .thenCompose(owner -> googleMapsDatabaseTable.generateAvailableGoogleMapsId()
            .thenApply(id -> addGoogleMaps(id, owner, body.getString("name"),
              body.getString("apiKey"))))));
  }

  private CompletableFuture<UUID> findGoogleMapsOwner(User user, UUID target) {
    return user.id().equals(target) ?
      CompletableFuture.completedFuture(target) :
      teamTargetDatabaseTable.findTargetSecured(user.id())
        .thenApply(team -> team.orElse(target));
  }

  private Map<String, Object> addGoogleMaps(
    UUID id, UUID ownerId, String name, String apiKey
  ) {
    googleMapsDatabaseTable.insertGoogleMaps(id, ownerId,  name, apiKey);
    return Map.of("success", true);
  }
}
