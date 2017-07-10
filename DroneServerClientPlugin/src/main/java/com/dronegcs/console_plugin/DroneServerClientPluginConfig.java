package com.dronegcs.console_plugin;

import com.dronedb.persistence.ws.internal.*;
import com.dronegcs.console_plugin.exceptions.ClientPluginException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by talma on 5/25/17.
 */
@Configuration
public class DroneServerClientPluginConfig {

    // DB Access

    private static <T> T LoadServices(Class<T> clz) throws ClientPluginException {
        try {
            return getSrvicePort(clz, "127.0.0.1", 1234);
        }
        catch (Throwable e) {
            System.out.print("Failed to connect to the local database server, " + e.getMessage());

            try {
                return getSrvicePort(clz, "178.62.1.156", 1234);
            }
            catch (Throwable e1) {
                System.out.print("Failed to connect to the external database server, " + e.getMessage());
            }
        }

        throw new ClientPluginException("Failed to build connection to web service port");
    }

    private static <T> T getSrvicePort(Class<T> clz, String ipStr, int port) throws MalformedURLException {
        System.err.println("Got " + clz.getSimpleName() + " on ip " + ipStr + " port " + port);
        //URL url = new URL("http://localhost:9999/ws/" + clz.getSimpleName() + "?wsdl");
        URL url = new URL("http://" + ipStr + ":" + port + "/ws/" + clz.getSimpleName() + "?wsdl");
        QName qName = new QName("http://internal.ws.persistence.dronedb.com/", clz.getSimpleName() + "ImplService");
        Service service = Service.create(url, qName);
        return service.getPort(clz);
    }

    @Bean
    public MissionCrudSvcRemote missionCrudSvcRemote() throws ClientPluginException {
        return LoadServices(MissionCrudSvcRemote.class);
    }

    @Bean
    public QuerySvcRemote querySvcRemote() throws ClientPluginException {
        return LoadServices(QuerySvcRemote.class);
    }

    @Bean
    public DroneDbCrudSvcRemote droneDbCrudSvcRemote() throws ClientPluginException {
        return LoadServices(DroneDbCrudSvcRemote.class);
    }

    @Bean
    public PerimeterCrudSvcRemote perimeterCrudSvcRemote() throws ClientPluginException {
        return LoadServices(PerimeterCrudSvcRemote.class);
    }

    @Bean
    public SessionsSvcRemote sessionsSvcRemote() throws ClientPluginException {
        return LoadServices(SessionsSvcRemote.class);
    }

}
