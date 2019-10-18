package com.mulesoft.hashicorp.consul.provider.api;

import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

/**
 * Builds the provider for a consul:config element.
 */
public class ConsulConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

  private final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigurationPropertiesProviderFactory.class);

  public static final String EXTENSION_NAMESPACE =
      ConsulConfigurationPropertiesExtensionLoadingDelegate.EXTENSION_NAME.toLowerCase().replace(" ", "-");
  private static final ComponentIdentifier CONSUL_PROPERTIES_PROVIDER =
      builder().namespace(EXTENSION_NAMESPACE).name(ConsulConfigurationPropertiesExtensionLoadingDelegate.CONFIG_ELEMENT).build();

  @Override
  public ComponentIdentifier getSupportedComponentIdentifier() {
    return CONSUL_PROPERTIES_PROVIDER;
  }

  @Override
  public ConfigurationPropertiesProvider createProvider(final ConfigurationParameters parameters,
                                                        ResourceProvider externalResourceProvider) {

    try {
      return new ConsulConfigurationPropertiesProvider(getConsul(parameters),parameters,externalResourceProvider);
    } catch (ConsulException ve) {
      LOGGER.error("Error connecting to Consul", ve);
      return null;
    }
  }

  /**
   * Get a consul connection based on the parameters provided by the user
   *
   * @param parameters The parameters read from the Mule config file
   * @return a fully configured {@link Consul} object
   */
  private Consul getConsul(ConfigurationParameters parameters) throws ConsulException {

    String consulUrl = parameters.getStringParameter("consulUrl");

    Consul.Builder consulBuilder = Consul.builder().withUrl(consulUrl);

    List<ConfigurationParameters> authList = parameters
            .getComplexConfigurationParameter(builder()
                    .namespace(EXTENSION_NAMESPACE)
                    .name(ConsulConfigurationPropertiesExtensionLoadingDelegate.AUTH_PARAMETER_GROUP).build());

    if (authList.size() > 0) {
      consulBuilder = getAuthConsulConfig(consulBuilder, authList.get(0));
    }

    try {
      final Consul consul = consulBuilder.build();
      return consul;
    } catch (ConsulException ce){
      throw ce;
    }
  }

  /**
   * Get Consul Builder from the basic parameters
   * @param consulBuilder current state of the Consul Builder
   * @param authParameters parameters from the basic element
   * @return consulBuilder with additional parameters added to it
   */
  private Consul.Builder getAuthConsulConfig(Consul.Builder consulBuilder, ConfigurationParameters authParameters) {
    // parameters.getStringParameter() throws a NullPointerException when the parameter is not present and
    // the getComplexConfigurationParameter() and getComplexConfigurationParameters() are for child elements, so
    // all we can really do is catch the exception when the optional parameters doesn't exist
    boolean tokenAuth = false;
    try {
      consulBuilder = consulBuilder.withTokenAuth(authParameters.getStringParameter("token"));
      tokenAuth = true;
    } catch (Exception e) {
      LOGGER.debug("auth token parameter is not present");
      tokenAuth = false;
    }
    if (!tokenAuth) {
      try {
        consulBuilder = consulBuilder.withBasicAuth(authParameters.getStringParameter("username"),
                                                    authParameters.getStringParameter("password"));
      } catch (Exception e) {
        LOGGER.debug("auth username and password parameters are not present");
      }
    }
    return consulBuilder;
  }

  /**
   * Determines if the resource exists on the classpath
   *
   * @param path path to the file
   * @return true of the resource is on the classpath
   */
  private boolean classpathResourceExists(String path) {
    boolean fileExists = false;
    URL fileUrl = getClass().getResource(path);
    if (fileUrl != null) {
      File file = new File(fileUrl.getFile());
      if (file != null) {
        fileExists = file.exists();
      }
    }
    return fileExists;
  }

}
