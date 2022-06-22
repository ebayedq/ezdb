package de.eztools.ezdb.shell;

import de.eztools.ezdb.api.shell.ConnectionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringShellConfig {

    @Bean
    @Qualifier("target")
    public ConnectionService getTargetConnectionService() {
        return new PooledConnectionService();
    }

    @Bean
    @Qualifier("source")
    public ConnectionService getSourceConnectionService() {
        return new PooledConnectionService();
    }
}