package com.epitech.epiheader

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.time.LocalDate
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

class EpiHeader : AnAction() {

    private val headersMap = emptyMap<String, String>().plus(
        listOf("h", "hpp", "hh", "hxx", "h++", "c", "cpp", "cc", "cxx", "c++").associateWith {
            """
                /*
                ** EPITECH PROJECT, %d
                ** %s
                ** File description:
                ** %s
                */
                
                """.trimIndent()
        }.plus(
            listOf("makefile", "py").associateWith {
                """
                    ##
                    ## EPITECH PROJECT, %d
                    ## %s
                    ## File description:
                    ## %s
                    ##
                    
                    """.trimIndent()
            }
        )
    )
    override fun actionPerformed(e: AnActionEvent) {

        val project: Project? = e.project
        val file: VirtualFile? = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        val document: Document = e.getData(PlatformDataKeys.EDITOR)!!.document
        val editor = e.getData(PlatformDataKeys.EDITOR)

        val fileName : String = file!!.name
        val fileExtension : String? = file.extension?.lowercase()
        val fileContent : String = document.text

        val header : String? = if (fileName.lowercase() == "makefile" || fileName.lowercase() == "cmakelists.txt") {
            headersMap["makefile"]!!.format(
                LocalDate.now().year,
                project!!.name,
                fileName.split(".")[0]
            )
        } else if (fileExtension != null && headersMap.containsKey(fileExtension.lowercase())) {
            headersMap[fileExtension.lowercase()]!!.format(
                LocalDate.now().year,
                project!!.name,
                fileName
            )
        } else {
            null
        }

        if (header == null) {
            val notification = Notification(
                "plugin.epitech.notificationGroup",
                "EpiHeader",
                (fileExtension?.let { "Extension \"$fileExtension\" not supported" } ?: "File named \"$fileName\" not supported"),
                NotificationType.WARNING
            )
            Notifications.Bus.notify(notification, project)
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            Runnable {
                if (fileContent.startsWith(header)) return@Runnable
                document.setText(header + fileContent)
            }.run()
        }

        val notification = Notification(
            "plugin.epitech.notificationGroup",
            "EpiHeader",
            "EpiHeader applied to $fileName",
            NotificationType.INFORMATION
        )
        Notifications.Bus.notify(notification, project)

        editor!!.caretModel.moveToOffset(header.length)
    }
}
