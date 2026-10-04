package net.taskwolf.google.maps;

import net.taskwolf.core.account.AccountLink;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.workflow.integration.Integration;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.workflow.trigger.TriggerRepository;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.google.maps.action.details.GoogleMapsPlaceDetailsAction;
import net.taskwolf.google.maps.action.geocode.GoogleMapsGeocodeAction;
import net.taskwolf.google.maps.action.search.GoogleMapsSearchPlacesAction;
import net.taskwolf.google.maps.select.GoogleMapsAccountSelect;
import net.taskwolf.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.springframework.boot.SpringApplication;

@ModuleDescription(name = "google-maps", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class GoogleMapsModule extends Integration {
  private Log log;
  private SpringApplication springApplication;
  private GoogleMapsContextInitializer contextInitializer;
  private AccountLink accountLink;
  private InputComponentSelect googleMapsAccountSelect;


  public GoogleMapsModule(Injector injector) {
    super(injector.createChildInjector(GoogleMapsInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Google Maps");
    springApplication = injector().getInstance(SpringApplication.class);
    var googleMapsDatabaseTable = injector().getInstance(GoogleMapsDatabaseTable.class);
    contextInitializer = GoogleMapsContextInitializer.create(googleMapsDatabaseTable);
    springApplication.addInitializers(contextInitializer);
    accountLink = GoogleMapsAccountLink.create(googleMapsDatabaseTable);
    googleMapsAccountSelect = GoogleMapsAccountSelect.create(googleMapsDatabaseTable);
  }

  @Override
  public void disable() {
    var initializers = Lists.newArrayList(springApplication.getInitializers());
    initializers.remove(contextInitializer);
    springApplication.setInitializers(initializers);
  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Google Maps", "", "google-maps",
      ModuleInformation.Type.PUBLIC, ModuleInformation.Novelty.NEW);
  }

  @Override
  public TriggerRepository triggerRepository() {
    return TriggerRepository.create();
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var googleMapsDatabaseTable = injector().getInstance(GoogleMapsDatabaseTable.class);
    var repository = ActionRepository.create();
    repository.registerAction(GoogleMapsGeocodeAction.create(
      googleMapsAccountSelect, googleMapsDatabaseTable, databaseConnection,
      databaseKeyspace));
    repository.registerAction(GoogleMapsSearchPlacesAction.create(
      googleMapsAccountSelect, googleMapsDatabaseTable, databaseConnection,
      databaseKeyspace));
    repository.registerAction(GoogleMapsPlaceDetailsAction.create(
      googleMapsAccountSelect, googleMapsDatabaseTable, databaseConnection,
      databaseKeyspace));
    return repository;
  }
}