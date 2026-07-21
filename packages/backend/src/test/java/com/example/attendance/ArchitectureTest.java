package com.example.attendance;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.attendance");
    }

    @Test
    @DisplayName("Controller は Repository に直接依存しない")
    void controllers_should_not_depend_on_repositories() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("Entity は Controller, Service に依存しない")
    void entities_should_not_depend_on_upper_layers() {
        noClasses()
                .that().resideInAPackage("..entity..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..controller..", "..service..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("domain パッケージは infrastructure に依存しない")
    void domain_should_not_depend_on_infrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }
}
