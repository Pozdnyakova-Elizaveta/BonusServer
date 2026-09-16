package org.example;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.example.Config.AppConfig;
import org.example.Config.WebConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext rootContext = new AnnotationConfigApplicationContext(AppConfig.class);
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(rootContext.getEnvironment().getProperty("server.port", "8080")));
        tomcat.setBaseDir(Files.createTempDirectory("tomcat").toAbsolutePath().toString());
        tomcat.getConnector();
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
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
