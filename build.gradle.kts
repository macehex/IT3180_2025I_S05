plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    // Enable modularity for normal builds, disable for fat JAR
    modularity.inferModulePath.set(true)
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainModule.set("com.example.quanlytoanha")
    mainClass.set("com.example.quanlytoanha.Launcher")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.graphics", "javafx.base")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

dependencies {
    implementation("io.github.palexdev:materialfx:11.17.0")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    
    // Use Spring Security with excluded transitive dependencies that cause module issues
    implementation("org.springframework.security:spring-security-core:6.5.5") {
        exclude(group = "io.micrometer", module = "micrometer-observation")
        exclude(group = "io.micrometer", module = "micrometer-commons")
        exclude(group = "org.springframework", module = "spring-context")
        exclude(group = "org.springframework", module = "spring-aop")
        exclude(group = "org.springframework", module = "spring-expression")
    }
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.junit.platform:junit-platform-suite:1.12.1")
    testImplementation("org.mockito:mockito-core:5.5.0")         // Mockito core
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0") // Tích hợp Mockito với JUnit 5
}

// Alternative 1: Fat JAR approach (simpler, avoids module issues)
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes["Main-Class"] = "com.example.quanlytoanha.Launcher"
    }
    
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
}

// Task to compile without module-info.java for fat JAR
tasks.register<JavaCompile>("compileJavaForFatJar") {
    source = fileTree("src/main/java") {
        exclude("module-info.java")
    }
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/fatjar"))
    classpath = configurations.compileClasspath.get()
    options.encoding = "UTF-8"
    options.compilerArgs.add("--enable-preview")
}

// Modified fat JAR task that uses non-modular compilation
tasks.register<Jar>("fatJarNoModule") {
    dependsOn("compileJavaForFatJar", "processResources")
    archiveClassifier.set("fat-nomodule")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes["Main-Class"] = "com.example.quanlytoanha.Launcher"
    }
    
    from(tasks.getByName("compileJavaForFatJar").outputs)
    from(tasks.processResources.get().outputs)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

// Alternative 2: Simplified jlink (try this if fat JAR doesn't work)
jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages", "--ignore-signing-information"))
    launcher {
        name = "app"
    }
    // Force all non-modular JARs onto classpath instead of module path
    forceMerge("log4j")
    forceMerge("slf4j") 
    forceMerge("spring")
}
