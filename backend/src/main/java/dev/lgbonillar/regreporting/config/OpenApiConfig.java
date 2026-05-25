package dev.lgbonillar.regreporting.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI regulatoryReportingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Regulatory Reporting Platform API")
                        .version("0.1.0")
                        .description("""
                                  API for uploading regulatory report files, processing jobs,
                                  approvals, rejections, revocations, auditability and JWT authentication.
                                  """)
                        .contact(new Contact()
                                .name("Luis Bonilla")
                                .url("https://lgbonillar.dev")))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
