package ru.alumni.hub.subtlenotify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SpringFoxConfig {                                    
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.basePackage("ca.mss.credorax.controller"))
          .apis(RequestHandlerSelectors.any())              
          .paths(PathSelectors.any())                          
          .build()
          .apiInfo(metadata());                                           
    }
    
    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title( "Payment gateway front end" )
                .description( "The gateway accepts credit card payments for processing and maintains a transaction history." )
                .version( "1.0" )
                .build();
    }
}