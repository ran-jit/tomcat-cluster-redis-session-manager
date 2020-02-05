package tomcat.request.session.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** author: Ranjith Manickam @ 5 Feb' 2020 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String name() default "";

    String defaultValue() default "";

    PropertyType type() default PropertyType.STRING;

    enum PropertyType {
        STRING,
        BOOLEAN,
        INTEGER,
        LONG
    }
}
