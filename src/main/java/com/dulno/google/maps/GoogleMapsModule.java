package com.dulno.google.maps;

import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.core.trigger.TriggerRepository;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.google.maps.select.GoogleMapsAccountSelect;
import com.dulno.google.maps.structure.GoogleMapsDatabaseTable;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.springframework.boot.SpringApplication;

@ModuleDescription(name = "google-maps", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class GoogleMapsModule extends Module {
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
      ModuleInformation.Type.PUBLIC);
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
    return repository;
  }
}