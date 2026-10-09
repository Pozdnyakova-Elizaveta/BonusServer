package org.example;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.example.Config.AppConfig;
import org.example.Config.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.nio.file.Files;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            log.error("Fatal error during startup", e);
            System.exit(1);
        }
    }
    private static void run() throws Exception {
        //AnnotationConfigApplicationContext — Spring-контекст, который конфигурируется Java-классами с @Configuration (а не XML).
        AnnotationConfigApplicationContext rootContext = new AnnotationConfigApplicationContext(AppConfig.class);
        //Создаёт экземпляр встроенного Tomcat
        Tomcat tomcat = new Tomcat();
        //Установка порта из конфигурации
        tomcat.setPort(Integer.parseInt(rootContext.getEnvironment().getProperty("server.port", "8080")));
        //Установка рабочей директории tomcat, чтобы не засорять рабочую директорию проекта и не конфликтовать между запусками
        tomcat.setBaseDir(Files.createTempDirectory("tomcat").toAbsolutePath().toString());
        //вызов создаёт и регистрирует default-коннектор, если его ещё нет. Без него Tomcat запустится, но не будет слушать ни одного порта
        tomcat.getConnector();
        //"" — context path. Приложение доступно по http://localhost:8080/..., без префикса.
        //new File(".").getAbsolutePath() — docBase: абсолютный путь к рабочей директории проекта
        //Возвращает созданный Context, с которым дальше работаем: добавляем фильтры и сервлеты.
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        //Регистрация фильтра Spring Security
        FilterDef securityFilterDef = new FilterDef();
        //специальное имя, которое ищет Spring Security
        securityFilterDef.setFilterName("springSecurityFilterChain");
        //специальный фильтр-обёртка Spring'а: он не содержит логики сам, а делегирует вызов бину с именем springSecurityFilterChain из Spring-контекста.
        securityFilterDef.setFilterClass("org.springframework.web.filter.DelegatingFilterProxy");
        //добавляет описание фильтра в web-контекст Tomcat
        ctx.addFilterDef(securityFilterDef);
        //маппинг фильтра на URL-паттерны
        FilterMap securityFilterMap = new FilterMap();
        //ссылка на FilterDef по имени (springSecurityFilterChain)
        securityFilterMap.setFilterName("springSecurityFilterChain");
        //фильтр применяется ко всем URL
        securityFilterMap.addURLPattern("/*");
        //регистрирует маппинг в контексте
        ctx.addFilterMap(securityFilterMap);
        //Создание контекста DispatcherServlet AnnotationConfigWebApplicationContext — вариант Spring-контекста, адаптированный для web (WebApplicationContext).
        AnnotationConfigWebApplicationContext dispatcherCtx = new AnnotationConfigWebApplicationContext();
        //dispatcherCtx (дочерний) видит бины rootContext (родительского) - классическая иерархия в Spring MVC
        dispatcherCtx.setParent(rootContext);
        //Регистрирует WebConfig как @Configuration-класс в dispatcher-контексте. Аналог @Configuration для слоя web
        dispatcherCtx.register(WebConfig.class);
        //DispatcherServlet — центральный сервлет Spring MVC. Принимает все HTTP-запросы, находит @Controller, вызывает метод, обрабатывает возвращаемое значение.
        Wrapper dispatcher = Tomcat.addServlet(ctx, "dispatcher", new DispatcherServlet(dispatcherCtx));
        //loadOnStartup = 1 — сервлет инициализируется при старте контейнера, а не при первом запросе.
        dispatcher.setLoadOnStartup(1);
        //"/" — default servlet mapping: DispatcherServlet обрабатывает все запросы, которые не попали в более специфичные маппинги.
        ctx.addServletMappingDecoded("/", "dispatcher");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received, stopping application...");
            try {
                //Останавливаем Tomcat
                tomcat.stop();
                // destroy() освобождает ресурсы (порты, потоки), stop() — только graceful shutdown
                tomcat.destroy();
                log.info("Tomcat stopped");
            } catch (Exception e) {
                log.error("Error while stopping Tomcat", e);
            }
            try {
                //Закрываем дочерний web-контекст, если он ещё жив
                if (dispatcherCtx.isActive()) {
                    dispatcherCtx.close();
                }
            } catch (Exception e) {
                log.error("Error while closing dispatcher context", e);
            }
            try {
                //Закрываем корневой контекст (бины, @PreDestroy, пулы)
                if (rootContext.isActive()) {
                    rootContext.close();
                }
                log.info("Spring contexts closed");
            } catch (Exception e) {
                log.error("Error while closing root context", e);
            }
        }, "app-shutdown"));
        //Запускает Tomcat
        tomcat.start();
        //Без await() main завершится, JVM выйдет, Tomcat остановится.
        tomcat.getServer().await();
    }
}
