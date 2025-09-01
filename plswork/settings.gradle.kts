import java.util.Properties
import java.io.File

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // Load GitHub credentials safely
    val githubPropertiesFile = File(rootDir, "github.properties")
    val githubProperties = Properties()
    if (githubPropertiesFile.exists()) {
        githubProperties.load(githubPropertiesFile.inputStream())
    }

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.github.com/qawaz/compose-code-editor") {
            name = "GitHubPackages"
            credentials {
                username = githubProperties.getProperty("gpr.usr") ?: System.getenv("GPR_USER")
                password = githubProperties.getProperty("gpr.key") ?: System.getenv("GPR_API_KEY")
            }
        }
    }
}
rootProject.name = "plswork"
include(":app")
