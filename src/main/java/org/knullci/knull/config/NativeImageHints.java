package org.knullci.knull.config;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * GraalVM Native Image hints for reflection used by Thymeleaf and SpEL.
 */
@Configuration
@ImportRuntimeHints(NativeImageHints.ThymeleafRuntimeHints.class)
public class NativeImageHints {

    static class ThymeleafRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register Collection classes used by Thymeleaf/SpEL
            registerCollectionHints(hints, ArrayList.class);
            registerCollectionHints(hints, LinkedList.class);
            registerCollectionHints(hints, HashSet.class);
            registerCollectionHints(hints, LinkedHashSet.class);
            registerCollectionHints(hints, TreeSet.class);

            // Register Map classes
            registerMapHints(hints, HashMap.class);
            registerMapHints(hints, LinkedHashMap.class);
            registerMapHints(hints, TreeMap.class);

            // Register interfaces
            registerCollectionHints(hints, List.class);
            registerCollectionHints(hints, Set.class);
            registerCollectionHints(hints, Collection.class);
            registerMapHints(hints, Map.class);

            // Register domain model classes for Thymeleaf SpEL access
            registerDomainClass(hints, "org.knullci.knull.domain.model.KnullUserDetails");
            registerDomainClass(hints, "org.knullci.knull.domain.model.User");
            registerDomainClass(hints, "org.knullci.knull.domain.model.Job");
            registerDomainClass(hints, "org.knullci.knull.domain.model.Build");
            registerDomainClass(hints, "org.knullci.knull.domain.model.BuildStep");
            registerDomainClass(hints, "org.knullci.knull.domain.model.Credentials");
            registerDomainClass(hints, "org.knullci.knull.domain.model.JobConfig");
            registerDomainClass(hints, "org.knullci.knull.domain.model.SimpleJobConfig");
            registerDomainClass(hints, "org.knullci.knull.domain.model.MultiBranchJobConfig");
            registerDomainClass(hints, "org.knullci.knull.domain.model.Settings");
            registerDomainClass(hints, "org.knullci.knull.domain.model.SecretFile");
            registerDomainClass(hints, "org.knullci.knull.domain.model.TokenCredential");
            registerDomainClass(hints, "org.knullci.knull.domain.model.UsernamePasswordCredential");
            registerDomainClass(hints, "org.knullci.knull.domain.enums.Role");
            registerDomainClass(hints, "org.knullci.knull.domain.enums.Permission");

            // Register String methods for SpEL
            registerStringHints(hints);
        }

        private void registerDomainClass(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(clazz, builder -> {
                    builder.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS,
                            org.springframework.aot.hint.MemberCategory.INTROSPECT_PUBLIC_METHODS,
                            org.springframework.aot.hint.MemberCategory.INTROSPECT_DECLARED_METHODS,
                            org.springframework.aot.hint.MemberCategory.DECLARED_FIELDS,
                            org.springframework.aot.hint.MemberCategory.PUBLIC_FIELDS);
                });
            } catch (ClassNotFoundException e) {
                // Class not found, skip
            }
        }

        private void registerStringHints(RuntimeHints hints) {
            hints.reflection().registerType(String.class, builder -> {
                builder.withMembers(
                        org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS,
                        org.springframework.aot.hint.MemberCategory.INTROSPECT_PUBLIC_METHODS);
            });
        }

        private void registerCollectionHints(RuntimeHints hints, Class<?> clazz) {
            hints.reflection().registerType(clazz, builder -> {
                builder.withMembers(
                        org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS,
                        org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS,
                        org.springframework.aot.hint.MemberCategory.INTROSPECT_PUBLIC_METHODS,
                        org.springframework.aot.hint.MemberCategory.INTROSPECT_DECLARED_METHODS);
            });

            // Explicitly register common methods
            try {
                hints.reflection().registerMethod(
                        clazz.getMethod("isEmpty"), ExecutableMode.INVOKE);
                hints.reflection().registerMethod(
                        clazz.getMethod("size"), ExecutableMode.INVOKE);
                hints.reflection().registerMethod(
                        clazz.getMethod("iterator"), ExecutableMode.INVOKE);
            } catch (NoSuchMethodException e) {
                // Method not found on this class, skip
            }
        }

        private void registerMapHints(RuntimeHints hints, Class<?> clazz) {
            hints.reflection().registerType(clazz, builder -> {
                builder.withMembers(
                        org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS,
                        org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS,
                        org.springframework.aot.hint.MemberCategory.INTROSPECT_PUBLIC_METHODS,
                        org.springframework.aot.hint.MemberCategory.INTROSPECT_DECLARED_METHODS);
            });

            try {
                hints.reflection().registerMethod(
                        clazz.getMethod("isEmpty"), ExecutableMode.INVOKE);
                hints.reflection().registerMethod(
                        clazz.getMethod("size"), ExecutableMode.INVOKE);
                hints.reflection().registerMethod(
                        clazz.getMethod("keySet"), ExecutableMode.INVOKE);
                hints.reflection().registerMethod(
                        clazz.getMethod("values"), ExecutableMode.INVOKE);
                hints.reflection().registerMethod(
                        clazz.getMethod("entrySet"), ExecutableMode.INVOKE);
            } catch (NoSuchMethodException e) {
                // Method not found on this class, skip
            }
        }
    }
}
