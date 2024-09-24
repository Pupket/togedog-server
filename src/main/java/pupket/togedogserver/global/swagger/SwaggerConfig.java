package pupket.togedogserver.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        License license = new License();
        license.setName("togedog");

        Info info = new Info()
                .title("\"togedog API Document\"")
                .description("togedog API 문서 입니다.")
                .version("v0.0.1")
                .license(license);

        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt); // 헤더에 토큰 포함
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));

        // 첫 번째 서버 설정 (예: HTTPS)
        Server server1 = new Server();
        server1.setUrl("https://www.walktogedog.life");
        server1.setDescription("Production Server");

        Server server2 = new Server();
        server2.setUrl("http://localhost:8080");
        server2.setDescription("local Server");

        // 두 개의 서버를 Swagger에 추가
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(server1, server2)) // 두 개의 서버 추가
                .addSecurityItem(new SecurityRequirement().addList(jwt));
    }
}
