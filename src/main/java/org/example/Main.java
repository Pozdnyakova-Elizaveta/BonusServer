package org.example;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.example.Config.AppConfig;
import org.example.Config.WebConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext rootContext = new AnnotationConfigApplicationContext(AppConfig.class);
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(rootContext.getEnvironment().getProperty("server.port", "8080")));
        tomcat.setBaseDir(Files.createTempDirectory("tomcat").toAbsolutePath().toString());
        tomcat.getConnector();
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        FilterDef securityFilterDef = new FilterDef();
        securityFilterDef.setFilterName("springSecurityFilterChain");
        securityFilterDef.setFilterClass("org.springframework.web.filter.DelegatingFilterProxy");
        ctx.addFilterDef(securityFilterDef);

        FilterMap securityFilterMap = new FilterMap();
        securityFilterMap.setFilterName("springSecurityFilterChain");
        securityFilterMap.addURLPattern("/*");
        ctx.addFilterMap(securityFilterMap);
        AnnotationConfigWebApplicationContext dispatcherCtx = new AnnotationConfigWebApplicationContext();
        dispatcherCtx.setParent(rootContext);
        dispatcherCtx.register(WebConfig.class);
        Wrapper dispatcher = Tomcat.addServlet(ctx, "dispatcher", new DispatcherServlet(dispatcherCtx));
        dispatcher.setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/", "dispatcher");
        tomcat.start();
        tomcat.getServer().await();
    }
}
