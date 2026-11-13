package it.mmzitarosa.guitartortona;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

// CORS e sicurezza: vedi it.mmzitarosa.guitartortona.security.SecurityConfig
@Configuration @EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class GuitarTortonaWebConfig {

}
