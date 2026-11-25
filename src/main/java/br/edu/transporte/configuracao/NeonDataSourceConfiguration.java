package br.edu.transporte.configuracao;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Configura PostgreSQL via JDBC. Aceita a DATABASE_URL gerada pelo Neon
 * (postgresql:// ou postgres://) e as variáveis tradicionais BANCO_*.
 */
@Configuration(proxyBeanMethods = false)
public class NeonDataSourceConfiguration {

    @Bean
    DataSource dataSource(Environment environment) {
        String databaseUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("NEON_DATABASE_URL"),
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("BANCO_JDBC_URL"));

        String username = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                environment.getProperty("BANCO_USUARIO"));
        String password = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                environment.getProperty("BANCO_SENHA"));

        String jdbcUrl;
        if (databaseUrl != null) {
            ParsedConnection parsed = toJdbcConnection(databaseUrl);
            jdbcUrl = parsed.jdbcUrl();
            username = firstNonBlank(parsed.username(), username);
            password = firstNonBlank(parsed.password(), password);
        } else {
            String host = required(environment, "BANCO_HOST");
            String port = firstNonBlank(environment.getProperty("BANCO_PORTA"), "5432");
            String database = required(environment, "BANCO_NOME");
            jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        }

        if (username == null || password == null) {
            throw new IllegalStateException(
                    "Defina credenciais PostgreSQL em DATABASE_URL ou BANCO_USUARIO/BANCO_SENHA.");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        dataSource.setPoolName("transporte-postgres");
        return dataSource;
    }

    private static ParsedConnection toJdbcConnection(String connectionString) {
        if (connectionString.startsWith("jdbc:postgresql://")) {
            return new ParsedConnection(ensureSsl(connectionString), null, null);
        }

        URI uri = URI.create(connectionString);
        String scheme = uri.getScheme();
        if (!"postgresql".equalsIgnoreCase(scheme) && !"postgres".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException(
                    "DATABASE_URL deve usar postgres://, postgresql:// ou jdbc:postgresql://.");
        }
        if (uri.getHost() == null || uri.getPath() == null || uri.getPath().length() < 2) {
            throw new IllegalStateException("DATABASE_URL não contém host e nome do banco válidos.");
        }

        String authorityHost = uri.getHost().contains(":") ? "[" + uri.getHost() + "]" : uri.getHost();
        int port = uri.getPort() < 0 ? 5432 : uri.getPort();
        String query = uri.getRawQuery();
        if (query == null || !query.toLowerCase().contains("sslmode=")) {
            query = query == null || query.isBlank() ? "sslmode=require" : query + "&sslmode=require";
        }
        String jdbcUrl = "jdbc:postgresql://" + authorityHost + ":" + port + uri.getRawPath()
                + (query == null ? "" : "?" + query);

        String username = null;
        String password = null;
        if (uri.getRawUserInfo() != null) {
            String[] credentials = uri.getRawUserInfo().split(":", 2);
            username = decode(credentials[0]);
            if (credentials.length > 1) {
                password = decode(credentials[1]);
            }
        }
        return new ParsedConnection(jdbcUrl, username, password);
    }

    private static String ensureSsl(String jdbcUrl) {
        String lower = jdbcUrl.toLowerCase();
        if (lower.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static String required(Environment environment, String name) {
        String value = environment.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente " + name + ".");
        }
        return value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record ParsedConnection(String jdbcUrl, String username, String password) {}
}
