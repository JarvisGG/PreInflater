package jarvis.com.preinflater.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yyf @ Zhihu Inc.
 * @since 03-30-2019
 */
@Inherited
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PreInflater {
    int layout();
    String scheduler() default "io";
}
