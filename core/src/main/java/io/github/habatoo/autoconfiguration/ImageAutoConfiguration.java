package io.github.habatoo.autoconfiguration;

import io.github.habatoo.properties.ImageProperties;
import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.impl.FileNameGeneratorImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ImageProperties.class)
public class ImageAutoConfiguration {

    @Bean
    public FileNameGenerator fileNameGenerator(ImageProperties imageProperties) {
        return new FileNameGeneratorImpl(imageProperties);
    }
}
