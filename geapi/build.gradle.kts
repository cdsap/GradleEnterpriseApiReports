plugins {
    id("application")
    id("kotlin")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("io.github.cdsap.geapi.MainKt")
}

//
// open class FatBinaryTask : DefaultTask() {
//
//    @InputFile
//    val fatJar: RegularFileProperty = project.objects.fileProperty()
//
//    @OutputFile
//    val outputFile: RegularFileProperty = project.objects.fileProperty()
//
//    @TaskAction
//    fun buildBinary() {
//        val fileJar = fatJar.get()
//        outputFile.get().asFile.apply {
//            parentFile.mkdirs()
//            delete()
//            writeText("#!/bin/sh\n\nexec java \$JAVA_OPTS -jar \$0 \"\$@\"\n\n")
//            appendBytes(fileJar.asFile.readBytes())
//            setExecutable(true)
//        }
//    }
// }

dependencies {
    implementation("io.ktor:ktor-client-core:2.0.3")
    implementation("io.ktor:ktor-client-cio:2.0.3")
    implementation("io.ktor:ktor-client-auth:2.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.3")

    implementation("io.ktor:ktor-serialization-gson:2.0.3")
    implementation("com.jakewharton.picnic:picnic:0.6.0")
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
}

//
// tasks.register<Jar>("fatJar") {
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//    group = "Build"
//    description = "Produces a fatJar for the Reporter"
//
//    dependsOn(project.tasks.named("jar"))
//
//    manifest {
//        attributes("Main-Class" to "io.github.cdsap.geapi.MainKt")
//    }
//
//    inputs.files(project.configurations.getByName("runtimeClasspath"))
//    from(project.configurations.getByName("runtimeClasspath").map {
//        if (it.isDirectory) it else project.zipTree(it)
//    })
//    with(project.tasks["jar"] as CopySpec)
// }
//
//
// tasks.register<FatBinaryTask>("fatBinary") {
//    group = "Build"
//    description = "Produces a executable binary for the Reporter"
//
//    dependsOn(tasks.named("fatJar"))
//
//    this.fatJar.set((tasks.named("fatJar") as TaskProvider<Jar>).get().archiveFile)
//    this.outputFile.set(File("${project.buildDir}/libs/geapi"))
// }
