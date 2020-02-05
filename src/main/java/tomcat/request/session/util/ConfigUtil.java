package tomcat.request.session.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.request.session.annotation.Property;
import tomcat.request.session.model.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/** author: Ranjith Manickam @ 5 Feb' 2020 */
public class ConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    private static final String CONF = "conf";
    private static final String CATALINA_BASE = "catalina.base";

    /** To get application config. */
    public static Config getConfig() {
        Properties properties = getApplicationProperties();
        Config config = new Config();

        for (Field field : Config.class.getDeclaredFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property == null) {
                continue;
            }

            String propertyName = property.name();
            Property.PropertyType propertyType = property.type();

            if (propertyName.isEmpty()) {
                continue;
            }

            String value = properties.getProperty(propertyName);
            if (isSystemProperty(value)) {
                value = getSystemProperty(value);
            }

            if (value == null || value.isEmpty()) {
                value = property.defaultValue();

                if (value.isEmpty()) {
                    continue;
                }
            }

            field.setAccessible(true);
            try {
                switch (propertyType) {
                    case BOOLEAN:
                        field.set(config, Boolean.parseBoolean(value));
                        break;
                    case INTEGER:
                        field.set(config, Integer.parseInt(value));
                        break;
                    case LONG:
                        field.set(config, Long.parseLong(value));
                        break;
                    case STRING:
                    default:
                        field.set(config, value);
                        break;
                }
            } catch (Exception ex) {
                LOGGER.error("Error while initializing application properties", ex);
            }
        }

        return config;
    }

    /** To get redis data cache properties. */
    private static Properties getApplicationProperties() {
        Properties properties = new Properties();
        try {
            String filePath = System.getProperty(CATALINA_BASE)
                    .concat(File.separator)
                    .concat(CONF).concat(File.separator)
                    .concat(Config.APPLICATION_PROPERTIES_FILE);

            InputStream resourceStream = null;
            try {
                resourceStream = (!filePath.isEmpty() && new File(filePath).exists()) ? new FileInputStream(filePath) : null;
                if (resourceStream == null) {
                    LOGGER.info("Initializing tomcat redis session manager with default properties");
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    resourceStream = loader.getResourceAsStream(Config.APPLICATION_PROPERTIES_FILE);
                }
                properties.load(resourceStream);
            } finally {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error while retrieving application properties", ex);
        }
        return properties;
    }

    /**
     * To get property with the specified key from system property.
     *
     * @param key - search key.
     * @return - Returns the system property value.
     */
    private static String getSystemProperty(String key) {
        int fromIndex = 0;

        while (true) {
            int beginIndex = key.indexOf("${", fromIndex);
            int endIndex = key.indexOf("}", fromIndex);

            if (beginIndex < 0 || endIndex < 0) {
                break;
            }

            String expression = key.substring(beginIndex + 2, endIndex);
            String value = System.getProperty(expression);

            if (value == null || value.isEmpty()) {
                fromIndex = endIndex + 1;
                continue;
            }

            key = key.replace(String.format("${%s}", expression), value);
        }

        return key;
    }

    /**
     * To check if the value is from system property.
     *
     * @param key - search key.
     * @return - Returns true if the key start with '${' and ends with '}'.
     */
    private static boolean isSystemProperty(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        int beginIndex = key.indexOf("${");
        int endIndex = key.indexOf("}");
        return beginIndex >= 0 && endIndex >= 0 && beginIndex < endIndex;
    }
}
