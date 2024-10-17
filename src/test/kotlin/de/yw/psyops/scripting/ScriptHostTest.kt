package de.yw.psyops.scripting

import de.yw.psyops.mappings.KICK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class ScriptHostTest {

    @Test
    fun simpleScript(@TempDir tempDir: Path) {
        val scriptLocation = tempDir.resolve("myScript.kts")
        val script = """
            midiLoops = mutableListOf(
               fillSteps(
                   floatArrayOf(1.0f, 0.25f, 0.25f),
                   8,
                   KICK
               )
            )
            """.trimIndent()
        scriptLocation.writeText(script)

        val midiLoops = loadLoopsFromScript(scriptLocation.toFile())

        assertThat(midiLoops).satisfiesExactly({ midiLoop ->
            assertThat(midiLoop.noteMap.values).satisfiesExactlyInAnyOrder(
                { note ->
                    assertThat(note.chance).isEqualTo(1.0f)
                    assertThat(note.midiNote).isEqualTo(KICK)
                },
                { note ->
                    assertThat(note.chance).isEqualTo(0.25f)
                    assertThat(note.midiNote).isEqualTo(KICK)
                },
                { note ->
                    assertThat(note.chance).isEqualTo(0.25f)
                    assertThat(note.midiNote).isEqualTo(KICK)
                },
            )
        })
    }
}