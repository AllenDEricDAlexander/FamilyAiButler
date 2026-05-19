package top.egon.familyaibutler.family.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.configuration
 * @ClassName: SwaggerConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-11:39
 * @Description: SwaggerConfig
 * @Version: 1.0
 */

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swaggerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FamilyAIButler-BaseModel")
                        .description("FamilyAIButler-BaseModel")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Egon")
                                .url("https://github.com/AllenDEricDAlexander/FamilyAiButler")
                                .email("mqa010225@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("blog")
                        .url("https://allendericdalexander.github.io/")
                )
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .scheme("token")
                                        .name("DOC_TOKEN")
                                        .in(SecurityScheme.In.HEADER)
                        ))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
}