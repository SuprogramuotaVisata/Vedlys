package com.suprogramuota_visata.vedlys.utils

import java.util.Locale

sealed class ScriptValidationResult {
    data object Valid : ScriptValidationResult()
    data class Invalid(val reason: String) : ScriptValidationResult()
}

object ScriptValidator {

    private val DANGEROUS_COMMANDS = listOf(
        // Destructive OS file / disk commands
        "rmdir", "rm -rf", "Remove-Item", "del /f", "del /s", "del /q",
        "format ", "Format-Volume", "Diskpart", "shutil.rmtree", "os.remove", "os.rmdir",
        
        // System shutdown / process kill / policy override
        "Stop-Computer", "Restart-Computer", "Set-ExecutionPolicy", "Disable-NetAdapter",
        "taskkill", "shutdown /s", "shutdown /r", "os.system(\"shutdown", "subprocess.call([\"shutdown",
        "sys.exit", "exit()", "os._exit",
        
        // Dynamic code injection
        "Invoke-Expression", "iex ", "eval(", "exec(", "__import__('os').system",
        
        // Protected Windows system locations
        "C:\\Windows", "System32", "Program Files", "HKLM:", "HKCU:"
    )

    fun validateScript(name: String, language: String, codeOrPath: String): ScriptValidationResult {
        val trimmedName = name.trim()
        val trimmedContent = codeOrPath.trim()

        if (trimmedName.isBlank()) {
            return ScriptValidationResult.Invalid("Skripto pavadinimas negali būti tuščias.")
        }

        if (trimmedContent.isBlank()) {
            return ScriptValidationResult.Invalid("Skripto kodo turinys arba failo kelias negali būti tuščias.")
        }

        // File extension validation
        val isPython = language.equals("PYTHON", ignoreCase = true)
        val isPowerShell = language.equals("POWERSHELL", ignoreCase = true)

        if (isPython && !trimmedName.endsWith(".py", ignoreCase = true)) {
            return ScriptValidationResult.Invalid("Python skripto pavadinimas privalo turėti plėtinį '.py'.")
        }

        if (isPowerShell && !trimmedName.endsWith(".ps1", ignoreCase = true) && !trimmedName.endsWith(".bat", ignoreCase = true) && !trimmedName.endsWith(".cmd", ignoreCase = true)) {
            return ScriptValidationResult.Invalid("PowerShell skripto pavadinimas privalo turėti plėtinį '.ps1' arba '.bat'.")
        }

        // Syntax bracket balance checks
        var openParentheses = 0
        var openBrackets = 0
        var openBraces = 0

        for (char in trimmedContent) {
            when (char) {
                '(' -> openParentheses++
                ')' -> openParentheses--
                '[' -> openBrackets++
                ']' -> openBrackets--
                '{' -> openBraces++
                '}' -> openBraces--
            }
            if (openParentheses < 0 || openBrackets < 0 || openBraces < 0) {
                return ScriptValidationResult.Invalid("Skripto kodo sintaksės klaida: nesubalansuoti arba nesutampantys skliaustai.")
            }
        }

        if (openParentheses != 0 || openBrackets != 0 || openBraces != 0) {
            return ScriptValidationResult.Invalid("Skripto kodo sintaksės klaida: neatidaryti arba neuždaryti skliaustai.")
        }

        // Safety Inspection (OS & 3rd Party App Protection)
        val contentUpper = trimmedContent.uppercase(Locale.getDefault())
        for (dangerousCmd in DANGEROUS_COMMANDS) {
            if (contentUpper.contains(dangerousCmd.uppercase(Locale.getDefault()))) {
                return ScriptValidationResult.Invalid(
                    "Saugumo klaida: Aptikta pavojinga OS / sistemos komanda '$dangerousCmd'. Skriptas užblokuotas siekiant apsaugoti operacinę sistemą ir kitas programas."
                )
            }
        }

        return ScriptValidationResult.Valid
    }
}
