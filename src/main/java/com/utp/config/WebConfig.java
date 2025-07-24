// package com.utp.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// /**
//  * Configuración Web general
//  * 
//  * Configura:
//  * - CORS
//  * - Recursos estáticos
//  * - View controllers
//  * - Interceptores
//  * 
//  */
// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     /**
//      * Configuración de CORS para desarrollo
//      */
//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/api/**")
//                 .allowedOrigins("http://localhost:3000", "http://localhost:8080")
//                 .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
//                 .allowedHeaders("*")
//                 .allowCredentials(true)
//                 .maxAge(3600);
//     }

//     /**
//      * Configuración de recursos estáticos
//      */
//     @Override
//     public void addResourceHandlers(ResourceHandlerRegistry registry) {
//         // CSS, JS, imágenes
//         registry.addResourceHandler("/css/**")
//                 .addResourceLocations("classpath:/static/css/")
//                 .setCachePeriod(31556926); // 1 año en segundos

//         registry.addResourceHandler("/js/**")
//                 .addResourceLocations("classpath:/static/js/")
//                 .setCachePeriod(31556926);

//         registry.addResourceHandler("/img/**")
//                 .addResourceLocations("classpath:/static/img/")
//                 .setCachePeriod(31556926);

//         registry.addResourceHandler("/assets/**")
//                 .addResourceLocations("classpath:/static/assets/")
//                 .setCachePeriod(31556926);

//         // Favicon
//         registry.addResourceHandler("/favicon.ico")
//                 .addResourceLocations("classpath:/static/")
//                 .setCachePeriod(31556926);
//     }

//     /**
//      * Configuración de controladores de vista simples
//      */
//     @Override
//     public void addViewControllers(ViewControllerRegistry registry) {
//         // Redirigir la raíz al dashboard o página principal
//         registry.addViewController("/").setViewName("redirect:/dashboard");
        
//         // Páginas estáticas sin controlador
//         registry.addViewController("/login").setViewName("auth/login");
//         registry.addViewController("/register").setViewName("auth/register");
//         registry.addViewController("/error").setViewName("error/error");
//     }
// }