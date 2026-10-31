package com.dawn.catalog.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    static JavaClasses classes;

    @BeforeAll
    static void load() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.dawn");
    }

    // catalog subdomain is hexagonal-lite: services must not touch infra
    @Test
    void catalog_services_should_not_depend_on_infra() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.dawn.catalog.service..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.dawn.catalog.infra..");
        rule.check(classes);
    }

    @Test
    void rating_services_should_not_depend_on_infra() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.dawn.rating.service..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.dawn.rating.infra..");
        rule.check(classes);
    }

    @Test
    void controllers_should_not_depend_on_port_out() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..port.out..");
        rule.check(classes);
    }

    // builder and media are layered: controllers must not touch repositories
    @Test
    void controllers_should_not_depend_on_repositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..");
        rule.check(classes);
    }
}
