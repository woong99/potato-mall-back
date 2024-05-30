package potatowoong.potatomallback.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdditionalTomcatConfiguration {

    @Value("${server.docs-port}")
    private String docsPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> additionalConnector() {
        return factory -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setPort(Integer.parseInt(docsPort));
            factory.addAdditionalTomcatConnectors(connector);
        };
    }

}
