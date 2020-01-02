import java.io.File
import java.io.File.separator
import java.net.URL

object Util {

    fun downloadLibFromUrl(lib: ExternalLib,
        libSaveDir: String = "${System.getProperty("user.home")}${separator}.gradle${separator}caches${separator}modules-2${separator}files-2.1${separator}download"): String {

        val folder = File(libSaveDir)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file = File("$libSaveDir/${lib.libFullName}")
        if (!file.exists()) {
            URL(lib.libUrl).openStream().readAllBytes().also { file.appendBytes(it) }
        }
        return file.absolutePath
    }
}
