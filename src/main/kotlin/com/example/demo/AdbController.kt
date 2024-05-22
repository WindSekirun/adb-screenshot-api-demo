package com.example.demo

import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

@RestController
class AdbController {

    @GetMapping("/screenshot")
    fun getScreenshot(@RequestParam serial: String): ResponseEntity<InputStreamResource> {
        val processBuilder = ProcessBuilder("adb", "-s", serial, "exec-out", "screencap", "-p")
        val process = processBuilder.start()

        val outputStream = ByteArrayOutputStream()
        process.inputStream.use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }

        val screenshotBytes = outputStream.toByteArray()

        process.waitFor()

        val inputStreamResource = InputStreamResource(ByteArrayInputStream(screenshotBytes))
        // attachment : 다운로드, inline: 뷰어
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=screenshot.png")
            .contentType(MediaType.IMAGE_PNG)
            .body(inputStreamResource)
    }

    @GetMapping("/click")
    fun performClick(
        @RequestParam serial: String,
        @RequestParam x: Int,
        @RequestParam y: Int
    ): ResponseEntity<String> {
        return performCommand {
            ProcessBuilder("adb", "-s", serial, "shell", "input", "tap", x.toString(), y.toString())
        }
    }

    @GetMapping("/home")
    fun moveToHome(
        @RequestParam serial: String,
    ): ResponseEntity<String> {
        return performCommand {
            ProcessBuilder("adb", "-s", serial, "shell", "input", "keyevent", "3")
        }
    }

    @GetMapping("/power")
    fun powerButton(
        @RequestParam serial: String,
    ): ResponseEntity<String> {
        return performCommand {
            ProcessBuilder("adb", "-s", serial, "shell", "input", "keyevent", "26")
        }
    }

    @GetMapping("/brightness")
    fun brightness(
        @RequestParam serial: String,
        @RequestParam percent: Int
    ): ResponseEntity<String> {
        if (percent < 0 || percent > 100) {
            return ResponseEntity.badRequest().body("Invalid parameter")
        }

        val brightnessLevel = (percent * 2.55).toInt()

        performCommand {
            ProcessBuilder("adb", "-s", serial, "shell", "settings", "put", "system", "screen_brightness_mode", "0")
        }
        return performCommand {
            ProcessBuilder(
                "adb",
                "-s",
                serial,
                "shell",
                "settings",
                "put",
                "system",
                "screen_brightness",
                brightnessLevel.toString()
            )
        }
    }


    private fun performCommand(block: () -> ProcessBuilder): ResponseEntity<String> {
        val processBuilder = block()
        val command = processBuilder.command().joinToString(" ")
        return try {
            val process = processBuilder.start()
            process.waitFor()

            if (process.exitValue() == 0) {
                ResponseEntity.ok().body(command)
            } else {
                ResponseEntity.status(500).body("Failed to perform $command")
            }
        } catch (e: IOException) {
            ResponseEntity.status(500).body("Error performing $command: ${e.message}")
        } catch (e: InterruptedException) {
            ResponseEntity.status(500).body("Interrupted: ${e.message}")
        }
    }
}