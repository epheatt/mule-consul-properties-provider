package com.mulesoft.hashicorp.consul.provider.api;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclarer;
import org.mule.runtime.api.meta.model.display.DisplayModel;
import org.mule.runtime.api.meta.model.display.LayoutModel;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.runtime.api.meta.Category.SELECT;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

/**
 * Declares extension for Consul Properties Provider
 *
 */
public class ConsulConfigurationPropertiesExtensionLoadingDelegate implements ExtensionLoadingDelegate {

  public static final String EXTENSION_NAME = "Consul Properties Provider";
  public static final String CONFIG_ELEMENT = "config";

  public static final String AUTH_PARAMETER_GROUP = "auth";
  public static final String HOST_PARAMETER_GROUP = "host";


  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext context) {

    ConfigurationDeclarer configurationDeclarer = extensionDeclarer.named(EXTENSION_NAME)
            .describedAs(String.format("%s Extension", EXTENSION_NAME))
            .withCategory(SELECT)
            .onVersion("1.0.0")
            .fromVendor("Mulesoft Consulting")
            .withConfig(CONFIG_ELEMENT);    // This defines a global element in the extension with name config

    ParameterGroupDeclarer defaultParameterGroup = configurationDeclarer.onDefaultParameterGroup();
    defaultParameterGroup
            .withRequiredParameter("consulUrl")
            .withDisplayModel(DisplayModel.builder().displayName("Consul URL").build())
            .ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("URL for the Consul Server");
    defaultParameterGroup
            .withOptionalParameter("baseKeyPrefix").defaultingTo("")
            .withDisplayModel(DisplayModel.builder().displayName("Consul Base Key Prefix").build())
            .ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Consul Base Key Prefix");


  }

  /**
   * Add the Basic Connection parameters to the parameter list
   *
   * @param configurationDeclarer Extension {@link ConfigurationDeclarer}
   */
  private void addAuthParameters(ConfigurationDeclarer configurationDeclarer) {
    ParameterGroupDeclarer authParameterGroup = configurationDeclarer
            .onParameterGroup(AUTH_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);
    authParameterGroup
            .withOptionalParameter("token")
            .withDisplayModel(DisplayModel.builder().displayName("Consul Auth Token").build())
            .ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Consul Auth Token with access to necessary kv context");
    authParameterGroup
            .withOptionalParameter("username")
            .withDisplayModel(DisplayModel.builder().displayName("Username").build())
            .ofType(BaseTypeBuilder.create(JAVA).numberType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Basic Auth Username");
    authParameterGroup
            .withOptionalParameter("password")
            .withDisplayModel(DisplayModel.builder().displayName("Password").build())
            .ofType(BaseTypeBuilder.create(JAVA).numberType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Basic Auth Password");
  }

  /**
   * Add the Host and Port Connection parameters to the parameter list
   *
   * @param configurationDeclarer Extension {@link ConfigurationDeclarer}
   */
  private void addHostParameters(ConfigurationDeclarer configurationDeclarer) {
    ParameterGroupDeclarer hostParameterGroup = configurationDeclarer
            .onParameterGroup(HOST_PARAMETER_GROUP)
            .withDslInlineRepresentation(true);
    hostParameterGroup
            .withOptionalParameter("scheme")
            .withDisplayModel(DisplayModel.builder().displayName("Scheme").build())
            .ofType(BaseTypeBuilder.create(JAVA).stringType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("HTTP or HTTPS");
    hostParameterGroup
            .withRequiredParameter("address")
            .withDisplayModel(DisplayModel.builder().displayName("Address").build())
            .ofType(BaseTypeBuilder.create(JAVA).numberType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Consul Hostname or IP Address");
    hostParameterGroup
            .withRequiredParameter("port")
            .withDisplayModel(DisplayModel.builder().displayName("Port]").build())
            .ofType(BaseTypeBuilder.create(JAVA).numberType().build())
            .withExpressionSupport(ExpressionSupport.SUPPORTED)
            .describedAs("Consul Port");
  }

}
