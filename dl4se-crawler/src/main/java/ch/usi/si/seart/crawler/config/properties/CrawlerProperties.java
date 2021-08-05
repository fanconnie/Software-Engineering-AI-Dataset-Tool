package ch.usi.si.seart.crawler.config.properties;

import ch.usi.si.seart.validation.constraints.FileExtension;
import ch.usi.si.seart.validation.constraints.LanguageName;
import com.google.api.client.http.GenericUrl;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.CollectionUtils;
import org.springframewor