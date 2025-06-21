package codes.dimitri.mediminder.api.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
class OpenAPIConfiguration {
    @Bean
    public OpenAPI openAPI(OAuth2ResourceServerProperties properties) {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("oauth2"))
            .components(new Components()
                .addSecuritySchemes("oauth2", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl(properties.getJwt().getIssuerUri() + "authorize?audience=https://api.mediminder.app")
                            .tokenUrl(properties.getJwt().getIssuerUri() + "oauth/token")))));
    }

    static {
        SpringDocUtils.getConfig().replaceWithClass(ZoneId.class, String.class);
    }
}
