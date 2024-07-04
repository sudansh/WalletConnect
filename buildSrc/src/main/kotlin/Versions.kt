import org.gradle.api.JavaVersion

//Latest versions
const val BOM_VERSION = "1.32.1"

val jvmVersion = JavaVersion.VERSION_11
const val MIN_SDK: Int = 23
const val TARGET_SDK: Int = 34
const val COMPILE_SDK: Int = TARGET_SDK
const val SAMPLE_VERSION_NAME = BOM_VERSION