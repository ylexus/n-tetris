package net.yudichev.ntetris;

import org.immutables.value.Value;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target({PACKAGE, TYPE})
@Retention(CLASS) // Make it class retention for incremental compilation
@Value.Style(get = {"is*", "get*"}, init = "set*", typeAbstract = "Base*", typeImmutable = "*", visibility = Value.Style.ImplementationVisibility.PUBLIC)
public @interface PublicImmutablesStyle {
}
