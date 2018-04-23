package io.dico.dicore;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegistratorListenerTag {

    String[] events() default {};

    EventPriority priority() default EventPriority.HIGHEST;

    boolean ignoreCancelled() default true;
}
