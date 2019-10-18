package com.mulesoft.hashicorp.consul.provider.api;

import com.mulesoft.hashicorp.consul.provider.api.exception.PropertyNotFoundException;
import com.mulesoft.hashicorp.consul.provider.api.exception.UnknownConsulException;
import com.mulesoft.hashicorp.consul.provider.api.exception.ConsulAccessException;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider to read Consul properties from the Consul server.
 */
public class ConsulConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsulConfigurationPropertiesProvider.class);

    private final static String CONSUL_PROPERTIES_PREFIX = "consul::";
    private final static Pattern CONSUL_PATTERN = Pattern.compile(CONSUL_PROPERTIES_PREFIX + "([^.}]*)");
    private final static Pattern ENV_PATTERN = Pattern.compile("\\$\\[([^\\]]*)\\]");

    private final Consul consul;
    private final String envPrefix;
    private final ConfigurationParameters parameters;
    private final ResourceProvider externalResourceProvider;

    /**
     * Constructs a ConsulConfigurationPropertiesProvider. Consul must not be null.
     * @param consul
     */
    public ConsulConfigurationPropertiesProvider(final Consul consul,
                                                 final ConfigurationParameters parameters,
                                                 final ResourceProvider externalResourceProvider) {
        this.consul = consul;
        this.parameters = parameters;
        this.envPrefix = getEnvPrefix(parameters);
        this.externalResourceProvider = externalResourceProvider;
    }

    private String getEnvPrefix(ConfigurationParameters parameters) {
        String baseKeyPrefix;
        try {
            baseKeyPrefix = parameters.getStringParameter("baseKeyPrefix");
        } catch (Exception e) {
            LOGGER.debug("baseKeyPrefix parameter is not present");
            baseKeyPrefix = "";
        }
        return baseKeyPrefix;
    }

    /**
     * Retrieves the property value from Consul. It stores the retrieved path in a Map so a Dynamic Secrets can be used.
     *
     * @param path     the path to the secret
     * @return         the value of the property or null if the property is not found
     */
    private Optional<String> getProperty(String path) throws PropertyNotFoundException, ConsulAccessException, UnknownConsulException {

        try {

            Optional<String> value = consul.keyValueClient().getValueAsString(path);

            if (value.isPresent()) {
                return value;
            }

        } catch (ConsulException ve) {
            if (ve.getCode() == 404) {
                LOGGER.error("Error getting data from Consul, property not found", ve);
                throw new PropertyNotFoundException("The property at " + path + " was not found", ve);
            } else if (ve.getCode() == 403) {
                LOGGER.error("Error getting data from Consul, access denied", ve);
                throw new ConsulAccessException("Access to the property at " + path + " is denied", ve);
            } else {
                LOGGER.error("Error getting data from Consul", ve);
                throw new UnknownConsulException("Unknown Consul exception", ve);
            }

        }

        return Optional.empty();
    }

    /**
     * Get a configuration property value from Consul.
     *
     * @param configurationAttributeKey  the key to lookup
     * @return                           the String value of the property
     */
    @Override
    public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {

        if (configurationAttributeKey.startsWith(CONSUL_PROPERTIES_PREFIX)) {
            Matcher matcher = CONSUL_PATTERN.matcher(configurationAttributeKey);
            if (matcher.find()) {

                final String effectiveKey = envPrefix.concat(configurationAttributeKey.substring(CONSUL_PROPERTIES_PREFIX.length()));

                try {
                    final Optional<String> value = getProperty(effectiveKey);

                    if (value.isPresent()) {
                        return Optional.of(new ConfigurationProperty() {

                            @Override
                            public Object getSource() {
                                return "Consul provider source";
                            }

                            @Override
                            public Object getRawValue() {
                                return value.get();
                            }

                            @Override
                            public String getKey() {
                                return effectiveKey;
                            }
                        });
                    }
                } catch (Exception e) {
                    return Optional.empty();
                }

                return Optional.empty();

            }
        }
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Consul properties provider";
    }

}
