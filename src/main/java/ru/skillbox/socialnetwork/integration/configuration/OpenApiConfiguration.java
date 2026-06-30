package ru.skillbox.socialnetwork.integration.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI openApiDescription() {
        Server localhostServer = new Server();
        localhostServer.setUrl("http://localhost:8765");
        localhostServer.setDescription("Local mc-integration API");

        Server productionServer = new Server();
        productionServer.setUrl("http://localhost:8081");
        productionServer.setDescription("Production mc-integration API");

        Contact contact = new Contact();
        contact.setName("Aleksei Gorshkov");
        contact.setEmail("alexngorshkov@gmail.com");
        contact.setUrl("https://linkedin.com/in/alexngorshkov");

        License mitLicense = new License().name("MIT Licence")
                .url("https://choosealicense.com/licenses/mit/");
        Info info = new Info()
                .title("Social Network mc-integration API")
                .version("1.0")
                .contact(contact)
                .description("Social Network mc-integration API")
                .termsOfService("https://github.com/alexngorshkov")
                .license(mitLicense);
        return new OpenAPI().info(info).servers(List.of(localhostServer, productionServer));
    }
}
