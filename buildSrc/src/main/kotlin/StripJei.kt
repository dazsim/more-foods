import java.io.FilterReader
import java.io.Reader
import java.io.StringReader

// fabric.mod.json without the JEI entrypoint, for versions with no JEI build
class StripJei(input: Reader) : FilterReader(
    StringReader(input.readText()
        .replace(Regex(""",\s*"jei_mod_plugin":\s*\[[^\]]*\]"""), "")
        .replace(Regex(""",\s*"suggests":\s*\{[^}]*\}"""), ""))
)
