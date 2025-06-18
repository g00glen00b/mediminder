package codes.dimitri.mediminder.api.common;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
class OpenAPIConfiguration {
    static {
        SpringDocUtils.getConfig().replaceWithClass(ZoneId.class, String.class);
    }
}
